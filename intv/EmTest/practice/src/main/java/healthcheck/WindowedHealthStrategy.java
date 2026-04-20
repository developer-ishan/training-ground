package healthcheck;

import java.util.ArrayDeque;
import java.util.Deque;
import tracking.STATUS;

public final class WindowedHealthStrategy implements HealthCheckStrategy {
    private final int windowSize;
    private final double minSuccessFractionWhileUp;
    private final double minSuccessFractionWhileDown;
    private final Deque<STATUS> window = new ArrayDeque<>();

    public WindowedHealthStrategy(
            int windowSize,
            double minSuccessFractionWhileUp,
            double minSuccessFractionWhileDown) {
        if (windowSize < 1) {
            throw new IllegalArgumentException("windowSize must be >= 1");
        }
        if (minSuccessFractionWhileUp < 0 || minSuccessFractionWhileUp > 1
                || minSuccessFractionWhileDown < 0 || minSuccessFractionWhileDown > 1) {
            throw new IllegalArgumentException("fractions must be in [0, 1]");
        }
        this.windowSize = windowSize;
        this.minSuccessFractionWhileUp = minSuccessFractionWhileUp;
        this.minSuccessFractionWhileDown = minSuccessFractionWhileDown;
    }

    @Override
    public boolean shouldDropToDown(STATUS outcome) {
        pushTrim(outcome);
        if (window.size() < windowSize) {
            return false;
        }
        double rate = successRate();
        return rate < minSuccessFractionWhileUp;
    }

    @Override
    public boolean shouldRiseToUp(STATUS outcome) {
        pushTrim(outcome);
        if (window.size() < windowSize) {
            return false;
        }
        double rate = successRate();
        return rate >= minSuccessFractionWhileDown;
    }

    @Override
    public void onEnteredUp() {
        window.clear();
    }

    @Override
    public void onEnteredDown() {
        window.clear();
    }

    private void pushTrim(STATUS status) {
        window.addLast(status);
        while (window.size() > windowSize) {
            window.removeFirst();
        }
    }

    private double successRate() {
        int ok = 0;
        for (STATUS s : window) {
            if (s == STATUS.SUCCESS) {
                ok++;
            }
        }
        return ok / (double) window.size();
    }
}
