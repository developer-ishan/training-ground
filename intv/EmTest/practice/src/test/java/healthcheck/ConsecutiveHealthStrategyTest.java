package healthcheck;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracking.STATUS;

class ConsecutiveHealthStrategyTest {

    private ConsecutiveHealthStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ConsecutiveHealthStrategy(3, 2);
        strategy.onEnteredUp();
    }

    @Test
    void whileUp_successResetsFailureStreak() {
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertFalse(strategy.shouldDropToDown(STATUS.SUCCESS));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertTrue(strategy.shouldDropToDown(STATUS.FAILURE));
    }

    @Test
    void whileDown_failureResetsSuccessStreak() {
        strategy.onEnteredDown();
        assertFalse(strategy.shouldRiseToUp(STATUS.SUCCESS));
        assertFalse(strategy.shouldRiseToUp(STATUS.FAILURE));
        assertFalse(strategy.shouldRiseToUp(STATUS.SUCCESS));
        assertTrue(strategy.shouldRiseToUp(STATUS.SUCCESS));
    }

    @Test
    void onEnteredUp_clearsInternalCounters() {
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        strategy.onEnteredUp();
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
    }
}
