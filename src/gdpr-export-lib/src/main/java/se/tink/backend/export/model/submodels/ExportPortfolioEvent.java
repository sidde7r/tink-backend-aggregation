package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportPortfolioEvent implements DefaultSetter {

    private final String timestamp;
    private final String name;
    private final String type;
    private final String totalProfit;
    private final String totalValue;

    public ExportPortfolioEvent(Date timestamp, String name, String type, Double totalProfit,
            Double totalValue) {
        this.timestamp = notNull(timestamp);
        this.name = notNull(name);
        this.type = notNull(type);
        this.totalProfit = notNull(totalProfit);
        this.totalValue = notNull(totalValue);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getTotalProfit() {
        return totalProfit;
    }

    public String getTotalValue() {
        return totalValue;
    }
}
