package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher;

import java.util.Collection;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1ApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.rpc.LoanListResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.aggregation.nxgen.http.URL;

public class Sparebank1LoanFetcher implements AccountFetcher<LoanAccount> {

    private final Sparebank1ApiClient apiClient;
    private final String bankName;

    public Sparebank1LoanFetcher(Sparebank1ApiClient apiClient, String bankKey) {
        this.apiClient = apiClient;
        this.bankName = bankKey.substring(4);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        URL url = Sparebank1Constants.Urls.LOANS.parameter(Sparebank1Constants.UrlParameter.BANK_NAME, this.bankName);

        return this.apiClient.get(url, LoanListResponse.class).getLoans().stream()
                .map(loanEntity -> {
                    LoanDetailsEntity loanDetails = fetchLoanDetailsFor(loanEntity.getId());
                    return LoanAccount.builder(loanEntity.getFormattedNumber(), loanEntity.getBalance())
                            .setName(loanEntity.getName())
                            .setInterestRate(loanDetails.getInterestRate())
                            .setUniqueIdentifier(loanEntity.getId())
                            .setDetails(LoanDetails.builder()
                                    .setName(loanDetails.getName()) // Is this same as LoanAccount.name?
                                    .setInitialBalance(loanDetails.getInitialBalance())
                                    .setSecurity(loanDetails.getCollateral())
                                    .build())
                            .build();
                }).collect(Collectors.toList());
    }

    private LoanDetailsEntity fetchLoanDetailsFor(String id) {
        URL url = Sparebank1Constants.Urls.LOAN_DETAILS
                .parameter(Sparebank1Constants.UrlParameter.BANK_NAME, this.bankName)
                .parameter(Sparebank1Constants.UrlParameter.ACCOUNT_ID, id)
                .queryParam("_", String.valueOf(System.currentTimeMillis()));

        return this.apiClient.get(url, LoanDetailsEntity.class);
    }
}
