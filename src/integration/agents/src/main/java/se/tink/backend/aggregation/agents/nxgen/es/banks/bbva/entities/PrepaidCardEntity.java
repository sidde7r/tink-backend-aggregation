package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepaidCardEntity extends AbstractContractDetailsEntity {

    private AmountEntity consolidatedBalance;
    private LegacyProductEntity legacyProduct;

    private String migrationType;
    private TypeEntity type;
    private AmountEntity availableBalance;
    private String pan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date stampDate;

    private IndicatorsEntity indicators;
    private AmountEntity operativeLimit;
    private PaymentMethodEntity paymentMethod;
    private StatusEntity status;

    public AmountEntity getConsolidatedBalance() {
        return consolidatedBalance;
    }

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

    public String getPan() {
        return pan;
    }

    public Date getStampDate() {
        return stampDate;
    }

    public IndicatorsEntity getIndicators() {
        return indicators;
    }

    public AmountEntity getOperativeLimit() {
        return operativeLimit;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public StatusEntity getStatus() {
        return status;
    }

    @Override
    protected String getAccountNumber() {
        return null;
    }
}
