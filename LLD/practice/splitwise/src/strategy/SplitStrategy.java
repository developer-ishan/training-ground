package src.strategy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import src.models.*;

public interface SplitStrategy {
    Map<User, BigDecimal> split(
        BigDecimal amount,
        List<User> participants,
        Map<User, BigDecimal> splitInput  // null/empty for EQUAL; exact amounts for EXACT; percentages for PERCENTAGE
    );
}
