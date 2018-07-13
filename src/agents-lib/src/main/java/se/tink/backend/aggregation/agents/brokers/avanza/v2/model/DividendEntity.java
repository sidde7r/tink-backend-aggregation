package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DividendEntity {
    private String currency;
    private String exDate;
    private double amountPerShare;
    private String paymentDate;

    public String getCurrency() {
        return currency;
    }

    public String getExDate() {
        return exDate;
    }

    public double getAmountPerShare() {
        return amountPerShare;
    }

    public String getPaymentDate() {
        return paymentDate;
    }
}
