package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.AsLhvConstants;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.AccountItem;
import se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities.Card;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;

@JsonObject
public class GetUserDataResponse extends BaseResponse {

    @JsonProperty("cards")
    private List<Card> cards;

    @JsonProperty("accounts")
    private List<AccountItem> accounts;

    @JsonIgnore
    public Optional<List<Card>> getCards() {
        return Optional.ofNullable(cards);
    }

    @JsonIgnore
    public Collection<TransactionalAccount> getTransactionalAccounts(
            final String currentUser,
            final String currency,
            final int baseCurrencyId) {
        return accounts.stream()
                .filter(account -> AsLhvConstants.ACCOUNT_TYPE_MAPPER.isTransactionalAccount(account.getType()))
                .map(account -> buildTransactionalAccount(account, baseCurrencyId, currency, currentUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Collection<CreditCardAccount> getCreditCardAccounts(
            final String currentUser,
            final String currency,
            final int baseCurrencyId) {
        return accounts.stream()
                .filter(account -> AsLhvConstants.ACCOUNT_TYPE_MAPPER.isCreditCardAccount(account.getType()))
                .map(account -> buildCreditCardAccount(account, baseCurrencyId, currency, currentUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

//    @JsonIgnore
//    private <T extends Account> Collection<T> getAccounts(
//            final String currentUser,
//            final String currency,
//            final int baseCurrencyId,
//            final Class<T> type) {
//        return accounts.stream()
//                .map(account -> buildAccount(account, baseCurrencyId, currency, currentUser, type))
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toSet());
//    }

    @JsonIgnore
    private Optional<CreditCardAccount> buildCreditCardAccount(
            final AccountItem account,
            final int baseCurrencyId,
            final String currency,
            final String currentUser) {
        double balance = account.getBalance(baseCurrencyId);
        double freeCredit = account.getFreeCredit(baseCurrencyId);
        Optional<String> iban = account.getIban();
        Optional<String> name = account.getName();
        Optional<String> number = account.getNumber();
        Optional<String> portfolioId = account.getPortfolioId();

        // portfolioId is required to fetch transactions.
        if (!iban.isPresent() || !number.isPresent() || !portfolioId.isPresent()) {
            return Optional.empty();
        }

        Amount accountBalance = new Amount(currency, balance);
        Amount availableCredit = new Amount(currency, freeCredit);
        return Optional.of(CreditCardAccount.builder(iban.get(), accountBalance, availableCredit)
                .setBalance(accountBalance)
                .setName(name.isPresent() ? name.get() : iban.get())
                .setHolderName(new HolderName(currentUser))
                .setBankIdentifier(portfolioId.get())
                .setAccountNumber(number.get())
                .build());
    }

    @JsonIgnore
    private Optional<TransactionalAccount> buildTransactionalAccount(
            final AccountItem account,
            final int baseCurrencyId,
            final String currency,
            final String currentUser) {
        double balance = account.getBalance(baseCurrencyId);
        Optional<String> iban = account.getIban();
        Optional<String> name = account.getName();
        Optional<String> number = account.getNumber();
        Optional<String> portfolioId = account.getPortfolioId();

        // portfolioId is required to fetch transactions.
        if (!iban.isPresent() || !number.isPresent() || !portfolioId.isPresent()) {
            return Optional.empty();
        }

        Amount accountBalance = new Amount(currency, balance);
        return Optional.of(TransactionalAccount.builder(account.getType(), iban.get())
                .setBalance(accountBalance)
                .setName(name.isPresent() ? name.get() : iban.get())
                .setHolderName(new HolderName(currentUser))
                .setBankIdentifier(portfolioId.get())
                .setAccountNumber(number.get())
                .build());
    }
}
