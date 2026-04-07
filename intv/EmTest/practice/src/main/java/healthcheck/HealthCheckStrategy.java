package healthcheck;

import tracking.STATUS;

public interface HealthCheckStrategy {

    boolean shouldDropToDown(STATUS outcome);

    boolean shouldRiseToUp(STATUS outcome);

    void onEnteredUp();

    void onEnteredDown();
}
