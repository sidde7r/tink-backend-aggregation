package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto;

public class SignValidationRequest {
    private final TypeValuePair signingResponse;
    private final TypeValuePair panNr;
    private final SigningRequestHeaderDto header;

    private SignValidationRequest(TypeValuePair signingResponse, TypeValuePair panNr, SigningRequestHeaderDto header) {
        this.signingResponse = signingResponse;
        this.panNr = panNr;
        this.header = header;
    }

    public static SignValidationRequest create(TypeValuePair signingResponse, TypeValuePair panNr,
            SigningRequestHeaderDto header) {
        return new SignValidationRequest(signingResponse, panNr, header);
    }

    public static SignValidationRequest create(String signingResponse, String panNr, String signingId) {
        return new SignValidationRequest(
                TypeValuePair.createText(signingResponse),
                TypeValuePair.createText(panNr),
                SigningRequestHeaderDto.create(
                        TypeEncodedPair.createHidden(signingId)));
    }

    public TypeValuePair getSigningResponse() {
        return signingResponse;
    }

    public TypeValuePair getPanNr() {
        return panNr;
    }

    public SigningRequestHeaderDto getHeader() {
        return header;
    }
}
