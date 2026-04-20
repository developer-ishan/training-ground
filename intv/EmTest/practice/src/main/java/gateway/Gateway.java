package gateway;

import java.util.Random;
import llm.LLMProvider;
import tracking.STATUS;

public final class Gateway {
    private final LLMProvider claude;
    private final LLMProvider openAi;
    private final Random random;

    public Gateway(LLMProvider claude, LLMProvider openAi) {
        this(claude, openAi, new Random());
    }

    public Gateway(LLMProvider claude, LLMProvider openAi, Random random) {
        this.claude = claude;
        this.openAi = openAi;
        this.random = random;
    }

    public String askPrompt(String prompt) {
        boolean claudeUp = claude.modelContext().isUp();
        boolean openAiUp = openAi.modelContext().isUp();

        if (claudeUp) {
            return dispatch(claude, prompt);
        }
        int d = random.nextInt(100);
        if (openAiUp) {
            if (d < 5) {
                return dispatch(claude, prompt);
            }
            return dispatch(openAi, prompt);
        }
        if (d < 5) {
            return dispatch(claude, prompt);
        }
        if (d < 10) {
            return dispatch(openAi, prompt);
        }
        throw new GatewayRejectedException("request dropped: both models down");
    }

    private String dispatch(LLMProvider provider, String prompt) {
        try {
            String out = provider.askPrompt(prompt);
            provider.modelContext().onResponse(STATUS.SUCCESS);
            return out;
        } catch (RuntimeException e) {
            provider.modelContext().onResponse(STATUS.FAILURE);
            throw e;
        }
    }
}
