package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.CustodyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.agents.rpc.Credentials;

public class SecurityHoldingsResponse extends BaseResponse {
    private List<CustodyAccount> custodyAccounts;

    public Collection<InvestmentAccount> toTinkInvestments(HandelsbankenSEApiClient client, Credentials credentials) {
        return custodyAccounts.stream()
                .map(custodyAccount -> custodyAccount.toInvestmentAccount(client, credentials))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
