package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseCodes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GenericResponse {
    @JsonProperty("Header")
    private HeaderEntity header;

    public HeaderEntity getHeader() {
        return header;
    }

    public boolean isSessionExpired() {
        Integer resultCode = getResultCode();
        return !Objects.equals(ResponseCodes.SESSION_EXPIRED, resultCode)
                && Integer.valueOf(0).equals(header.getSessionTimeout());
    }

    private Integer getResultCode() {
        return Optional.ofNullable(getHeader())
                .map(HeaderEntity::getStatus)
                .map(StatusEntity::getCode)
                .orElse(null);
    }

    private Integer getSessionTimeout() {
        return Optional.ofNullable(getHeader()).map(HeaderEntity::getSessionTimeout).orElse(null);
    }
}
