package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

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
        double result = freeAmount;
        if (balances != null) {
            for (Balance balance : balances) {
                if (balance.getCurrencyId() == baseCurrencyId) {
                    result = balance.getFreeAmount();
                }
            }
        }
        return result;
    }

    @JsonIgnore
    public double getFreeCredit(int baseCurrencyId) {
        double result = freeCreditAmount;
        if (balances != null) {
            for (Balance balance : balances) {
                if (balance.getCurrencyId() == baseCurrencyId) {
                    result = balance.getFreeCreditAmount();
                }
            }
        }
        return result;
    }

    @JsonIgnore
    private boolean isInvalidAccount() {
        // portfolioId is required to fetch transactions.
        return !AsLhvConstants.ACCOUNT_TYPE_MAPPER.translate(type).isPresent() ||
                iban == null ||
                number == null ||
                portfolioId == null;
    }

    @JsonIgnore
    private String getAccountName() {
        if (name == null) {
            return iban;
        }
        return name;
    }

    @JsonIgnore
    public Optional<CreditCardAccount> buildCreditCardAccount(
            final int baseCurrencyId,
            final String currency,
            final String currentUser) {
        if (isInvalidAccount()) {
            return Optional.empty();
        }

        if (!AsLhvConstants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(type)) {
            return Optional.empty();
        }

        double balance = getBalance(baseCurrencyId);
        double freeCredit = getFreeCredit(baseCurrencyId);
        Amount accountBalance = new Amount(currency, balance);
        Amount availableCredit = new Amount(currency, freeCredit);
        return Optional.of(CreditCardAccount.builder(iban, accountBalance, availableCredit)
                .addIdentifier(new IbanIdentifier(iban))
                .setBalance(accountBalance)
                .setName(getAccountName())
                .setHolderName(new HolderName(currentUser))
                .setBankIdentifier(portfolioId)
                .setAccountNumber(number)
                .build());
    }

    @JsonIgnore
    public Optional<TransactionalAccount> buildTransactionalAccount(
            final int baseCurrencyId,
            final String currency,
            final String currentUser) {
        if (isInvalidAccount()) {
            return Optional.empty();
        }

        if (!AsLhvConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(type)) {
            return Optional.empty();
        }

        AccountTypes accountType = AsLhvConstants.ACCOUNT_TYPE_MAPPER.translate(type).get();
        double balance = getBalance(baseCurrencyId);
        Amount accountBalance = new Amount(currency, balance);
        return Optional.of(TransactionalAccount.builder(accountType, iban)
                .addIdentifier(new IbanIdentifier(iban))
                .setBalance(accountBalance)
                .setName(getAccountName())
                .setHolderName(new HolderName(currentUser))
                .setBankIdentifier(portfolioId)
                .setAccountNumber(number)
                .build());
    }
}
