package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.SkandiaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.entities.UpcomingPaymentEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.upcomingtransaction.rpc.FetchPaymentsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class SkandiaBankenUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {
    private final SkandiaBankenApiClient apiClient;

    public SkandiaBankenUpcomingTransactionFetcher(SkandiaBankenApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        final FetchPaymentsResponse approvedPaymentsResponse = apiClient.fetchApprovedPayments();

        return approvedPaymentsResponse.stream()
                .filter(
                        payment ->
                                payment.getSenderAccountNumber().equals(account.getAccountNumber()))
                .filter(UpcomingPaymentEntity::isApproved)
                .map(UpcomingPaymentEntity::toTinkUpcomingTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
