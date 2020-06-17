package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ResultBankIdEntity {
    private String progressStatus;
    private AuthenticationTokenEntity authenticationToken;
    private AgreementsEntity agreements;

    public String getProgressStatus() {
        return progressStatus;
    }

    public String getToken() {
        return authenticationToken.getToken();
    }

    public String getId(String orgNumber) {
        return agreements.getId(orgNumber);
    }

    public String getHolderName() {
        return agreements.getHolderName();
    }
}
