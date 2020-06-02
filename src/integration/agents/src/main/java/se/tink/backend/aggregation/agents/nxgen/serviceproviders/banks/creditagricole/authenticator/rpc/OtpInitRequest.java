package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OtpInitRequest extends DefaultAuthRequest {
    private int userId;
    private String perimeterId;

    public OtpInitRequest(int userId, String perimeterId) {
        super(CreditAgricoleConstants.Form.ADD_EXTERNAL_IBAN);
        this.userId = userId;
        this.perimeterId = perimeterId;
    }
}
