package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.TokenResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RequiredArgsConstructor
public class SpardaErrorHandler {

    private static final TppMessage TOKEN_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("TOKEN_INVALID")
                    .text(
                            "The OAuth2 token is associated to the TPP but is not valid for the addressed service/resource")
                    .build();

    private final SpardaTokenApiClient tokenApiClient;
    private final SpardaStorage storage;
    private final String clientId;

    void tryHandleTokenExpiredError(HttpResponseException httpResponseException) {
        if (httpResponseException.getResponse().getStatus() == 401) {
            Optional<ErrorResponse> maybeErrorResponse =
                    ErrorResponse.fromHttpException(httpResponseException);
            if (maybeErrorResponse.isPresent()) {
                ErrorResponse errorResponse = maybeErrorResponse.get();
                boolean isTokenInvalid =
                        ErrorResponse.anyTppMessageMatchesPredicate(TOKEN_INVALID)
                                .test(errorResponse);

                if (isTokenInvalid) {
                    TokenResponse tokenResponse =
                            tokenApiClient.refreshToken(
                                    storage.getToken().getRefreshToken().orElse(""), clientId);

                    storage.saveToken(tokenResponse.toTinkToken());
                    return;
                }
            }
        }
        throw httpResponseException;
    }
}
