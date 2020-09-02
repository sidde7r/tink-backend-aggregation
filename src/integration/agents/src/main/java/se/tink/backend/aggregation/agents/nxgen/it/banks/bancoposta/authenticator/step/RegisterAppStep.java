package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import static se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator.REGISTER_APP_STEP_ID;

import com.amazonaws.util.StringUtils;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.crypto.RSADecrypter;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.RegisterAppDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.RegisterAppJWEManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class RegisterAppStep implements AuthenticationStep {
    private final BancoPostaApiClient apiClient;
    private UserContext userContext;
    private final RegisterAppJWEManager jweManager;

    public RegisterAppStep(BancoPostaApiClient apiClient, UserContext userContext) {
        this.apiClient = apiClient;
        this.userContext = userContext;
        this.jweManager = new RegisterAppJWEManager(userContext);
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        String registerAppJWE = jweManager.genRegisterAppJWE();
        Map<String, String> responseJwt = apiClient.registerApp(registerAppJWE);

        String errorCode = responseJwt.get("command-error-code");

        if (!StringUtils.isNullOrEmpty(errorCode)) {
            handleKnownErrors(errorCode);
            if (userContext.isUserPinSetRequired()) {
                return AuthenticationStepResponse.executeStepWithId(REGISTER_APP_STEP_ID);
            }
        }
        String commandResultJWT =
                Optional.ofNullable(responseJwt.get("command-result"))
                        .orElseThrow(
                                () ->
                                        LoginError.NOT_SUPPORTED.exception(
                                                "Missing register app data"));
        RegisterAppDetailsResponse response = convertJWTToResponse(commandResultJWT);

        String appRegisterId = response.getData().getAppRegisterId();
        userContext.saveToPersistentStorage(Storage.APP_REGISTER_ID, appRegisterId);

        String secretApp = response.getData().getSecretApp();
        userContext.saveToPersistentStorage(Storage.SECRET_APP, secretApp);

        return AuthenticationStepResponse.executeStepWithId(
                BancoPostaAuthenticator.FINALIZE_AUTH_STEP_ID);
    }

    @SneakyThrows
    private RegisterAppDetailsResponse convertJWTToResponse(String jwtString) {
        JWEObject jweObject = JWEObject.parse(jwtString);
        jweObject.decrypt(new RSADecrypter(userContext.getKeyPair().getPrivate()));
        String json = jweObject.getPayload().toString();
        return SerializationUtils.deserializeFromString(json, RegisterAppDetailsResponse.class);
    }

    private void handleKnownErrors(String errorCode) {
        switch (errorCode) {
            case "PIN-ERR-1":
                userContext.setUserPinSetRequired(true);
                break;
            case "DEVICE-ERR-2":
                throw LoginError.REGISTER_DEVICE_ERROR.exception();
            default:
                throw LoginError.DEFAULT_MESSAGE.exception(
                        String.format("Unknown register app error number %s ", errorCode));
        }
    }

    @Override
    public String getIdentifier() {
        return REGISTER_APP_STEP_ID;
    }
}
