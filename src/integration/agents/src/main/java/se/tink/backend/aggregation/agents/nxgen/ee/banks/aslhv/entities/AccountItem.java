package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AccountItem {

    @JsonProperty("active")
    private boolean active;

    @JsonProperty("type")
    private int type;

    @JsonProperty("number")
    private String number;

    @JsonProperty("balance")
    private List<Balance> balances;

    @JsonProperty("iban")
    private String iban;

    @JsonProperty("name")
    private String name;

    @JsonProperty("portfolio_id")
    private String portfolioId;

    @JsonProperty("free_amount")
    private double freeAmount;

    @JsonProperty("free_credit_amount")
    private double freeCreditAmount;

    public boolean isActive() {
        return active;
    }

    @JsonIgnore
    public double getBalance(int baseCurrencyId) {
        return Optional.ofNullable(balances)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(balance -> balance.getCurrencyId() == baseCurrencyId)
                .findFirst()
                .map(Balance::getFreeAmount)
                .orElse(0.0);
    }

    @JsonIgnore
    public double getFreeCredit(int baseCurrencyId) {
        return Optional.ofNullable(balances)
                .map(Collection::stream)
                .orElseGet(Stream::empty)
                .filter(balance -> balance.getCurrencyId() == baseCurrencyId)
                .findFirst()
                .map(Balance::getFreeCreditAmount)
                .orElse(0.0);
    }

    @JsonIgnore
    private Predicate<AccountTypes> isInvalidAccount() {
        // portfolioId is required to fetch transactions.
        return at -> iban == null || number == null || portfolioId == null;
    }

    @JsonIgnore
    private String getAccountName() {
        if (name == null) {
            return iban;
        }
        return name;
    }

    @JsonIgnore
    private String getType() {
        return String.valueOf(type);
    }

    @JsonIgnore
    public Optional<CreditCardAccount> buildCreditCardAccount(
            final int baseCurrencyId, final String currency, final String currentUser) {
        return Optional.of(getType())
                .flatMap(AsLhvConstants.ACCOUNT_TYPE_MAPPER::translate)
                .filter(AccountTypes.CREDIT_CARD::equals)
                .filter(isInvalidAccount())
                .map(
                        accountTypes -> {
                            final Amount accountBalance =
                                    new Amount(currency, getBalance(baseCurrencyId));
                            final Amount availableCredit =
                                    new Amount(currency, getFreeCredit(baseCurrencyId));

                            return CreditCardAccount.builder(iban, accountBalance, availableCredit)
                                    .addIdentifier(new IbanIdentifier(iban))
                                    .setBalance(accountBalance)
                                    .setName(getAccountName())
                                    .setHolderName(new HolderName(currentUser))
                                    .setBankIdentifier(portfolioId)
                                    .setAccountNumber(number)
                                    .build();
                        });
    }

    @JsonIgnore
    public Optional<TransactionalAccount> buildTransactionalAccount(
            final int baseCurrencyId, final String currency, final String currentUser) {
        return Optional.of(getType())
                .flatMap(AsLhvConstants.ACCOUNT_TYPE_MAPPER::translate)
                .filter(TransactionalAccount.ALLOWED_ACCOUNT_TYPES::contains)
                .filter(isInvalidAccount())
                .map(
                        accountType -> {
                            final Amount accountBalance =
                                    new Amount(currency, getBalance(baseCurrencyId));

                            return TransactionalAccount.builder(accountType, iban)
                                    .addIdentifier(new IbanIdentifier(iban))
                                    .setBalance(accountBalance)
                                    .setName(getAccountName())
                                    .setHolderName(new HolderName(currentUser))
                                    .setBankIdentifier(portfolioId)
                                    .setAccountNumber(number)
                                    .build();
                        });
    }
}
