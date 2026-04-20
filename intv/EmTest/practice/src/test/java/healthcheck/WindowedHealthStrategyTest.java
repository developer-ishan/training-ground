package healthcheck;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracking.STATUS;

class WindowedHealthStrategyTest {

    private WindowedHealthStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WindowedHealthStrategy(4, 0.5, 0.75);
        strategy.onEnteredUp();
    }

    @Test
    void whileUp_windowNotFull_neverDrops() {
        assertFalse(strategy.shouldDropToDown(STATUS.SUCCESS));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
        assertFalse(strategy.shouldDropToDown(STATUS.FAILURE));
    }

    @Test
    void whileUp_fullWindow_belowHalfSuccess_triggersDown() {
        strategy.shouldDropToDown(STATUS.FAILURE);
        strategy.shouldDropToDown(STATUS.SUCCESS);
        strategy.shouldDropToDown(STATUS.FAILURE);
        assertTrue(strategy.shouldDropToDown(STATUS.FAILURE));
    }

    @Test
    void whileDown_fullWindow_highEnoughSuccess_triggersUp() {
        strategy.onEnteredDown();
        assertFalse(strategy.shouldRiseToUp(STATUS.SUCCESS));
        assertFalse(strategy.shouldRiseToUp(STATUS.SUCCESS));
        assertFalse(strategy.shouldRiseToUp(STATUS.SUCCESS));
        assertTrue(strategy.shouldRiseToUp(STATUS.SUCCESS));
    }
}
