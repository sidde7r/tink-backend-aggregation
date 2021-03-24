package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
        String accountNumber = getAccountNumber();
        String accountName = getAccountName();
        String uniqueId = getPanLast4Digits();
        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(accountNumber)
                                .withBalance(getBalance())
                                .withAvailableCredit(
                                        availableBalance == null
                                                ? ExactCurrencyAmount.inEUR(0.00)
                                                : availableBalance.toTinkAmount())
                                .withCardAlias(accountName)
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(uniqueId)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                accountNumber))
                                .build())
                .setApiIdentifier(getId())
                .build();
    }

    private ExactCurrencyAmount getBalance() {
        // this allows taking into account pending transactions
        return availableBalance == null
                ? ExactCurrencyAmount.inEUR(0.00)
                : availableBalance.toTinkAmount().subtract(limit.toTinkAmount());
    }

    @JsonIgnore
    @Override
    protected String getAccountNumber() {
        return "************" + getPanLast4Digits();
    }

    @JsonIgnore
    private String getPanLast4Digits() {
        return Optional.ofNullable(pan)
                .filter(pan -> pan.length() >= 4)
                .map(pan -> pan.substring(pan.length() - 4))
                .orElseThrow(() -> new NoSuchElementException("can't determine the card number"));
    }

    public boolean isNotComplementaryCard() {
        // BBVA has complementary credit cards those are linked to another credit card but have a
        // credit limit of 1;
        return !BbvaConstants.ProductTypes.COMPLEMENTARY.equalsIgnoreCase(getProduct().getName());
    }
}
