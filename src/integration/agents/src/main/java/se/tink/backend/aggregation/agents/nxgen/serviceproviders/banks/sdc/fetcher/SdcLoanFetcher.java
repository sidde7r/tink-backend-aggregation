package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.agent.sdk.operation.Provider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcSessionStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SdcServiceConfigurationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities.SessionStorageAgreements;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc.ListLoanAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit.TotalKreditLoanFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class SdcLoanFetcher extends SdcAgreementFetcher implements AccountFetcher<LoanAccount> {
    private final Provider provider;

    private final TotalKreditLoanFetcher totalKreditLoanFetcher;

    public SdcLoanFetcher(
            SdcApiClient bankClient, SdcSessionStorage sessionStorage, Provider provider) {
        super(bankClient, sessionStorage);
        this.provider = provider;
        totalKreditLoanFetcher = new TotalKreditLoanFetcher(bankClient);
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        List<LoanAccount> accountList = new ArrayList<>();

        SessionStorageAgreements agreements = getAgreements();

        for (SessionStorageAgreement agreement : agreements) {
            Optional<SdcServiceConfigurationEntity> serviceConfiguration =
                    selectAgreement(agreement, agreements);

            if (!serviceConfiguration.isPresent()) {
                continue;
            }

            if (serviceConfiguration.get().isLoan()) {
                accountList.addAll(sdcAccountsToTinkAccounts(bankClient.listLoans()));
            }

            if (serviceConfiguration.get().isTotalkredit()) {
                accountList.addAll(
                        totalKreditLoanFetcher.fetchTotalKreditAccounts(
                                agreement.getAgreementId()));
            }
        }

        return accountList;
    }

    private List<LoanAccount> sdcAccountsToTinkAccounts(
            final ListLoanAccountsResponse loanAccounsResponse) {
        if (loanAccounsResponse != null) {
            return loanAccounsResponse.stream()
                    .map(la -> la.toTinkLoan(provider.getCurrency()))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }
}
