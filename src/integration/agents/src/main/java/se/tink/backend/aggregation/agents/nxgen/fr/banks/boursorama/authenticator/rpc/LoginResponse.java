package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.AuthenticatorStatusEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.authenticator.entity.ErrorResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse extends ErrorResponse {
    private List<Object> contactIdsPro;
    private String deviceEnrolmentTokenValue;
    private String deviceKey;
    private String level;
    private boolean managerUserId;
    private String memberId;
    private boolean professional;
    private AuthenticatorStatusEntity status;
    private String touchToken;
    private String userId;

    public String getDeviceEnrolmentTokenValue() {
        return deviceEnrolmentTokenValue;
    }
}
