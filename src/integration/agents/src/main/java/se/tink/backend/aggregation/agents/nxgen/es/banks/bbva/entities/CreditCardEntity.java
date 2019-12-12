package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardEntity extends AbstractContractDetailsEntity {

    private LegacyProductEntity legacyProduct;

    private String migrationType;
    private TypeEntity type;
    private AmountEntity availableBalance;
    private AmountEntity limit;
    private String pan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date stampDate;

    private AmountEntity disposedAmount;
    private IndicatorsEntity indicators;
    private PaymentMethodEntity paymentMethod;
    private StatusEntity status;

    public LegacyProductEntity getLegacyProduct() {
        return legacyProduct;
    }

    public String getMigrationType() {
        return migrationType;
    }

    public TypeEntity getType() {
        return type;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public AmountEntity getLimit() {
        return limit;
    }

    public String getPan() {
        return pan;
    }

    public Date getStampDate() {
        return stampDate;
    }

    public AmountEntity getDisposedAmount() {
        return disposedAmount;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
