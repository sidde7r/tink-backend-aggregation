package se.tink.backend.export.model.submodels;

public class ExportPortfolio {

    private final String type;
    private final String rawType;
    private final Double totalProfit;
    private final Double totalValue;

    public ExportPortfolio(String type, String rawType, Double totalProfit, Double totalValue) {
        this.type = type;
        this.rawType = rawType;
        this.totalProfit = totalProfit;
        this.totalValue = totalValue;
    }

    public String getType() {
        return type;
    }

    public String getRawType() {
        return rawType;
    }

    public Double getTotalProfit() {
        return totalProfit;
    }

    public Double getTotalValue() {
        return totalValue;
    }
}
