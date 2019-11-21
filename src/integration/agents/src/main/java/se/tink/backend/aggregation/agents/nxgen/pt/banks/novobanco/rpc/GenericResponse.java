package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc;

import java.util.Objects;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseCodes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntityWrapper;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericResponse extends HeaderEntityWrapper {

    public boolean isSessionExpired() {
        Integer resultCode = getResultCode();
        return Objects.equals(ResponseCodes.SESSION_EXPIRED, resultCode)
                || hasZeroTimeoutAndErrorCode(resultCode);
    }

    private boolean hasZeroTimeoutAndErrorCode(Integer resultCode) {
        return !Objects.equals(ResponseCodes.OK, resultCode)
                && Integer.valueOf(0).equals(getHeader().getSessionTimeout());
    }
}
