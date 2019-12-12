package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebitCardEntity extends AbstractContractDetailsEntity {

    private LegacyProductEntity legacyProduct;

    private String migrationType;
    private TypeEntity type;
    private IndicatorsEntity indicators;
    private AmountEntity availableBalance;
    private PaymentMethodEntity paymentMethod;
    private String pan;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date stampDate;

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

    public IndicatorsEntity getIndicators() {
        return indicators;
    }

    public AmountEntity getAvailableBalance() {
        return availableBalance;
    }

    public PaymentMethodEntity getPaymentMethod() {
        return paymentMethod;
    }

    public String getPan() {
        return pan;
    }

    public Date getStampDate() {
        return stampDate;
    }

    public StatusEntity getStatus() {
        return status;
    }
}
