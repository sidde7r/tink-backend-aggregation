package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InitBankIdRequest {
    private final boolean generateEasyLoginId;
    private final String userId;
    private final boolean useEasyLogin;

    private InitBankIdRequest(boolean generateEasyLoginId, String userId, boolean useEasyLogin) {
        this.generateEasyLoginId = generateEasyLoginId;
        this.userId = userId;
        this.useEasyLogin = useEasyLogin;
    }

    public static InitBankIdRequest createFromUserId(String userId) {
        return new InitBankIdRequest(true, userId, false);
    }

    public boolean isGenerateEasyLoginId() {
        return generateEasyLoginId;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isUseEasyLogin() {
        return useEasyLogin;
    }
}
