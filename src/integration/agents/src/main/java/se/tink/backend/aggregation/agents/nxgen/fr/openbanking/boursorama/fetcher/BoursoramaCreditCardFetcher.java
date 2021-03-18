package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher;

import static java.util.stream.Collectors.collectingAndThen;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceAmountEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.CashAccountType;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.creditcard.CreditCardModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RequiredArgsConstructor
public class BoursoramaCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionDatePaginator<CreditCardAccount> {

    private static final long MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA = 89L;

    private static final String STATUS_BOOKED = "BOOK";

    private final BoursoramaApiClient boursoramaApiClient;
    private final LocalDateTimeSource localDateTimeSource;

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        return Optional.ofNullable(boursoramaApiClient.fetchAccounts().getAccounts())
                .orElse(Collections.emptyList()).stream()
                .filter(this::isCreditCards)
                .map(this::mapToTinkCreditCardAccount)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            CreditCardAccount account, Date fromDate, Date toDate) {

        final LocalDate fromDateLocal = getLocalDateFromDate(fromDate);
        final LocalDate toDateLocal = getLocalDateFromDate(toDate);

        final LocalDate oldestDateForFetch = getOldestDateForTransactionFetch();

        if (oldestDateForFetch.isAfter(toDateLocal)) {
            return PaginatorResponseImpl.createEmpty(false);
        }

        final LocalDate limitedFromDate =
                oldestDateForFetch.isAfter(fromDateLocal) ? oldestDateForFetch : fromDateLocal;

        return boursoramaApiClient
                .fetchTransactions(account.getApiIdentifier(), limitedFromDate, toDateLocal)
                .getTransactions().stream()
                .map(this::mapTransaction)
                .collect(collectingAndThen(Collectors.toList(), PaginatorResponseImpl::create));
    }

    private CreditCardAccount mapToTinkCreditCardAccount(AccountEntity accountEntity) {
        BalanceAmountEntity balance =
                boursoramaApiClient.fetchBalances(accountEntity.getResourceId()).getBalances()
                        .stream()
                        .findAny()
                        .map(BalanceEntity::getBalanceAmount)
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Could not find right type balance for account with id: "
                                                        + accountEntity.getResourceId()));

        return CreditCardAccount.nxBuilder()
                .withCardDetails(
                        CreditCardModule.builder()
                                .withCardNumber(
                                        accountEntity.getAccountId().getOther().getIdentification())
                                .withBalance(
                                        ExactCurrencyAmount.of(
                                                balance.getAmount(), balance.getCurrency()))
                                .withAvailableCredit(ExactCurrencyAmount.of(BigDecimal.ZERO, "EUR"))
                                .withCardAlias(accountEntity.getProduct())
                                .build())
                .withInferredAccountFlags()
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountEntity.getResourceId())
                                .withAccountNumber(accountEntity.getLinkedAccount())
                                .withAccountName(accountEntity.getName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK,
                                                accountEntity
                                                        .getAccountId()
                                                        .getOther()
                                                        .getIdentification()))
                                .setProductName(accountEntity.getProduct())
                                .build())
                .setApiIdentifier(accountEntity.getResourceId())
                .build();
    }

    private Transaction mapTransaction(TransactionEntity transaction) {
        return Transaction.builder()
                .setAmount(
                        ExactCurrencyAmount.of(
                                transaction.getTransactionAmount().getAmount(),
                                transaction.getTransactionAmount().getCurrency()))
                .setDescription(StringUtils.join(transaction.getRemittanceInformation(), ';'))
                .setDate(transaction.getBookingDate())
                .setRawDetails(transaction.getEntryReference())
                .setPending(!STATUS_BOOKED.equals(transaction.getStatus()))
                .build();
    }

    private boolean isCreditCards(AccountEntity accountEntity) {
        return CashAccountType.CARD.toString().equals(accountEntity.getCashAccountType());
    }

    private LocalDate getOldestDateForTransactionFetch() {
        return localDateTimeSource
                .now()
                .toLocalDate()
                .minusDays(MAX_NUM_DAYS_FOR_FETCH_WITHOUT_SCA);
    }

    private LocalDate getLocalDateFromDate(Date date) {
        return date != null ? date.toInstant().atZone(ZoneId.of("CET")).toLocalDate() : null;
    }
}
