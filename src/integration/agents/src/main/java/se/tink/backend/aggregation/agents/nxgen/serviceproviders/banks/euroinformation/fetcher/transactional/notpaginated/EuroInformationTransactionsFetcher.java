package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.notpaginated;

import com.google.api.client.util.Lists;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationErrorCodes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationTransactionsFetcher
        implements TransactionFetcher<TransactionalAccount> {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(EuroInformationTransactionsFetcher.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationTransactionsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationTransactionsFetcher create(EuroInformationApiClient apiClient) {
        return new EuroInformationTransactionsFetcher(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        String webId = account.getFromTemporaryStorage(EuroInformationConstants.Tags.WEB_ID);
        List<AggregationTransaction> transactions = Lists.newArrayList();

        Optional<TransactionSummaryResponse> transactionsForAccount =
                getTransactionsForAccount(webId);
        transactionsForAccount.ifPresent(
                transactionList ->
                        transactionList.getTransactions().stream()
                                .map(TransactionEntity::toTransaction)
                                .forEach(transactions::add));

        return transactions;
    }

    private Optional<TransactionSummaryResponse> getTransactionsForAccount(String webId) {
        TransactionSummaryResponse details = apiClient.getTransactionsWhenNoPfm(webId);
        String returnCode = details.getReturnCode();
        if (!EuroInformationUtils.isSuccess(returnCode)) {
            LOGGER.info(
                    EuroInformationErrorCodes.getByCodeNumber(returnCode).toString()
                            + SerializationUtils.serializeToString(details));
            return Optional.empty();
        }
        return Optional.of(details);
    }
}
