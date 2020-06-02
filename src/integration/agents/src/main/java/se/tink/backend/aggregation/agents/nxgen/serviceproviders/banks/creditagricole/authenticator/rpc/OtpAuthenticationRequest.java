package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpAuthenticationRequest extends DefaultAuthRequest {
    private String scope;
    private String perimeterId;
    private String userId;
    private String otp;
    private String grantType;

    public OtpAuthenticationRequest(String userId, String perimeterId, String otp) {
        super(CreditAgricoleConstants.Form.ADD_EXTERNAL_IBAN);
        this.perimeterId = perimeterId;
        this.userId = userId;
        this.otp = otp;
        this.grantType = CreditAgricoleConstants.Form.OTP_GRANT_TYPE;
    }
}
