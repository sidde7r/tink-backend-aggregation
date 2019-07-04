package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.AccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.EnterCardConstants.StorageKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountEntity {

    private String customerID;
    private String accountNumber;
    private String accountType;
    private String timestamp;
    private String accountStatus;
    private Number otb;
    private Number creditLimit;
    private Number mtp;
    private Boolean eFaktura;
    private Boolean autogiro;
    private String productName;
    private String productDescription;
    private String country;
    private String currency;
    private String transactionPurposeList;
    private Number originalBalance;
    private Number balance;
    private Number terms;
    private Number interestBearingBalance;
    private Number nonInterestBearingBalance;
    private Number nominalInterestRate;
    private Number installmentCharges;
    private String installmentChargePeriod;
    private Number coApplicant;
    private Number reservedAmount;
    private Number balanceOnCardLoan;
    private Number bankID;
    private List<CardDetailsEntity> cardDetails;

    @JsonProperty("_links")
    private LinksEntity links;

    public boolean isCreditCardAccount() {
        return accountType.equalsIgnoreCase(AccountType.CREDIT_CARD);
    }

    public CreditCardAccount toCreditCardAccount() {
        return CreditCardAccount.builder(accountNumber)
                .setAccountNumber(accountNumber)
                .setBankIdentifier(String.valueOf(bankID))
                .setName(getName())
                .setBalance(new Amount(currency, balance))
                .putInTemporaryStorage(StorageKeys.ACCOUNT_NUMBER, accountNumber)
                .build();
    }

    public String getName() {
        return cardDetails.stream()
                .findFirst()
                .map(CardDetailsEntity::getCardHolderName)
                .orElse("");
    }
}
