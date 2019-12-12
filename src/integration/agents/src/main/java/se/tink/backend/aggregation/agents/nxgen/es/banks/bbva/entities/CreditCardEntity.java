package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

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

    @JsonIgnore
    public CreditCardAccount toTinkCreditCard() {
        String uniqueId = createUniqueIdFromName();
        return CreditCardAccount.builder(uniqueId)
                // Using as number the ID created in previous step as that's how it's shown in the
                // app
                .setAccountNumber(getMaskedPan().orElse(uniqueId))
                .putInTemporaryStorage(BbvaConstants.StorageKeys.ACCOUNT_ID, getId())
                .setExactBalance(availableBalance.toTinkAmount())
                .setName(getProduct().getDescription())
                .build();
    }

    private String createUniqueIdFromName() {
        return getPanLast4Digits()
                .orElseThrow(() -> new NoSuchElementException("can't determine the card number"));
    }

    @JsonIgnore
    private Optional<String> getMaskedPan() {
        return getPanLast4Digits().map(pan -> "************" + pan);
    }

    @JsonIgnore
    private Optional<String> getPanLast4Digits() {
        return Optional.ofNullable(pan)
                .filter(pan -> pan.length() >= 4)
                .map(pan -> pan.substring(pan.length() - 4));
    }
}
