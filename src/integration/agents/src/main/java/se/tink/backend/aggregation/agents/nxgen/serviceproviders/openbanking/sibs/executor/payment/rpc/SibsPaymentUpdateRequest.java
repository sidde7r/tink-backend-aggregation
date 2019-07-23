package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import java.util.HashMap;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.SibsPSUDataEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsPaymentUpdateRequest extends HashMap<String, Object> {

    private SibsPSUDataEntity psuData;
    private String scaAuthenticationData;
    private String authenticationMethodId;

    public SibsPSUDataEntity getPsuData() {
        return psuData;
    }

    public void setPsuData(SibsPSUDataEntity psuData) {
        this.psuData = psuData;
    }

    public String getScaAuthenticationData() {
        return scaAuthenticationData;
    }

    public void setScaAuthenticationData(String scaAuthenticationData) {
        this.scaAuthenticationData = scaAuthenticationData;
    }

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public void setAuthenticationMethodId(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
