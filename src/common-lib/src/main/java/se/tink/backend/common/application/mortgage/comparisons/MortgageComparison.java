package se.tink.backend.common.application.mortgage.comparisons;

import java.util.List;

public class MortgageComparison {
    private String description;
    private List<MortgageComparisonProvider> providers;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<MortgageComparisonProvider> getProviders() {
        return providers;
    }

    public void setProviders(List<MortgageComparisonProvider> providers) {
        this.providers = providers;
    }
}
