package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SoftwareStatement {

    private SoftwareStatementAssertion softwareStatementAssertion;

    public SoftwareStatement() {}

    public SoftwareStatement(
            SoftwareStatementAssertion softwareStatementAssertion, SignatureKey signingKey) {
        this.softwareStatementAssertion = softwareStatementAssertion;
    }

    public SoftwareStatementAssertion getAssertion() {
        return softwareStatementAssertion;
    }

    public String getSoftwareId() {
        return softwareStatementAssertion.getSoftwareId();
    }

    public String[] getAllRedirectUris() {
        return softwareStatementAssertion.getAllRedirectUris();
    }
}
