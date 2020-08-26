package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseAccountConverter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.AccountsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity.BalancesItemEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class HandelsbankenBaseTransactionalAccountFetcher
        implements AccountFetcher<TransactionalAccount>,
                TransactionDatePaginator<TransactionalAccount> {

    private final HandelsbankenBaseApiClient apiClient;
    private HandelsbankenBaseAccountConverter converter;
    private final Date maxDate;

    private static final Logger logger =
            LoggerFactory.getLogger(HandelsbankenBaseTransactionalAccountFetcher.class);

    public HandelsbankenBaseTransactionalAccountFetcher(
            HandelsbankenBaseApiClient apiClient, LocalDate maxPeriodTransactions) {
        this.apiClient = apiClient;
        this.maxDate =
                Date.from(
                        maxPeriodTransactions
                                .atStartOfDay()
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
    }

    public void setConverter(HandelsbankenBaseAccountConverter converter) {
        this.converter = converter;
    }

    private Optional<TransactionalAccount> mapToTransactionalAccount(
            AccountsItemEntity accountEntity) {
        BalancesItemEntity availableBalance =
                apiClient.getAccountDetails(accountEntity.getAccountId()).getBalances().stream()
                        .filter(BalancesItemEntity::isBalance)
                        .findFirst()
                        .orElse(null);
        if (availableBalance != null) {
            return converter.toTinkAccount(accountEntity, availableBalance);
        } else {
            logger.warn(ExceptionMessages.BALANCE_NOT_FOUND);
            return Optional.empty();
        }
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient.getAccountList().getAccounts().stream()
                .map(this::mapToTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public PaginatorResponse getTransactionsFor(
            TransactionalAccount account, Date fromDate, Date toDate) {

        if (fromDate.compareTo(maxDate) < 0) {
            fromDate = maxDate;
        }

        if (fromDate.compareTo(toDate) >= 0) {
            return PaginatorResponseImpl.createEmpty();
        }

        return apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
    }
}
