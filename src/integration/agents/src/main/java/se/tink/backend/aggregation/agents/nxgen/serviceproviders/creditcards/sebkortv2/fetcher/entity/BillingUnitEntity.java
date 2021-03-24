package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.SebKortConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BillingUnitEntity {
    private String billingUnitId;
    private String billingUnitIdClear;
    private String billingUnitName;
    private String arrangementId;
    private String arrangementNumber;
    private String cutOffDate;
    private String nextInvoiceDate;
    private String creditAmount;
    private String creditAmountNumber;
    private boolean showCreditAmount;
    private String unInvoicedAmount;
    private String balance;
    private List<ContractEntity> contracts;
    private boolean showInvoices;
    private String disposableAmount;
    private String latestPaymentDate;
    private String latestPaymentAmount;
    private int cardType;
    private boolean financeServiceAllowed;
    private boolean salesFinancing;

    public String getBillingUnitId() {
        return billingUnitId;
    }

    public String getBillingUnitIdClear() {
        return billingUnitIdClear;
    }

    public String getBillingUnitName() {
        return billingUnitName;
    }

    public String getArrangementId() {
        return arrangementId;
    }

    public String getArrangementNumber() {
        return arrangementNumber;
    }

    public String getCutOffDate() {
        return cutOffDate;
    }

    public String getNextInvoiceDate() {
        return nextInvoiceDate;
    }

    public String getCreditAmount() {
        return creditAmount;
    }

    public String getCreditAmountNumber() {
        return creditAmountNumber;
    }

    public boolean isShowCreditAmount() {
        return showCreditAmount;
    }

    public String getUnInvoicedAmount() {
        return unInvoicedAmount;
    }

    public String getBalance() {
        return balance;
    }

    public List<ContractEntity> getContracts() {
        return contracts;
    }

    public boolean isShowInvoices() {
        return showInvoices;
    }

    public String getDisposableAmount() {
        return disposableAmount;
    }

    public String getLatestPaymentDate() {
        return latestPaymentDate;
    }

    public String getLatestPaymentAmount() {
        return latestPaymentAmount;
    }

    public int getCardType() {
        return cardType;
    }

    public boolean isFinanceServiceAllowed() {
        return financeServiceAllowed;
    }

    public boolean isSalesFinancing() {
        return salesFinancing;
    }

    @JsonIgnore
    public CreditCardAccount createCreditCardAccount(SebKortConfiguration config, String currency) {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(arrangementNumber)
                                .withBalance(getBalanceOrUnInvoicedAmount(currency))
                                .withAvailableCredit(getAvailableCreditIfPresent(currency))
                                .withCardAlias(billingUnitName)
                                .build())
                .withFlagsFrom(SebKortConstants.PROVIDER_PSD2_FLAG_MAPPER, config.getProviderCode())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(arrangementNumber)
                                .withAccountNumber(arrangementNumber)
                                .withAccountName(billingUnitName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                                arrangementNumber))
                                .build())
                .setApiIdentifier(billingUnitIdClear)
                .build();
    }

    /**
     * Some SEBKort providers, like CircleK does not provide balance. In this case we default to the
     * uninvoiced amount which unfortunately is not the whole truth.
     */
    @JsonIgnore
    private ExactCurrencyAmount getBalanceOrUnInvoicedAmount(String currency) {

        if (Strings.isNullOrEmpty(balance)) {
            return ExactCurrencyAmount.of(
                    BigDecimal.valueOf(AgentParsingUtils.parseAmountTrimCurrency(unInvoicedAmount))
                            .negate(),
                    currency);
        }

        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(AgentParsingUtils.parseAmountTrimCurrency(balance)).negate(),
                currency);
    }

    /**
     * Some SEBKort providers, like CircleK does not provide available credit. In this case we
     * default to 0, as that's what would be stored in the database anyway.
     */
    @JsonIgnore
    private ExactCurrencyAmount getAvailableCreditIfPresent(String currency) {

        if (Strings.isNullOrEmpty(disposableAmount)) {
            return ExactCurrencyAmount.zero(currency);
        }

        return ExactCurrencyAmount.of(
                BigDecimal.valueOf(AgentParsingUtils.parseAmountTrimCurrency(disposableAmount)),
                currency);
    }
}
