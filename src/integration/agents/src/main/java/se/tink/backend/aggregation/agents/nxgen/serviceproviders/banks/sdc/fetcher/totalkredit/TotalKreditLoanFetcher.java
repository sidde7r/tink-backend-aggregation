package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

@Slf4j
@RequiredArgsConstructor
public class TotalKreditLoanFetcher {

    private final SdcApiClient bankClient;

    public List<LoanAccount> fetchTotalKreditAccounts(final String agreementId) {
        try {
            return bankClient.listTotalKreditLoans().stream()
                    .map(totalKreditLoan -> totalKreditLoan.toTinkLoan(agreementId))
                    .collect(Collectors.toList());
        } catch (RuntimeException re) {
            log.error("An error occured when fetching totalkredit products.", re);
        }
        return Collections.emptyList();
    }
}
