package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final SwedbankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public SwedbankTransactionFetcher(
            final SwedbankApiClient apiClient,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    private Optional<FetchOnlineTransactionsResponse> fetchOnlineTransactions(
            TransactionalAccount account) {

        HttpResponse httpResponse =
                apiClient.getTransactions(
                        account.getApiIdentifier(),
                        Timestamp.valueOf(
                                LocalDateTime.now()
                                        .minusDays(TimeValues.ONLINE_STATEMENT_MAX_DAYS)),
                        Timestamp.valueOf(LocalDateTime.now()));

        return Optional.ofNullable(httpResponse.getBody(FetchOnlineTransactionsResponse.class));
        //        } else {
        //            return downaloadZippedTransactions(
        //                    httpResponse
        //                            .getBody(StatementResponse.class)
        //                            .getLinks()
        //                            .getDownload()
        //                            .getHref());
        //        }
    }

    private Optional<FetchOnlineTransactionsResponse> downaloadZippedTransactions(
            String downloadLink) {
        do {
            try (ZipInputStream zipInputStream =
                    new ZipInputStream(
                            apiClient.getTransactions(downloadLink).getBodyInputStream())) {
                // There's only one file in the archive
                ZipEntry entry = zipInputStream.getNextEntry();
                if (entry == null) {
                    return Optional.empty();
                }
                return Optional.of(
                        SerializationUtils.deserializeFromString(
                                IOUtils.toString(zipInputStream, StandardCharsets.UTF_8),
                                FetchOnlineTransactionsResponse.class));
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
        Optional<FetchOnlineTransactionsResponse> fetchOnlineTransactionsResponse =
                fetchOnlineTransactions(account);
        List<AggregationTransaction> onlineTransactions =
                fetchOnlineTransactionsResponse
                        .map(FetchOnlineTransactionsResponse::getTransactions)
                        .map(TransactionsEntity::getTinkTransactions)
                        .orElseGet(Lists::newArrayList);
        return onlineTransactions;
    }
}
