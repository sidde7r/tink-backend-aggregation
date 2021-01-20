package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchOnlineTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.rpc.GenericResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankTransactionFetcher implements TransactionFetcher<TransactionalAccount> {

    private final SwedbankApiClient apiClient;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final Date fromDate;
    private final Date toDate;

    public SwedbankTransactionFetcher(
            final SwedbankApiClient apiClient,
            final SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.supplementalInformationHelper = supplementalInformationHelper;
        fromDate =
                Timestamp.valueOf(
                        LocalDateTime.now().minusDays(TimeValues.ONLINE_STATEMENT_MAX_DAYS));
        toDate = Timestamp.valueOf(LocalDateTime.now());
    }

    private boolean checkIfScaIsRequired(HttpResponseException e) {
        return (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                && e.getResponse().getBody(GenericResponse.class).requiresSca());
    }

    private String startScaAuthorization(String account, Date fromDate, Date toDate) {
        StatementResponse response =
                apiClient.startScaTransactionRequest(account, fromDate, toDate);
        if (response.getStatementStatus().equalsIgnoreCase(ConsentStatus.SIGNED)) {
            return AuthStatus.FINALIZED;
        }
        supplementalInformationHelper.openThirdPartyApp(
                ThirdPartyAppAuthenticationPayload.of(
                        new URL(response.getLinks().getHrefEntity().getHref())));

        for (int i = 0; i < SwedbankConstants.TimeValues.ATTEMPS_BEFORE_TIMEOUT; i++) {
            String status = apiClient.getScaStatus(response.getLinks().getScaStatus().getHref());
            if (!status.equalsIgnoreCase(AuthStatus.STARTED)
                    && !status.equalsIgnoreCase(AuthStatus.RECEIVED)) {
                return status;
            }

            Uninterruptibles.sleepUninterruptibly(
                    SwedbankConstants.TimeValues.SLEEP_TIME_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
        throw new IllegalStateException(
                SwedbankConstants.LogMessages.TRANSACTION_SIGNING_TIMED_OUT);
    }

    private Optional<FetchOnlineTransactionsResponse> fetchAllTransactions(
            TransactionalAccount account) {

        HttpResponse httpResponse;
        try {
            httpResponse = apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
        } catch (HttpResponseException e) {
            if (checkIfScaIsRequired(e)) {
                String scaStatus =
                        startScaAuthorization(account.getApiIdentifier(), fromDate, toDate);
                if (!scaStatus.equalsIgnoreCase(AuthStatus.FINALIZED)) {
                    return Optional.empty();
                }
                httpResponse =
                        apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
            } else {
                throw e;
            }
        }
        boolean isOnlineStatement =
                TimeUnit.DAYS.convert(
                                Instant.now().toEpochMilli() - fromDate.getTime(),
                                TimeUnit.MILLISECONDS)
                        <= TimeValues.ONLINE_STATEMENT_MAX_DAYS;
        if (isOnlineStatement) {
            return Optional.ofNullable(
                    (httpResponse.getBody(FetchOnlineTransactionsResponse.class)));
        } else {
            return downaloadZippedTransactions(
                    httpResponse
                            .getBody(StatementResponse.class)
                            .getLinks()
                            .getDownload()
                            .getHref());
        }
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
        Optional<FetchOnlineTransactionsResponse> fetchTransactionsResponse =
                fetchAllTransactions(account);
        return Stream.of(
                        fetchTransactionsResponse
                                .map(FetchOnlineTransactionsResponse::getTransactions)
                                .map(TransactionsEntity::getPending)
                                .map(
                                        tes ->
                                                tes.stream()
                                                        .map(te -> te.toTinkTransaction(true))
                                                        .collect(Collectors.toList()))
                                .orElseGet(Lists::newArrayList),
                        fetchTransactionsResponse
                                .map(FetchOnlineTransactionsResponse::getTransactions)
                                .map(TransactionsEntity::getBooked)
                                .map(
                                        tes ->
                                                tes.stream()
                                                        .map(te -> te.toTinkTransaction(false))
                                                        .collect(Collectors.toList()))
                                .orElseGet(Lists::newArrayList))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }
}
