package se.tink.backend.export.model.submodels;

import java.util.Date;
import se.tink.backend.export.helper.DefaultSetter;

public class ExportInstrumentEvent implements DefaultSetter {

    private final String timestamp;
    private final String name;
    private final String averageAcquisitionPrice;
    private final String marketValue;
    private final String profit;
    private final String quantity;

    public ExportInstrumentEvent(Date timestamp, String name, Double averageAcquisitionPrice,
            Double marketValue, Double profit, Double quantity) {
        this.timestamp = notNull(timestamp);
        this.name = notNull(name);
        this.averageAcquisitionPrice = notNull(averageAcquisitionPrice);
        this.marketValue = notNull(marketValue);
        this.profit = notNull(profit);
        this.quantity = notNull(quantity);
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return name;
    }

    public String getAverageAcquisitionPrice() {
        return averageAcquisitionPrice;
    }

    public String getMarketValue() {
        return marketValue;
    }

    public String getProfit() {
        return profit;
    }

    public String getQuantity() {
        return quantity;
    }
}
