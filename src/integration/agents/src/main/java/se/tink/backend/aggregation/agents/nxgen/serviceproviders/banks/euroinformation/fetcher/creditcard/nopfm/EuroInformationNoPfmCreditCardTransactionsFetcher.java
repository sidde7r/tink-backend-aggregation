package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm;

import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.EuroInformationConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.rpc.notpaginated.TransactionSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class EuroInformationNoPfmCreditCardTransactionsFetcher implements TransactionFetcher<CreditCardAccount> {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(EuroInformationNoPfmCreditCardTransactionsFetcher.class);
    private static final AggregationLogger AGGREGATION_LOGGER = new AggregationLogger(
            EuroInformationNoPfmCreditCardTransactionsFetcher.class);
    private final EuroInformationApiClient apiClient;

    private EuroInformationNoPfmCreditCardTransactionsFetcher(EuroInformationApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public static EuroInformationNoPfmCreditCardTransactionsFetcher create(EuroInformationApiClient apiClient) {
        return new EuroInformationNoPfmCreditCardTransactionsFetcher(apiClient);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        String webId = account.getFromTemporaryStorage(EuroInformationConstants.Tags.WEB_ID);
        Optional<TransactionSummaryResponse> transactionsForAccount = getTransactionsForAccount(webId);

        List<AggregationTransaction> transactions = Lists.newArrayList();
        transactionsForAccount.ifPresent(transactionList ->
                transactionList.getTransactions().stream()
                        .map(TransactionEntity::toTransaction)
                        .forEach(transactions::add)
        );
        return transactions;
    }

    private Optional<TransactionSummaryResponse> getTransactionsForAccount(String webId) {
        TransactionSummaryResponse details = apiClient.getTransactionsWhenNoPfm(webId);
        if (!EuroInformationUtils.isSuccess(details.getReturnCode())) {
            //TODO: We do not know if creditcard uses same endpoint for transactions, so we try to use it and log error
            AGGREGATION_LOGGER.infoExtraLong(SerializationUtils.serializeToString(details),
                    EuroInformationConstants.LoggingTags.creditcardTransactionsTag);
            return Optional.empty();
        }
        return Optional.of(details);
    }

}
