import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/** Logical LLM backend used for tracking and policy. */
enum Model {
    CLAUDE,
    OPENAI
}

/** Declared health from failure (x) / recovery (y) windows. */
enum ModelHealth {
    /** Last {@code y} attempts for this model all succeeded. */
    UP,
    /** Last {@code x} attempts for this model all failed (offload / drain). */
    DOWN,
    /** Not enough history, or windows neither fully fail nor fully succeed. */
    UNKNOWN
}

/** Per-model x (failure window), y (recovery window), and traffic % to this model when DOWN. */
final class ModelPolicyConfig {
    private final int x;
    private final int y;
    private final int trafficPercentWhenDown;

    ModelPolicyConfig(int x, int y, int trafficPercentWhenDown) {
        if (x < 1 || y < 1) {
            throw new IllegalArgumentException("x and y must be >= 1");
        }
        if (trafficPercentWhenDown < 0 || trafficPercentWhenDown > 100) {
            throw new IllegalArgumentException("trafficPercentWhenDown must be 0..100");
        }
        this.x = x;
        this.y = y;
        this.trafficPercentWhenDown = trafficPercentWhenDown;
    }

    int x() {
        return x;
    }

    int y() {
        return y;
    }

    int trafficPercentWhenDown() {
        return trafficPercentWhenDown;
    }
}

interface AIStrategy {
    String askPrompt(String prompt);

    Model model();
}

class ClaudeStrategy implements AIStrategy {
    @Override
    public String askPrompt(String prompt) {
        return "Claude: " + prompt;
    }

    @Override
    public Model model() {
        return Model.CLAUDE;
    }
}

class OpenAIStrategy implements AIStrategy {
    @Override
    public String askPrompt(String prompt) {
        return "OpenAI: " + prompt;
    }

    @Override
    public Model model() {
        return Model.OPENAI;
    }
}

class Req {
    final String prompt;
    final Model model;
    final boolean success;

    Req(String prompt, Model model, boolean success) {
        this.prompt = prompt;
        this.model = model;
        this.success = success;
    }
}

/**
 * Tracks LLM requests and exposes counts over a sliding tail window.
 */
class Tracker {
    private final List<Req> requests = new ArrayList<>();

    public void addRequest(String prompt, AIStrategy provider, boolean success) {
        Objects.requireNonNull(provider, "provider");
        requests.add(new Req(prompt, provider.model(), success));
    }

    public void addRequest(Req request) {
        Objects.requireNonNull(request, "request");
        requests.add(request);
    }

    /**
     * Counts entries among the last {@code windowSize} global requests that match
     * {@code success} and {@code model}.
     */
    public int countInLastWindow(int windowSize, boolean success, Model model) {
        Objects.requireNonNull(model, "model");
        if (windowSize <= 0 || requests.isEmpty()) {
            return 0;
        }
        int from = Math.max(0, requests.size() - windowSize);
        int count = 0;
        for (int i = from; i < requests.size(); i++) {
            Req r = requests.get(i);
            if (r.model == model && r.success == success) {
                count++;
            }
        }
        return count;
    }

    /**
     * True iff the {@code n} most recent requests for {@code model} (by time) each have
     * {@code success}. If fewer than {@code n} requests exist for that model, returns false.
     */
    public boolean lastNForModelAll(int n, boolean success, Model model) {
        Objects.requireNonNull(model, "model");
        if (n < 1) {
            return false;
        }
        int matched = 0;
        for (int i = requests.size() - 1; i >= 0 && matched < n; i--) {
            Req r = requests.get(i);
            if (r.model != model) {
                continue;
            }
            if (r.success != success) {
                return false;
            }
            matched++;
        }
        return matched == n;
    }
}

/**
 * Policy from tracker history: x = consecutive failures window (DOWN), y = consecutive successes (UP).
 * Per-model: x.c / y.c / x.oai / y.oai and traffic share when that model is DOWN.
 */
class PolicyProvider {
    private final Tracker tracker;
    private final ModelPolicyConfig claude;
    private final ModelPolicyConfig openai;

    /**
     * @param claude x.c, y.c, traffic to Claude when Claude is DOWN (e.g. 5 → 5% Claude, 95% OAI)
     * @param openai x.oai, y.oai, traffic to OpenAI when OpenAI is DOWN (e.g. 5 → 5% OAI)
     */
    PolicyProvider(Tracker tracker, ModelPolicyConfig claude, ModelPolicyConfig openai) {
        this.tracker = Objects.requireNonNull(tracker, "tracker");
        this.claude = Objects.requireNonNull(claude, "claude");
        this.openai = Objects.requireNonNull(openai, "openai");
    }

    /** Convenience: defaults from REQ — x.c=5,y.c=3,5% Claude when down; x.oai=5,y.oai=3,5% OAI when down. */
    PolicyProvider(Tracker tracker) {
        this(
                tracker,
                new ModelPolicyConfig(5, 3, 5),
                new ModelPolicyConfig(5, 3, 5));
    }

    /**
     * Failure (x) before recovery (y). Use {@link #healthForRouting(Model)} for gateway splits so
     * recovery is honored first.
     */
    public ModelHealth health(Model model) {
        Objects.requireNonNull(model, "model");
        ModelPolicyConfig cfg = configFor(model);
        if (tracker.lastNForModelAll(cfg.x(), false, model)) {
            return ModelHealth.DOWN;
        }
        if (tracker.lastNForModelAll(cfg.y(), true, model)) {
            return ModelHealth.UP;
        }
        return ModelHealth.UNKNOWN;
    }

    /**
     * For routing: check last {@code y} successes first so traffic can move back to the primary
     * as soon as recovery is observed; only then treat last {@code x} all-fail as DOWN.
     */
    public ModelHealth healthForRouting(Model model) {
        Objects.requireNonNull(model, "model");
        ModelPolicyConfig cfg = configFor(model);
        if (tracker.lastNForModelAll(cfg.y(), true, model)) {
            return ModelHealth.UP;
        }
        if (tracker.lastNForModelAll(cfg.x(), false, model)) {
            return ModelHealth.DOWN;
        }
        return ModelHealth.UNKNOWN;
    }

    /** Percent of traffic that should go to {@code model} when it is DOWN (offload knob). */
    public int trafficPercentWhenDown(Model model) {
        return configFor(model).trafficPercentWhenDown();
    }

    /** Suggested share 0–100 aligned with {@link #healthForRouting(Model)}. */
    public int suggestedTrafficPercent(Model model) {
        return healthForRouting(model) == ModelHealth.DOWN
                ? trafficPercentWhenDown(model)
                : 100;
    }

    private ModelPolicyConfig configFor(Model model) {
        if (model == Model.CLAUDE) {
            return claude;
        }
        return openai;
    }
}

/**
 * Claude primary, OpenAI fallback. Weights: 100/0 when Claude not DOWN; 5/95 when Claude DOWN and
 * OpenAI not DOWN; 5/5 with 90% dropped when both DOWN. Uses {@link PolicyProvider#healthForRouting}
 * so Claude recovery ({@code y} successes) is evaluated before failure streak ({@code x}).
 */
class LlmGateway {
    private static final int SHARE_WHEN_PRIMARY_DOWN = 5;
    private static final int SHARE_EACH_WHEN_BOTH_DOWN = 5;

    private final Tracker tracker;
    private final PolicyProvider policy;
    private final AIStrategy claude;
    private final AIStrategy openai;

    LlmGateway(Tracker tracker, PolicyProvider policy, AIStrategy claude, AIStrategy openai) {
        this.tracker = Objects.requireNonNull(tracker, "tracker");
        this.policy = Objects.requireNonNull(policy, "policy");
        this.claude = Objects.requireNonNull(claude, "claude");
        this.openai = Objects.requireNonNull(openai, "openai");
    }

    public String askPrompt(String prompt) {
        Objects.requireNonNull(prompt, "prompt");

        ModelHealth claudeH = policy.healthForRouting(Model.CLAUDE);
        ModelHealth openaiH = policy.healthForRouting(Model.OPENAI);
        boolean claudeDown = claudeH == ModelHealth.DOWN;
        boolean openaiDown = openaiH == ModelHealth.DOWN;

        AIStrategy chosen;
        if (!claudeDown) {
            chosen = claude;
        } else if (!openaiDown) {
            int r = ThreadLocalRandom.current().nextInt(100);
            chosen = r < SHARE_WHEN_PRIMARY_DOWN ? claude : openai;
        } else {
            int r = ThreadLocalRandom.current().nextInt(100);
            if (r < SHARE_EACH_WHEN_BOTH_DOWN) {
                chosen = claude;
            } else if (r < SHARE_EACH_WHEN_BOTH_DOWN * 2) {
                chosen = openai;
            } else {
                return "DROPPED: both providers unhealthy (90% reject bucket)";
            }
        }

        boolean success = true;
        String out;
        try {
            out = chosen.askPrompt(prompt);
        } catch (RuntimeException ex) {
            success = false;
            out = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        }
        tracker.addRequest(prompt, chosen, success);
        return out;
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
