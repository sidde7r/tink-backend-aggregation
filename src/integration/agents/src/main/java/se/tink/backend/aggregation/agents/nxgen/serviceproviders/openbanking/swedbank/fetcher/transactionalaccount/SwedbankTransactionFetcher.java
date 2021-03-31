package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOfflineTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class SwedbankTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final SwedbankApiClient apiClient;
    private final SessionStorage sessionStorage;
    private final String providerMarket;

    public SwedbankTransactionFetcher(
            final SwedbankApiClient apiClient,
            SessionStorage sessionStorage,
            String providerMarket) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.providerMarket = providerMarket;
    }

    private Optional<FetchOnlineTransactionsResponse> fetchOnlineTransactions(
            TransactionalAccount account) {

        return Optional.ofNullable(
                apiClient.getOnlineTransactions(
                        account.getApiIdentifier(),
                        LocalDate.now().minusDays(TimeValues.ONLINE_STATEMENT_MAX_DAYS),
                        LocalDate.now()));
    }

    private Optional<FetchOfflineTransactionsResponse> downloadZippedTransactions(
            Optional<StatementResponse> statementResponse, String accountId) {
        if (!statementResponse.isPresent()) {
            return Optional.empty();
        }
        String downloadLink = statementResponse.get().getLinks().getDownload().getHref();

        do {
            try (ZipInputStream zipInputStream =
                    new ZipInputStream(
                            apiClient.getOfflineTransactions(downloadLink).getBodyInputStream())) {
                // There's only one file in the archive
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null) {
                    return Optional.empty();
                }

                String offlineTransactions =
                        IOUtils.toString(zipInputStream, StandardCharsets.UTF_8);
                log.info(
                        "Unzipped transactions for account ID {}: {}",
                        accountId,
                        offlineTransactions);

                return Optional.of(
                        SerializationUtils.deserializeFromString(
                                offlineTransactions, FetchOfflineTransactionsResponse.class));

            } catch (HttpResponseException e) {
                if (e.getResponse().getStatus() == SwedbankConstants.HttpStatus.RESOURCE_PENDING) {
                    // Download resource is not ready yet. TPP should retry download link after some
                    // time (proposed wait time is 60 seconds).
                    Uninterruptibles.sleepUninterruptibly(
                            TimeValues.RETRY_TRANSACTIONS_DOWNLOAD, TimeUnit.MILLISECONDS);
                } else {
                    throw e;
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to parse transactions");
            }
        } while (true);
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        List<AggregationTransaction> transactions =
                fetchOnlineTransactions(account)
                        .map(FetchOnlineTransactionsResponse::getTransactions)
                        .map(te -> te.getTinkTransactions(providerMarket))
                        .orElseGet(Lists::newArrayList);

        transactions.addAll(
                downloadZippedTransactions(
                                sessionStorage.get(
                                        account.getApiIdentifier(), StatementResponse.class),
                                account.getApiIdentifier())
                        .map(FetchOfflineTransactionsResponse::getTransactions)
                        .map(
                                transactionEntities ->
                                        transactionEntities.stream()
                                                .map(te -> te.toTinkTransaction(providerMarket))
                                                .collect(Collectors.toList()))
                        .orElseGet(Lists::newArrayList));
        return transactions;
    }
}
