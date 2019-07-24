package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.entities.dictionary.SibsAuthenticationType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonInclude(Include.NON_NULL)
@JsonObject
public class SibsScaMethodsEntity {

    private SibsAuthenticationType authenticationType;
    private String authenticationVersion;
    private String authenticationMethodId;
    private String name;
    private String explanation;

    public SibsAuthenticationType getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(SibsAuthenticationType authenticationType) {
        this.authenticationType = authenticationType;
    }

    public String getAuthenticationVersion() {
        return authenticationVersion;
    }

    public void setAuthenticationVersion(String authenticationVersion) {
        this.authenticationVersion = authenticationVersion;
    }

    public String getAuthenticationMethodId() {
        return authenticationMethodId;
    }

    public void setAuthenticationMethodId(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}
