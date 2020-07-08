package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.AuthStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.ConsentStatus;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.SwedbankConstants.TimeValues;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.entity.transaction.TransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank.fetcher.transactionalaccount.rpc.StatementResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
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
                Timestamp.valueOf(LocalDateTime.now().minusMonths(TimeValues.MONTHS_TO_FETCH_MAX));
        toDate = Timestamp.valueOf(LocalDateTime.now());
    }

    private boolean checkIfScaIsRequired(HttpResponseException e) {
        return (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                && e.getResponse()
                        .getBody(String.class)
                        .toLowerCase()
                        .contains(SwedbankConstants.ErrorMessages.SCA_REQUIRED));
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

    private Optional<FetchTransactionsResponse> fetchAllTransactions(TransactionalAccount account) {

        StatementResponse response;
        try {
            response = apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
        } catch (HttpResponseException e) {
            if (checkIfScaIsRequired(e)) {
                String scaStatus =
                        startScaAuthorization(account.getApiIdentifier(), fromDate, toDate);
                if (!scaStatus.equalsIgnoreCase(AuthStatus.FINALIZED)) {
                    return Optional.empty();
                }
                response = apiClient.getTransactions(account.getApiIdentifier(), fromDate, toDate);
            } else {
                throw e;
            }
        }
        try (ZipInputStream zipInputStream =
                new ZipInputStream(
                        apiClient
                                .getTransactions(response.getLinks().getDownload().getHref())
                                .getBodyInputStream())) {
            // There's only one file in the archive
            zipInputStream.getNextEntry();
            return Optional.of(
                    SerializationUtils.deserializeFromString(
                            IOUtils.toString(zipInputStream, StandardCharsets.UTF_8),
                            FetchTransactionsResponse.class));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse transactions");
        }
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {
        // First add upcoming then booked
        Optional<FetchTransactionsResponse> fetchTransactionsResponse =
                fetchAllTransactions(account);
        return Stream.of(
                        fetchTransactionsResponse
                                .map(FetchTransactionsResponse::getTransactions)
                                .map(TransactionsEntity::getPending)
                                .map(
                                        tes ->
                                                tes.stream()
                                                        .map(te -> te.toTinkTransaction(true))
                                                        .collect(Collectors.toList()))
                                .orElseGet(Lists::newArrayList),
                        fetchTransactionsResponse
                                .map(FetchTransactionsResponse::getTransactions)
                                .map(TransactionsEntity::getPending)
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
