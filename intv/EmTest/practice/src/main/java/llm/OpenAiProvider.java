package llm;

import healthcheck.HealthCheckStrategy;

public final class OpenAiProvider extends LLMProvider {

    public OpenAiProvider(HealthCheckStrategy healthCheckStrategy) {
        super(ModelName.OPENAI, healthCheckStrategy);
    }

    @Override
    public String askPrompt(String prompt) {
        if (!flag) {
            throw new RuntimeException("GPT is not available");
        }
        return "GPT says success: " + prompt;
    }
}
