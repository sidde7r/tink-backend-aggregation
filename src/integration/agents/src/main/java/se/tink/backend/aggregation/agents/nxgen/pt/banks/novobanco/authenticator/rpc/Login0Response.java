package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoConstants.ResponseCodes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.BodyEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.HeaderEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.StatusEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.rpc.GenericResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Login0Response extends GenericResponse {
    public BodyEntity getBody() {
        return body;
    }

    @JsonProperty("Body")
    private BodyEntity body;

    public boolean isValidCredentials() {
        return Optional.ofNullable(getHeader())
                .map(HeaderEntity::getStatus)
                .map(StatusEntity::getCode)
                .map(code -> ResponseCodes.OK == code)
                .orElse(false);
    }
}
