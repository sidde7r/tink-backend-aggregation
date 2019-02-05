package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

public class SigningRequestHeaderDto {
    private final TypeEncodedPair signingId;

    private SigningRequestHeaderDto(TypeEncodedPair signingId) {
        this.signingId = signingId;
    }

    public static SigningRequestHeaderDto create(TypeEncodedPair signingId) {
        return new SigningRequestHeaderDto(signingId);
    }

    public TypeEncodedPair getSigningId() {
        return signingId;
    }
}
