package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.rpc;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
public class AuthorisationStatusResponse {
    private String scaStatus;
    private String authorisationCode;
    private SelectedRole selectedRole;
    private List<SelectedRole> availableRoles;
}
