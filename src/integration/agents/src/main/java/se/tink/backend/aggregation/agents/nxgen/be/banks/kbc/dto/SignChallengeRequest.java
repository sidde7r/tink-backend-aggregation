package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignChallengeRequest {
    private TypeEncodedPair signTypeId;
    private SigningRequestHeaderDto header;

    private SignChallengeRequest(TypeEncodedPair signTypeId, SigningRequestHeaderDto header) {
        this.signTypeId = signTypeId;
        this.header = header;
    }

    public static SignChallengeRequest create(String signTypeId, String signingId) {
        return new SignChallengeRequest(
                TypeEncodedPair.createHidden(signTypeId),
                SigningRequestHeaderDto.create(TypeEncodedPair.createHidden(signingId)));
    }

    public TypeEncodedPair getSignTypeId() {
        return signTypeId;
    }

    public SigningRequestHeaderDto getHeader() {
        return header;
    }
}
