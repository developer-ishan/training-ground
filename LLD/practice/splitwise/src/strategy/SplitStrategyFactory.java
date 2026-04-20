package src.strategy;

import java.util.EnumMap;
import java.util.Map;

import src.models.enums.SplitType;

public class SplitStrategyFactory {
    private final Map<SplitType, SplitStrategy> strategies;

    public SplitStrategyFactory() {
        strategies = new EnumMap<>(SplitType.class);
        strategies.put(SplitType.EQUAL,      new EqualSplitStrategy());
        strategies.put(SplitType.EXACT,      new ExactSplitStrategy());
        strategies.put(SplitType.PERCENTAGE, new PercentSplitStrategy());
    }

    public SplitStrategy getStrategy(SplitType type) {
        SplitStrategy s = strategies.get(type);
        if (s == null) throw new IllegalArgumentException("No strategy for split type: " + type);
        return s;
    }
}
