package src.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import src.models.User;

class EqualSplitStrategy implements SplitStrategy {
    @Override
    public Map<User, BigDecimal> split(BigDecimal amount, List<User> participants, Map<User, BigDecimal> splitInput) {
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participants list cannot be empty");
        }
        int n = participants.size();
        BigDecimal share = amount.divide(BigDecimal.valueOf(n), 2, RoundingMode.HALF_UP);
        BigDecimal remainder = amount.subtract(share.multiply(BigDecimal.valueOf(n)));

        Map<User, BigDecimal> result = new HashMap<>();
        for (int i = 0; i < n; i++) {
            // distribute remainder (in cents) to first participants
            BigDecimal userShare = (i == 0) ? share.add(remainder) : share;
            result.put(participants.get(i), userShare);
        }
        return result;
    }
}
