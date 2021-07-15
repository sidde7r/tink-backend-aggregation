package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount;

import com.google.api.client.http.HttpStatusCodes;
import com.google.common.base.Predicates;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.executors.entities.PaymentEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class NordeaUpcomingTransactionFetcher
        implements UpcomingTransactionFetcher<TransactionalAccount> {
    private final NordeaBaseApiClient apiClient;
    private final NordeaConfiguration nordeaConfiguration;

    public NordeaUpcomingTransactionFetcher(
            NordeaBaseApiClient apiClient, NordeaConfiguration nordeaConfiguration) {
        this.apiClient = apiClient;
        this.nordeaConfiguration = nordeaConfiguration;
    }

    @Override
    public Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(
            TransactionalAccount account) {
        if (nordeaConfiguration.isBusinessAgent()) {
            return Collections.emptyList();
        }

        try {
            return apiClient.fetchPayments().getPayments().stream()
                    .filter(Predicates.or(PaymentEntity::isConfirmed, PaymentEntity::isInProgress))
                    .filter(
                            paymentEntity ->
                                    paymentEntity.getFrom().equals(account.getAccountNumber()))
                    .map(PaymentEntity::toUpcomingTransaction)
                    .collect(Collectors.toList());
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatusCodes.STATUS_CODE_SERVER_ERROR) {
                throw BankServiceError.BANK_SIDE_FAILURE.exception();
            }
            throw e;
        }
    }
}
