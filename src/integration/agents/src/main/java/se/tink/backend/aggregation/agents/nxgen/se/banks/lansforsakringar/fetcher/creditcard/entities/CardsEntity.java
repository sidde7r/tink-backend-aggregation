package se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.MoreObjects;
import java.math.BigDecimal;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.banks.lansforsakringar.LansforsakringarConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CardsEntity {
    private String cardName;
    private BigDecimal balance;
    private String cardNumber;
    private String status;
    private String cardType;
    private String connectedAccountNumber;
    private BigDecimal cardLimit;
    private BigDecimal cardAvailable;
    private String expires;
    private double reservedAmount;
    private double aviAmount;
    private String versionNumber;
    private String statusText;
    private boolean replaced;
    private CardAccountDetailsEntity cardAccountDetails;
    private String embossedName;
    private String issuingNetwork;
    private String productName;
    private boolean enriched;
    private boolean accountDetailsAvailable;

    @JsonIgnore
    public boolean isCredit() {
        return Accounts.ACCOUNT_TYPE_MAPPER.isOf(cardType, AccountTypes.CREDIT_CARD);
    }

    @JsonIgnore
    public CreditCardAccount toTinkAccount() {
        return CreditCardAccount.nxBuilder()
                .withCardDetails(getCardDetails())
                .withoutFlags()
                .withId(getIdModule())
                .addHolderName(embossedName)
                .setApiIdentifier(cardNumber)
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(cardNumber)
                .withAccountNumber(cardNumber)
                .withAccountName(getAccountName())
                .addIdentifier(
                        AccountIdentifier.create(
                                AccountIdentifierType.PAYMENT_CARD_NUMBER, cardNumber))
                .build();
    }

    private String getAccountName() {
        if (accountDetailsAvailable && cardAccountDetails != null) {
            return cardAccountDetails.getAccountName();
        } else {
            return productName;
        }
    }

    private CreditCardModule getCardDetails() {
        return CreditCardModule.builder()
                .withCardNumber(cardNumber)
                .withBalance(ExactCurrencyAmount.of(balance.negate(), Accounts.CURRENCY))
                .withAvailableCredit(ExactCurrencyAmount.of(cardAvailable, Accounts.CURRENCY))
                .withCardAlias(cardName)
                .build();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this.getClass())
                .add("cardName", cardName)
                .add("balance", balance)
                .add("cardNumber", cardNumber)
                .add("status", status)
                .add("cardType", cardType)
                .add("ConnectedAccountNumber", connectedAccountNumber)
                .add("cardLimit", cardLimit)
                .add("cardAvailable", cardAvailable)
                .add("expires", expires)
                .add("reservedAmount", reservedAmount)
                .add("aviAmount", aviAmount)
                .add("versionNumber", versionNumber)
                .add("statusText", statusText)
                .add("replaced", replaced)
                .add("cardAccountDetails", cardAccountDetails)
                .toString();
    }
}
