package llm;

import context.ModelContext;
import healthcheck.HealthCheckStrategy;

public abstract class LLMProvider {
    private final ModelName modelName;
    protected final ModelContext modelContext;

    public boolean flag = true;

    protected LLMProvider(ModelName modelName, HealthCheckStrategy healthCheckStrategy) {
        this.modelName = modelName;
        this.modelContext = new ModelContext(healthCheckStrategy);
    }

    public ModelName modelName() {
        return modelName;
    }

    public ModelContext modelContext() {
        return modelContext;
    }

    public abstract String askPrompt(String prompt) throws RuntimeException;
}
