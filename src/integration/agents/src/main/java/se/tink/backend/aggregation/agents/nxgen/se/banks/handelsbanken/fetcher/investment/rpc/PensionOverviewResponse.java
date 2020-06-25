package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@JsonObject
public class PensionOverviewResponse extends BaseResponse {

    @JsonProperty("employerSaving")
    private List<HandelsbankenSEPensionInfo> pensionInfoList;

    public List<HandelsbankenSEPensionInfo> getPensionInfoList() {
        return pensionInfoList;
    }

    public Collection<InvestmentAccount> toInvestments(HandelsbankenSEApiClient client) {
        return pensionInfoList.stream()
                .map(pensionInfo -> toInvestmentAccount(pensionInfo, client))
                .collect(Collectors.toList());
    }

    public InvestmentAccount toInvestmentAccount(
            HandelsbankenSEPensionInfo pensionInfo, HandelsbankenSEApiClient client) {
        PensionDetailsResponse pensionDetailsResponse = client.pensionDetails(pensionInfo);
        return pensionDetailsResponse.toInvestmentAccount(client, pensionInfo);
    }
}
