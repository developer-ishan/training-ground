package context;

import healthcheck.HealthCheckStrategy;
import tracking.STATUS;

public interface State {
    boolean up();

    void onResponse(ModelContext ctx, HealthCheckStrategy strategy, STATUS outcome);
}
