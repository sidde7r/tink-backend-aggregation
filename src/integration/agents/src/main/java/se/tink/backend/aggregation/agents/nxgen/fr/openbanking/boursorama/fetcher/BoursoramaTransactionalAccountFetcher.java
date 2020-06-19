package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static java.util.stream.Collectors.collectingAndThen;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.BoursoramaConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class BoursoramaTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private static final String INSTANT_BALANCE = "XPCD";
    private static final String ACCOUNTING_BALANCE = "CLBD";

    private static final String DEBIT_TRANSACTION_CODE = "DBIT";
    private static final String CASH_ACCOUNT = "CACC";
    private static final String STATUS_BOOKED = "BOOK";

    private final BoursoramaApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final Clock clock;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        String userHash = sessionStorage.get(BoursoramaConstants.USER_HASH);
        return apiClient.fetchAccounts(userHash).getAccounts().stream()
                .filter(a -> a.getCashAccountType().equals(CASH_ACCOUNT))
                .map(account -> map(account, userHash))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        final String userHash = sessionStorage.get(BoursoramaConstants.USER_HASH);

        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDateLocal)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDateLocal) ? oldestDateForFetch : fromDateLocal;

        return apiClient
                .fetchTransactions(
                        userHash, account.getApiIdentifier(), limitedFromDate, toDateLocal)
                .getTransactions().stream()
                .map(this::mapTransaction)
                .collect(collectingAndThen(Collectors.toList(), PaginatorResponseImpl::create));
    }

    private Optional<TransactionalAccount> map(AccountEntity account, String userHash) {
        BalanceAmountEntity balance =
                apiClient.fetchBalances(userHash, account.getResourceId()).getBalances().stream()
                        .filter((this::isAvailableBalance))
                        .findAny()
                        .map(BalanceEntity::getBalanceAmount)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Could not find right type balance for account with id: "
                                                        + account.getResourceId()));

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(
                        BalanceModule.of(
                                ExactCurrencyAmount.of(balance.getAmount(), balance.getCurrency())))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(account.getResourceId())
                                .withAccountNumber(account.getAccountId().getIban())
                                .withAccountName(account.getName())
                                .addIdentifier(
                                        new IbanIdentifier(
                                                account.getBicFi(),
                                                account.getAccountId().getIban()))
                                .setProductName(account.getProduct())
                                .build())
                .setApiIdentifier(account.getResourceId())
                .build();
    }

    private boolean isAvailableBalance(BalanceEntity balanceEntity) {
        return INSTANT_BALANCE.equals(balanceEntity.getBalanceType())
                || ACCOUNTING_BALANCE.equals(balanceEntity.getBalanceType());
    }

    private Transaction mapTransaction(TransactionEntity transaction) {
        return Transaction.builder()
                .setAmount(mapTransactionAmount(transaction))
                .setDescription(StringUtils.join(transaction.getRemittanceInformation(), ';'))
                .setDate(transaction.getBookingDate())
                .setRawDetails(transaction.getEntryReference())
                .setPending(!STATUS_BOOKED.equals(transaction.getStatus()))
                .build();
    }

    private ExactCurrencyAmount mapTransactionAmount(TransactionEntity transaction) {
        ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        transaction.getTransactionAmount().getAmount(),
                        transaction.getTransactionAmount().getCurrency());

        return DEBIT_TRANSACTION_CODE.equals(transaction.getCreditDebitIndicator())
                ? amount.negate()
                : amount;
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return LocalDate.now(clock).minusDays(89);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return date.toInstant().atZone(clock.getZone()).toLocalDate();
    }
}
