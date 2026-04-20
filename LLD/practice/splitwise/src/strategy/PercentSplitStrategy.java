package src.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.models.User;

class PercentSplitStrategy implements SplitStrategy {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Override
    public Map<User, BigDecimal> split(BigDecimal amount, List<User> participants, Map<User, BigDecimal> splitInput) {
        if (splitInput == null || splitInput.isEmpty()) {
            throw new IllegalArgumentException("Percentage values must be provided");
        }
        BigDecimal totalPct = splitInput.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalPct.compareTo(HUNDRED) != 0) {
            throw new IllegalArgumentException("Percentages must sum to 100, got " + totalPct);
        }
        Map<User, BigDecimal> result = new HashMap<>();
        for (Map.Entry<User, BigDecimal> entry : splitInput.entrySet()) {
            BigDecimal share = amount.multiply(entry.getValue())
                                     .divide(HUNDRED, 2, RoundingMode.HALF_UP);
            result.put(entry.getKey(), share);
        }
        return result;
    }
}
