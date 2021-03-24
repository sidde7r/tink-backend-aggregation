package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonProperty("account_type")
    private String accountType;

    @JsonProperty("closed_date")
    private String closedDate;

    @JsonProperty("accrued_interest_credit")
    private String accruedInterestCredit;

    @JsonProperty("opened_date")
    private String openedDate;

    private BigDecimal balance;

    @JsonProperty("tax_account")
    private String taxAccount;

    @JsonProperty("customer_properties")
    private AccountCustomerProperties customerProperties;

    @JsonProperty("account_name")
    private String accountName;

    @JsonProperty("interest_rate")
    private Number interestRate;

    @JsonProperty("co_owned_account")
    private Boolean coOwnedAccount;

    private String currency;

    private String status;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(String customerName) {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withPaymentAccountFlag()
                .withBalance(BalanceModule.of(getAvailableBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(accountNumber)
                                .withAccountName(accountName)
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.SE,
                                                accountNumber,
                                                customerName))
                                .build())
                .addHolderName(customerName)
                .setApiIdentifier(accountNumber)
                .setBankIdentifier(accountNumber)
                .putInTemporaryStorage(SbabConstants.StorageKeys.ACCOUNT_NUMBER, accountNumber)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(balance, currency);
    }
}
