package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.authenticator.entities.InstrumentInfoEntity;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class InstrumentInfoResponse {
    private boolean success;
    private boolean restartLogon;
    private boolean retry;
    private boolean norId;
    private boolean userAlreadyAuthenticated;
    private InstrumentInfoEntity instrumentInfo;

    public boolean isSuccess() {
        return success;
    }

    public boolean isRestartLogon() {
        return restartLogon;
    }

    public boolean isRetry() {
        return retry;
    }

    public boolean isNorId() {
        return norId;
    }

    public boolean isUserAlreadyAuthenticated() {
        return userAlreadyAuthenticated;
    }

    public InstrumentInfoEntity getInstrumentInfo() {
        return instrumentInfo;
    }
}
