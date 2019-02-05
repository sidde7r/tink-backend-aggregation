package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.SdcApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcAmount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcChange;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities.SdcCustodyDetailsModel;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;

@JsonObject
public class CustodyOverviewResponse {
    private SdcAmount availableBalance;
    private List<SdcCustodyDetailsModel> deposits;
    private SdcChange intradayChange;
    private SdcAmount totalValue;

    public SdcAmount getAvailableBalance() {
        return availableBalance;
    }

    public List<SdcCustodyDetailsModel> getDeposits() {
        return deposits;
    }

    public SdcChange getIntradayChange() {
        return intradayChange;
    }

    public SdcAmount getTotalValue() {
        return totalValue;
    }

    public List<InvestmentAccount> toInvestmentAccounts(SdcApiClient bankClient) {
        if (deposits == null) {
            return Collections.emptyList();
        }

        return deposits.stream()
                .map(deposit -> deposit.toInvestmentAccount(bankClient))
                .collect(Collectors.toList());
    }
}
