package se.tink.backend.aggregation.agents.nxgen.pt.banks.edenred.authenticator.rpc;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AuthenticationPinRequest {

    private String userId;
    private String password;
    private String appType;
    private String appVersion;
}
