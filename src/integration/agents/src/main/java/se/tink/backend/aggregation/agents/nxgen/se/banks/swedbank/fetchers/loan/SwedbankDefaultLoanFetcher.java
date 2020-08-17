package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LoanAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SwedbankDefaultLoanFetcher implements AccountFetcher<LoanAccount> {
    protected final SwedbankSEApiClient apiClient;

    public SwedbankDefaultLoanFetcher(SwedbankSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);
            EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();

            List<LoanAccountEntity> loanAccountEntities =
                    engagementOverviewResponse.getLoanAccounts();

            // check if user has loans, if not, continue
            if (loanAccountEntities == null || loanAccountEntities.size() < 1) {
                continue;
            }

            for (LoanAccountEntity loanAccountEntity : loanAccountEntities) {
                if (loanAccountEntity.getLinks() == null
                        || loanAccountEntity.getLinks().getNext() == null) {
                    continue;
                }

                // we get error sometimes from swedbank backend, due to 0 balance loans
                LoanDetailsResponse loanDetailsResponse =
                        apiClient.loanDetails(loanAccountEntity.getLinks().getNext());

                Optional<String> interest = loanDetailsResponse.getInterest();

                if (!interest.isPresent()) {
                    continue;
                }

                loanAccountEntity
                        .toLoanAccount(
                                interest.get(), loanDetailsResponse.getDueDate().orElse(null))
                        .ifPresent(loanAccounts::add);
            }
        }

        return loanAccounts;
    }
}
