package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.dk;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.utils.log.LogTag;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;

@Slf4j
@RequiredArgsConstructor
public class DkSSAuthenticationController implements TypedAuthenticator {

    private static final LogTag LOG_TAG = LogTag.from("[DkSS]");

    private final DkSSAuthenticatorsConfig authenticatorsConfig;

    @Override
    public void authenticate(Credentials credentials) {
        DkSSMethod chosenMethod = getMethodChosenByUser(credentials);

        DkSSAuthenticatorProvider methodAuthenticatorProvider =
                authenticatorsConfig
                        .getAuthenticationProviderForMethod(chosenMethod)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No authentication provider for method: "
                                                        + chosenMethod));

        Authenticator methodAuthenticator = methodAuthenticatorProvider.initializeAuthenticator();
        methodAuthenticator.authenticate(credentials);
    }

    private DkSSMethod getMethodChosenByUser(Credentials credentials) {
        if (!credentials.hasField(Field.Key.AUTH_METHOD_SELECTOR)) {
            throw new IllegalStateException("Cannot find authentication method selector field");
        }
        String fieldValue = credentials.getField(Field.Key.AUTH_METHOD_SELECTOR);
        DkSSMethod method =
                DkSSMethod.getBySupplementalInfoKey(fieldValue)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "No method matching method for field value: "
                                                        + fieldValue));
        log.info("{} Method selected by user: {}", LOG_TAG, method);
        return method;
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
