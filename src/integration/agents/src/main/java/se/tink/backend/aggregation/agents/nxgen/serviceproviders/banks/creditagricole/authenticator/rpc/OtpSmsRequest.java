package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpSmsRequest extends DefaultAuthRequest {

    private String otp;
    private String grantType = "otp_sms";

    public OtpSmsRequest(String otp) {
        super("create_user");
        this.otp = otp;
    }
}
