package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.rpc;

import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.HandelsbankenSEConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.rpc.BaseResponse;

public class Failure extends BaseResponse {

    public boolean customerIsUnder16() {
        return HandelsbankenSEConstants.Fetcher.Transfers.UNDER_16.equals(getCode());
    }
}
