package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaConstants;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.TokenResponse;
import se.tink.backend.aggregation.nxgen.http.form.Form;

@RequiredArgsConstructor
public class SpardaTokenApiClient {

    private static final String GRANT_TYPE = "grant_type";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String CLIENT_ID = "client_id";
    private static final String CODE = "code";
    private static final String CODE_VERIFIER = "code_verifier";
    private static final String REDIRECT_URI = "redirect_uri";

    private final SpardaRequestBuilder requestBuilder;

    public TokenResponse exchangeCodeForToken(
            String code, String redirectUrl, String clientId, String codeVerifier) {
        String tokenEntity =
                Form.builder()
                        .put(GRANT_TYPE, AUTHORIZATION_CODE)
                        .put(CODE, code)
                        .put(REDIRECT_URI, redirectUrl)
                        .put(CLIENT_ID, clientId)
                        .put(CODE_VERIFIER, codeVerifier)
                        .build()
                        .serialize();
        return sendToken(tokenEntity);
    }

    public TokenResponse refreshToken(String refreshToken, String clientId) {
        String tokenEntity =
                Form.builder()
                        .put(GRANT_TYPE, REFRESH_TOKEN)
                        .put(REFRESH_TOKEN, refreshToken)
                        .put(CLIENT_ID, clientId)
                        .build()
                        .serialize();

        return sendToken(tokenEntity);
    }

    public TokenResponse sendToken(String tokenEntity) {
        return requestBuilder
                .createRawRequest(SpardaConstants.Urls.TOKEN)
                .type(MediaType.APPLICATION_FORM_URLENCODED)
                .post(TokenResponse.class, tokenEntity);
    }
}
