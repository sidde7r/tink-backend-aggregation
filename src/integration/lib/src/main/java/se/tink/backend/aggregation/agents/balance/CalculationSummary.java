package se.tink.backend.aggregation.agents.balance;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class CalculationSummary {

    private final String title;
    private final List<String> steps = new ArrayList<>();

    private CalculationSummary(String title) {
        this.title = title;
    }

    public static CalculationSummary of(String title) {
        return new CalculationSummary(title);
    }

    public CalculationSummary addStepDescription(String description) {
        steps.add(description);
        return this;
    }
}
