package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.rpc.TokenResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

class CertificateIsRevokedExceptionRequestRepeater
        extends HttpResponseExceptionRequestRepeater<TokenResponse> {

    private static final int MAX_NUMBER_OF_REPETITIONS = 5;
    private final IngBaseApiClient ingBaseApiClient;
    private final String payload;

    public CertificateIsRevokedExceptionRequestRepeater(
            final IngBaseApiClient ingBaseApiClient, final String payload) {
        super(MAX_NUMBER_OF_REPETITIONS);
        this.ingBaseApiClient = ingBaseApiClient;
        this.payload = payload;
    }

    @Override
    public TokenResponse request() {
        return ingBaseApiClient
                .buildRequestWithSignature(
                        IngBaseConstants.Urls.TOKEN,
                        IngBaseConstants.Signature.HTTP_METHOD_POST,
                        payload)
                .addBearerToken(ingBaseApiClient.getApplicationTokenFromSession())
                .body(payload, MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class);
    }

    @Override
    public boolean checkIfRepeat(HttpResponseException ex) {
        return ex.getResponse().getStatus() == 400
                && ex.getResponse().getBody(String.class).contains("Certificate is revoked");
    }
}
