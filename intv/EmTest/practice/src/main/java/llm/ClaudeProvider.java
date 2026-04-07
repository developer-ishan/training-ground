package llm;

import healthcheck.HealthCheckStrategy;

public final class ClaudeProvider extends LLMProvider {

    public ClaudeProvider(HealthCheckStrategy healthCheckStrategy) {
        super(ModelName.CLAUDE, healthCheckStrategy);
    }

    @Override
    public String askPrompt(String prompt) throws RuntimeException {
        if (!flag) {
            throw new RuntimeException("Claude is not available");
        }
        return "Claude says success: " + prompt;
    }
}
