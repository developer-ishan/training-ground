package healthcheck;

import tracking.STATUS;

public final class ConsecutiveHealthStrategy implements HealthCheckStrategy {
    private final int consecutiveFailuresBeforeDown;
    private final int consecutiveSuccessesBeforeUp;
    private int failuresWhileUp;
    private int successesWhileDown;

    public ConsecutiveHealthStrategy(int consecutiveFailuresBeforeDown, int consecutiveSuccessesBeforeUp) {
        this.consecutiveFailuresBeforeDown = consecutiveFailuresBeforeDown;
        this.consecutiveSuccessesBeforeUp = consecutiveSuccessesBeforeUp;
    }

    @Override
    public boolean shouldDropToDown(STATUS outcome) {
        if (outcome == STATUS.SUCCESS) {
            failuresWhileUp = 0;
            return false;
        }
        failuresWhileUp++;
        return failuresWhileUp >= consecutiveFailuresBeforeDown;
    }

    @Override
    public boolean shouldRiseToUp(STATUS outcome) {
        if (outcome == STATUS.FAILURE) {
            successesWhileDown = 0;
            return false;
        }
        successesWhileDown++;
        return successesWhileDown >= consecutiveSuccessesBeforeUp;
    }

    @Override
    public void onEnteredUp() {
        failuresWhileUp = 0;
        successesWhileDown = 0;
    }

    @Override
    public void onEnteredDown() {
        failuresWhileUp = 0;
        successesWhileDown = 0;
    }
}
