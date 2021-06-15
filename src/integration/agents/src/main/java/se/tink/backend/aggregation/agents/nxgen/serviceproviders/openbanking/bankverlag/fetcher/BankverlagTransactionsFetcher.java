package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.transaction.FinTsTransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@Slf4j
public class BankverlagTransactionsFetcher implements TransactionFetcher<TransactionalAccount> {
    private final BankverlagApiClient apiClient;
    private final BankverlagStorage storage;
    private String aspspId;
    // Reusing FinTsTransactionMapper as Bankverlag also provides transactions in Swift format
    // later move this mapper to common util
    FinTsTransactionMapper mapper = new FinTsTransactionMapper();

    public BankverlagTransactionsFetcher(
            BankverlagApiClient apiClient, BankverlagStorage storage, String aspspId) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.aspspId = aspspId;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(TransactionalAccount account) {

        List<AggregationTransaction> aggregationTransactions = new ArrayList<>();

        if (BankverlagConstants.AspspId.ASPSP_WITH_URI_FOR_TRANSACTIONS.contains(aspspId)) {
            HttpResponse zipFile =
                    apiClient.getTransactionsZipFile(
                            storage.getConsentId(),
                            account.getApiIdentifier(),
                            getFetchStartDate());
            processZipFileTransactions(aggregationTransactions, zipFile);

        } else {
            String rawTransactions =
                    apiClient.fetchTransactions(
                            storage.getConsentId(),
                            account.getApiIdentifier(),
                            getFetchStartDate());
            aggregationTransactions.addAll(getTransactionsFromSwiftFormat(rawTransactions));
        }

        return aggregationTransactions;
    }

    private void processZipFileTransactions(
            List<AggregationTransaction> aggregationTransactions, HttpResponse zipFile) {
        try (ZipInputStream zipInputStream = new ZipInputStream(zipFile.getBodyInputStream())) {

            ZipEntry entry = zipInputStream.getNextEntry();

            while (entry != null) {
                aggregationTransactions.addAll(
                        getTransactionsFromCamtFormat(
                                IOUtils.toString(zipInputStream, StandardCharsets.UTF_8)));
                entry = zipInputStream.getNextEntry();
            }

        } catch (HttpResponseException e) {
            log.error("Unable to download transactions zip file", e);
        } catch (IOException e) {
            log.error("Failed to read transactions zip file", e);
        }
    }

    private List<AggregationTransaction> getTransactionsFromSwiftFormat(String rawTransactions) {
        try {
            return mapper.parseSwift(rawTransactions);
        } catch (Exception e) {
            log.error("Unable to parse Swift transactions", e);
            throw e;
        }
    }

    private List<AggregationTransaction> getTransactionsFromCamtFormat(String rawTransactions) {
        try {
            return mapper.parseCamt(rawTransactions);
        } catch (Exception e) {
            log.error("Unable to parse Camt transactions", e);
            throw e;
        }
    }

    private LocalDate getFetchStartDate() {
        LocalDate startDate;
        if (storage.isFirstFetch()) {
            // currently Degussa Bank provide transactions only for less than 90 days.
            // For transactions more than 90 days we need extra SCA.
            if (BankverlagConstants.BankverlagAspspId.DEGUSSABANK.equalsIgnoreCase(aspspId)) {
                startDate = LocalDate.now().minusDays(89);
            } else {
                startDate = LocalDate.ofEpochDay(0);
            }
            storage.markFirstFetchAsDone();
        } else {
            startDate = LocalDate.now().minusDays(89);
        }
        return startDate;
    }
}
