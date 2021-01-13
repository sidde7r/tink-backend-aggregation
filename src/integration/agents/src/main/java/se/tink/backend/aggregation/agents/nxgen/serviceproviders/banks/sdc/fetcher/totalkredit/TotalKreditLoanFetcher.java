package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.totalkredit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;

@Slf4j
@RequiredArgsConstructor
public class TotalKreditLoanFetcher {

    private final SdcApiClient bankClient;

    public void fetchTotalKreditAccounts() {
        try {
            bankClient.listTotalKreditLoans();
        } catch (RuntimeException re) {
            log.error("An error occured when fetching totalkredit products.", re);
        }
    }
}
