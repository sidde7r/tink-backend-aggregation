package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class BoursoramaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>, TransactionFetcher<TransactionalAccount> {

    private static final String CASH_ACCOUNT = "CACC";
    private static final String INSTANT_BALANCE = "XPCD";
    private static final String DEBIT_TRANSACTION_CODE = "DBIT";
    //        CLBD Accounting Balance
    //        XPCD Instant Balance
    //        VALU Value-date balance
    //        OTHR Other Balance

    private final BoursoramaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public BoursoramaTransactionalAccountFetcher(
            BoursoramaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String accessToken = sessionStorage.get(BoursoramaConstants.USER_HASH);

        return apiClient.fetchAccounts(accessToken).getAccounts().stream()
                .filter(a -> a.getCashAccountType().equals(CASH_ACCOUNT))
                .map(account -> map(account, accessToken))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String accessToken = sessionStorage.get(BoursoramaConstants.USER_HASH);

        return apiClient.fetchTransactions(accessToken, account.getApiIdentifier())
                .getTransactions().stream()
                .map(this::mapTransaction)
                .collect(Collectors.toList());
    }

    private Optional<TransactionalAccount> map(AccountEntity a, String accessToken) {
        BalanceAmountEntity balance =
                apiClient.fetchBalances(accessToken, a.getResourceId()).getBalances().stream()
                        .filter(
                                (balanceEntity ->
                                        balanceEntity.getBalanceType().equals(INSTANT_BALANCE)))
                        .findAny()
                        .map(BalanceEntity::getBalanceAmount)
                        .orElseThrow(IllegalStateException::new);

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(balance.getAmount(), balance.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(a.getResourceId())
                                .withAccountNumber(a.getAccountId().getIban())
                                .withAccountName(a.getName())
                                .addIdentifier(
                                        new IbanIdentifier(
                                                a.getBicFi(), a.getAccountId().getIban()))
                                .setProductName(a.getProduct())
                                .build())
                .setApiIdentifier(a.getResourceId())
                .build();
    }

    private AggregationTransaction mapTransaction(TransactionEntity a) {
        return Transaction.builder()
                .setAmount(mapTransactionAmount(a))
                .setDescription(StringUtils.join(a.getRemittanceInformation(), ';'))
                .setDate(a.getBookingDate())
                .setRawDetails(a.getEntryReference())
                .build();
    }

    private ExactCurrencyAmount mapTransactionAmount(TransactionEntity a) {
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        a.getTransactionAmount().getAmount(),
                        a.getTransactionAmount().getCurrency());

        return a.getCreditDebitIndicator().equals(DEBIT_TRANSACTION_CODE)
                ? amount.negate()
                : amount;
    }
}
