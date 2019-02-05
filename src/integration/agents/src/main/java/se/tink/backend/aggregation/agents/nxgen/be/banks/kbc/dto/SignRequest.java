package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

public class SignRequest {
    private final SigningRequestHeaderDto header;

    private SignRequest(SigningRequestHeaderDto header) {
        this.header = header;
    }

    public static SignRequest create(SigningRequestHeaderDto header) {
        return new SignRequest(header);
    }

    public static SignRequest createWithSigningId(String signingId) {
        return new SignRequest(
                SigningRequestHeaderDto.create(
                        TypeEncodedPair.createHidden(signingId)));
    }

    public SigningRequestHeaderDto getHeader() {
        return header;
    }
}
