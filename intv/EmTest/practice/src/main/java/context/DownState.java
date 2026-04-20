package context;

import healthcheck.HealthCheckStrategy;
import tracking.STATUS;

final class DownState implements State {
    @Override
    public boolean up() {
        return false;
    }

    @Override
    public void onResponse(ModelContext ctx, HealthCheckStrategy strategy, STATUS outcome) {
        if (strategy.shouldRiseToUp(outcome)) {
            ctx.setState(new UpState());
            strategy.onEnteredUp();
        }
    }
}
