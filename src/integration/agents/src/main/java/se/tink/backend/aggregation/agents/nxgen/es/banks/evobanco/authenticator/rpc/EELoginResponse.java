package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.authenticator.entities.EeOLoginEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.entities.ErrorsEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.rpc.EERpcResponse;

public class EELoginResponse extends EEBaseLoginResponse implements EERpcResponse {
    @JsonProperty("EE_O_Login")
    private EeOLoginEntity eeOLogin;

    public EeOLoginEntity getEeOLogin() {
        return eeOLogin;
    }

    @Override
    public boolean isUnsuccessfulReturnCode() {
        return eeOLogin.isUnsuccessfulReturnCode();
    }

    @Override
    public Optional<ErrorsEntity> getErrors() {
        return eeOLogin.getErrors();
    }
}
