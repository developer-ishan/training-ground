package src.strategy;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.models.User;

class ExactSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, BigDecimal> split(BigDecimal amount, List<User> participants, Map<User, BigDecimal> splitInput) {
        if (splitInput == null || splitInput.isEmpty()) {
            throw new IllegalArgumentException("Exact amounts must be provided");
        }
        BigDecimal total = splitInput.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        if (total.compareTo(amount) != 0) {
            throw new IllegalArgumentException("Exact amounts must sum to total: expected " + amount + ", got " + total);
        }
        return new HashMap<>(splitInput);
    }
}
