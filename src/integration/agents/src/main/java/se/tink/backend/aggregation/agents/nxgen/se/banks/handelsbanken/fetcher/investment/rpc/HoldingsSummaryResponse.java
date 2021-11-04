package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.Holdings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class HoldingsSummaryResponse extends BaseResponse {

    private Holdings myHoldings;

    public Collection<InvestmentAccount> toTinkInvestments(HandelsbankenSEApiClient client) {
        return myHoldings.getCustodyAccounts().stream()
                .map(custodyAccount -> custodyAccount.toInvestmentAccount(client))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
