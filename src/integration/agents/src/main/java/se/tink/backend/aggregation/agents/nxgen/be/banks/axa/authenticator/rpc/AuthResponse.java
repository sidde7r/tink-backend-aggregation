package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.AuthDataEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities.HeaderEntity;

public class AuthResponse {

    private AuthDataEntity data;
    private String errorCode;
    private String errorMessage;
    private List<HeaderEntity> headers;
}
