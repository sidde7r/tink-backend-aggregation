package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.sidentity.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.ErsteBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;

@JsonObject
public class PollResponse implements ThirdPartyAppResponse<String> {

    @JsonProperty("secondFactorStatus")
    private String secondFactorStatus;

    @JsonProperty("pollingIntervalMs")
    private int pollingIntervalMs;

    public String getSecondFactorStatus() {
        return secondFactorStatus;
    }

    public int getPollingIntervalMs() {
        return pollingIntervalMs;
    }

    @Override
    public ThirdPartyAppStatus getStatus() {
        switch (secondFactorStatus) {
        case ErsteBankConstants.SIDENTITY.POLL_WAITING:
            return ThirdPartyAppStatus.WAITING;
        case ErsteBankConstants.SIDENTITY.POLL_DONE:
            return ThirdPartyAppStatus.DONE;
        default:
            throw new IllegalStateException(
                    String.format("Sidentity unknown polling status: %s", secondFactorStatus));
        }
    }

    @Override
    public String getReference() {
        return null;
    }
}