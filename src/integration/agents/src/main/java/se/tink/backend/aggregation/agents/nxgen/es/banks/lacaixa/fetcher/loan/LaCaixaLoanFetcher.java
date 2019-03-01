package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanDetailsAggregate;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class LaCaixaLoanFetcher implements AccountFetcher<LoanAccount> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LaCaixaLoanFetcher.class);
    private final LaCaixaApiClient apiClient;

    public LaCaixaLoanFetcher(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanListResponse> loanPagesList = new ArrayList<>();
        LoanListResponse loanListResponse = apiClient.fetchLoansList(true);
        loanPagesList.add(loanListResponse);
        while (loanListResponse.getMoreData()) {
            loanListResponse = apiClient.fetchLoansList(false);
            loanPagesList.add(loanListResponse);
        }

        List<LoanAccount> loanAccounts = new ArrayList<>();

        List<LoanEntity> loanList =
                loanPagesList
                        .stream()
                        .flatMap(l -> l.getLoans().stream())
                        .collect(Collectors.toList());
        loanList.forEach(
                l -> {
                    LoanDetailsResponse loanDetails = apiClient.fetchLoanDetails(l.getContractId());
                    LoanDetailsAggregate loanDetailsAggregate =
                            new LoanDetailsAggregate(l, loanDetails);
                    LoanAccount loanAccount = loanDetailsAggregate.toTinkLoanAccount();
                    loanAccounts.add(loanAccount);

                    logLoanData(loanAccount, l, loanDetails);
                });

        return loanAccounts;
    }

    // logging method to discover different types of laons than mortgage
    private void logLoanData(
            LoanAccount loanAccount,
            LoanEntity loanEntity,
            LoanDetailsResponse loanDetailsResponse) {
        if (LoanDetails.Type.OTHER == loanAccount.getDetails().getType()) {
            LOGGER.info(
                    "Unknown loan type: {} {}",
                    SerializationUtils.serializeToString(loanEntity),
                    SerializationUtils.serializeToString(loanDetailsResponse));
        }
    }
}
