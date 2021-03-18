package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.math.BigDecimal;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardEntity {

    private Boolean active;
    private Boolean cancelled;
    private String cardAccountCurrency;
    private String cardAccountDescription;
    private String cardAccountId;
    private String cardAccountNumber;
    private String cardAlias;
    private String cardKey;
    private String cardNumberServiceMBWAY;
    private String depositAccount;
    private String description;
    private String expirationDate;
    private String expirationDateMessage;
    private String maskedCardNumber;
    private Boolean mbNetIndicator;
    private String mobileNumberMBWAY;
    private Integer mobilePrefixMBWAY;
    private Boolean prePaidCard;
    private Boolean prePaidDualCreditCard;
    private String printedName;
    private String serviceIdentifierNumberMBWAY;
    private BigDecimal totalOutstandingBalance;
    private List<CardTransactionEntity> transactions;

    public String getCardAccountId() {
        return cardAccountId;
    }

    public List<CardTransactionEntity> getTransactions() {
        return transactions;
    }

    public String getCardAccountCurrency() {
        return cardAccountCurrency;
    }

    public CreditCardAccount toTinkAccount(
            CardAccountTransactionsEntity balances, CreditBalancesEntity limits) {

        return CreditCardAccount.nxBuilder()
                .withCardDetails(buildCardDetails(balances, limits))
                .withInferredAccountFlags()
                .withId(buildCardAccountId())
                .addHolderName(printedName)
                .setApiIdentifier(cardAccountId)
                .build();
    }

    private CreditCardModule buildCardDetails(
            CardAccountTransactionsEntity balances, CreditBalancesEntity limits) {

        return CreditCardModule.builder()
                .withCardNumber(maskedCardNumber)
                .withBalance(ExactCurrencyAmount.of(balances.getCreditTotal(), cardAccountCurrency))
                .withAvailableCredit(
                        ExactCurrencyAmount.of(limits.getAvailableCredit(), limits.getCurrency()))
                .withCardAlias(ObjectUtils.firstNonNull(cardAlias, cardAccountDescription))
                .build();
    }

    private IdModule buildCardAccountId() {
        return IdModule.builder()
                .withUniqueIdentifier(cardKey)
                .withAccountNumber(cardAccountNumber)
                .withAccountName(cardAccountDescription)
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, maskedCardNumber))
                .build();
    }
}
