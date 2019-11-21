package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

import java.util.Optional;

@JsonObject
public class HeaderEntityWrapper {
    @JsonProperty("Header")
    private HeaderEntity header;

    public HeaderEntity getHeader() {
        return header;
    }

    public Integer getResultCode() {
        return Optional.ofNullable(getHeader())
                .map(HeaderEntity::getStatus)
                .map(StatusEntity::getCode)
                .orElse(null);
    }

    public boolean isSuccessful() {
        Integer resultCode = getResultCode();
        return Optional.ofNullable(resultCode)
                .map(code -> NovoBancoConstants.ResponseCodes.OK == code)
                .orElse(false);
    }
}
