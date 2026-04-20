/**
 * Failure traffic: Claude DOWN (all fails); both Claude and OpenAI DOWN.
 * Run: {@code javac Main.java GatewayFailureTrafficTest.java && java GatewayFailureTrafficTest}
 */
public class GatewayFailureTrafficTest {

    private static final int X = 5;
    private static final int Y = 3;

    public static void main(String[] args) {
        testClaudeInFailure();
        testOpenAiAlsoInFailure();
        System.out.println("GatewayFailureTrafficTest: all passed");
    }

    /**
     * Last five Claude attempts all failed → Claude DOWN. OpenAI still UNKNOWN (no OAI attempts).
     */
    private static void testClaudeInFailure() {
        Tracker tracker = new Tracker();
        boolean[] fails = new boolean[10];
        seedClaudeOnly(tracker, fails);

        ModelPolicyConfig claudeCfg = new ModelPolicyConfig(X, Y, 5);
        ModelPolicyConfig openaiCfg = new ModelPolicyConfig(X, Y, 5);
        PolicyProvider policy = new PolicyProvider(tracker, claudeCfg, openaiCfg);

        assertTrue(
                policy.healthForRouting(Model.CLAUDE) == ModelHealth.DOWN,
                "Claude should be DOWN: last x=5 Claude attempts all failed");
        assertTrue(
                tracker.lastNForModelAll(X, false, Model.CLAUDE),
                "Tracker: last 5 Claude calls should all be failures");
        assertTrue(
                !tracker.lastNForModelAll(Y, true, Model.CLAUDE),
                "Claude should not satisfy y=3 all-success recovery yet");
        assertTrue(
                policy.healthForRouting(Model.OPENAI) == ModelHealth.UNKNOWN,
                "OpenAI still has no attempts → UNKNOWN, not DOWN");
    }

    /**
     * Oldest→newest: five OpenAI failures, then five Claude failures → both models’ x-windows all
     * fail → both DOWN.
     */
    private static void testOpenAiAlsoInFailure() {
        Tracker tracker = new Tracker();
        Model[] models = new Model[10];
        boolean[] ok = new boolean[10];
        for (int i = 0; i < 5; i++) {
            models[i] = Model.OPENAI;
            ok[i] = false;
        }
        for (int i = 5; i < 10; i++) {
            models[i] = Model.CLAUDE;
            ok[i] = false;
        }
        seedTimeline(tracker, models, ok);

        ModelPolicyConfig claudeCfg = new ModelPolicyConfig(X, Y, 5);
        ModelPolicyConfig openaiCfg = new ModelPolicyConfig(X, Y, 5);
        PolicyProvider policy = new PolicyProvider(tracker, claudeCfg, openaiCfg);

        assertTrue(
                policy.healthForRouting(Model.CLAUDE) == ModelHealth.DOWN,
                "Claude DOWN: last 5 Claude calls all failed");
        assertTrue(
                policy.healthForRouting(Model.OPENAI) == ModelHealth.DOWN,
                "OpenAI DOWN: last 5 OpenAI calls all failed");
        assertTrue(tracker.lastNForModelAll(X, false, Model.CLAUDE), "Claude x-window all fail");
        assertTrue(tracker.lastNForModelAll(X, false, Model.OPENAI), "OpenAI x-window all fail");
    }

    private static void seedClaudeOnly(Tracker tracker, boolean[] successes) {
        for (int i = 0; i < successes.length; i++) {
            tracker.addRequest(new Req("hist-" + i, Model.CLAUDE, successes[i]));
        }
    }

    private static void seedTimeline(Tracker tracker, Model[] models, boolean[] successes) {
        if (models.length != successes.length) {
            throw new IllegalArgumentException("models and successes length mismatch");
        }
        for (int i = 0; i < models.length; i++) {
            tracker.addRequest(new Req("hist-" + i, models[i], successes[i]));
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
