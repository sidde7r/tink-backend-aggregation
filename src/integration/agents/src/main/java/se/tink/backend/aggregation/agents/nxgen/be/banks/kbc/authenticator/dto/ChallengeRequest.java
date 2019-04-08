package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.authenticator.dto;

import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;

public class ChallengeRequest {
    private TypeValuePair authenticationType;
    private TypeValuePair language;

    private ChallengeRequest(TypeValuePair authenticationType, TypeValuePair language) {
        this.authenticationType = authenticationType;
        this.language = language;
    }

    public static ChallengeRequest create(
            TypeValuePair authenticationType, TypeValuePair language) {
        return new ChallengeRequest(authenticationType, language);
    }

    public static ChallengeRequest createWithStandardTypes(
            String authenticationType, String language) {
        return create(
                TypeValuePair.createText(authenticationType), TypeValuePair.createText(language));
    }

    public TypeValuePair getAuthenticationType() {
        return authenticationType;
    }

    public TypeValuePair getLanguage() {
        return language;
    }
}
