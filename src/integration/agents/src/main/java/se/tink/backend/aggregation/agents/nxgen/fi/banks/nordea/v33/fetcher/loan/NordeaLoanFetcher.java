package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIConstants.LogTags;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.entities.LoansEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.loan.rpc.FetchLoanDetailsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class NordeaLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger log = new AggregationLogger(NordeaLoanFetcher.class);
    private final NordeaFIApiClient apiClient;
    private final SessionStorage sessionStorage;

    public NordeaLoanFetcher(NordeaFIApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchLoans().stream()
                .map(this::getLoanAccount)
                .collect(Collectors.toList());
    }

    private LoanAccount getLoanAccount(LoansEntity loansEntity) {
        FetchLoanDetailsResponse loanDetails = apiClient.fetchLoanDetails(loansEntity.getLoanId());
        log.infoExtraLong(SerializationUtils.serializeToString(loanDetails), LogTags.LOAN_ACCOUNT);
        return loanDetails.toTinkLoanAccount();
    }

    public FetchLoanDetailsResponse fetchLoanDetails(String accountId) {
        return apiClient.fetchLoanDetails(accountId);
    }
}
