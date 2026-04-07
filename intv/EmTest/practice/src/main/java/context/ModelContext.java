package context;

import healthcheck.HealthCheckStrategy;
import tracking.STATUS;

public final class ModelContext {
    private final HealthCheckStrategy healthCheckStrategy;
    private State state;

    public ModelContext(HealthCheckStrategy healthCheckStrategy) {
        this.healthCheckStrategy = healthCheckStrategy;
        this.state = new UpState();
        healthCheckStrategy.onEnteredUp();
    }

    public HealthCheckStrategy healthCheckStrategy() {
        return healthCheckStrategy;
    }

    public State state() {
        return state;
    }

    public boolean isUp() {
        return state.up();
    }

    public void onResponse(STATUS status) {
        state.onResponse(this, healthCheckStrategy, status);
    }

    public void setState(State state) {
        this.state = state;
    }
}
