package context;

import healthcheck.HealthCheckStrategy;
import tracking.STATUS;

final class UpState implements State {
    @Override
    public boolean up() {
        return true;
    }

    @Override
    public void onResponse(ModelContext ctx, HealthCheckStrategy strategy, STATUS outcome) {
        if (strategy.shouldDropToDown(outcome)) {
            ctx.setState(new DownState());
            strategy.onEnteredDown();
        }
    }
}
