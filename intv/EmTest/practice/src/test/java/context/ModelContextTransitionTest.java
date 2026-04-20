package context;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import healthcheck.ConsecutiveHealthStrategy;
import org.junit.jupiter.api.Test;
import tracking.STATUS;

class ModelContextTransitionTest {

    @Test
    void consecutivePolicy_togglesUpAndDown() {
        ConsecutiveHealthStrategy strategy = new ConsecutiveHealthStrategy(2, 2);
        ModelContext ctx = new ModelContext(strategy);

        assertTrue(ctx.isUp());

        ctx.onResponse(STATUS.FAILURE);
        ctx.onResponse(STATUS.FAILURE);
        assertFalse(ctx.isUp());

        ctx.onResponse(STATUS.SUCCESS);
        ctx.onResponse(STATUS.SUCCESS);
        assertTrue(ctx.isUp());
    }

    @Test
    void successWhileUp_resetsFailureCountTowardDown() {
        ConsecutiveHealthStrategy strategy = new ConsecutiveHealthStrategy(3, 1);
        ModelContext ctx = new ModelContext(strategy);

        ctx.onResponse(STATUS.FAILURE);
        ctx.onResponse(STATUS.FAILURE);
        ctx.onResponse(STATUS.SUCCESS);
        ctx.onResponse(STATUS.FAILURE);
        ctx.onResponse(STATUS.FAILURE);
        assertTrue(ctx.isUp());
        ctx.onResponse(STATUS.FAILURE);
        assertFalse(ctx.isUp());
    }
}
