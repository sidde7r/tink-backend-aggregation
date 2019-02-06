package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.AktiaConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegistrationCompleteRequest {
    private String deviceToken;
    private String pushNotificationToken;
    private List<String> consents;

    public RegistrationCompleteRequest(String deviceToken) {
        this.deviceToken = deviceToken;
        this.pushNotificationToken = AktiaConstants.Avain.PUSH_NOTIFICATION_TOKEN;
        this.consents = Collections.singletonList(AktiaConstants.Avain.CONSENT);
    }
}
