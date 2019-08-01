package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TaxAmountEntity {
    @JsonProperty("rate")
    private Float rate = null;

    @JsonProperty("taxableBaseAmount")
    private AmountTypeEntity taxableBaseAmount = null;

    @JsonProperty("totalAmount")
    private AmountTypeEntity totalAmount = null;

    @JsonProperty("details")
    private List<TaxRecordDetails> details = null;

    public Float getRate() {
        return rate;
    }

    public void setRate(Float rate) {
        this.rate = rate;
    }

    public AmountTypeEntity getTaxableBaseAmount() {
        return taxableBaseAmount;
    }

    public void setTaxableBaseAmount(AmountTypeEntity taxableBaseAmount) {
        this.taxableBaseAmount = taxableBaseAmount;
    }

    public AmountTypeEntity getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(AmountTypeEntity totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<TaxRecordDetails> getDetails() {
        return details;
    }

    public void setDetails(List<TaxRecordDetails> details) {
        this.details = details;
    }
}
