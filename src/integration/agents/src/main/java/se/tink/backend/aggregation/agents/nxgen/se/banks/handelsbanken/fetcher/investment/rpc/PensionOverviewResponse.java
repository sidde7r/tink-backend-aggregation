package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.investment.entities.HandelsbankenSEPensionInfo;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PensionOverviewResponse extends BaseResponse {

    @JsonProperty("employerSaving")
    private List<HandelsbankenSEPensionInfo> pensionInfoList;

    public List<HandelsbankenSEPensionInfo> getPensionInfoList() {
        return pensionInfoList;
    }
}
