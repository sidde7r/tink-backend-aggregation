package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step;

import com.nimbusds.jose.JWSObject;
import java.util.Optional;
import java.util.UUID;
import lombok.SneakyThrows;
import net.minidev.json.JSONObject;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.UserContext;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.AuthorizationTransactionResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.ChallengeResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.rpc.CheckRegisterAppResponse;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.jwt.FinalizeAuthJWEManager;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.form.Form;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FinalizeAuthStep implements AuthenticationStep {
    private final BancoPostaApiClient apiClient;
    private UserContext userContext;
    private final FinalizeAuthJWEManager jweManager;

    public FinalizeAuthStep(BancoPostaApiClient apiClient, UserContext userContext) {
        this.apiClient = apiClient;
        this.userContext = userContext;
        this.jweManager = new FinalizeAuthJWEManager(userContext);
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        CheckRegisterAppResponse checkRegisterAppResponse = checkRegisterApp();

        if (!checkRegisterAppResponse.getCommandResult().isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
        ChallengeResponse challengeResponse = challenge();

        String signature = authorizeTransaction(challengeResponse);

        String basicToken = requestAccessToken(signature, challengeResponse.getTransactionId());
        userContext.saveToPersistentStorage(Storage.ACCESS_BASIC_TOKEN, basicToken);

        String dataToken = apiClient.performJwtAuthorization();
        userContext.saveToPersistentStorage(Storage.ACCESS_DATA_TOKEN, dataToken);

        userContext.saveToPersistentStorage(Storage.MANUAL_AUTH_FINISH_FLAG, true);
        return AuthenticationStepResponse.authenticationSucceeded();
    }

    private String authorizeTransaction(ChallengeResponse challengeResponse) {
        String authorizeTransactionJWE = jweManager.genAuthorizeTransactionJWE(challengeResponse);
        AuthorizationTransactionResponse response =
                apiClient.authorizeTransaction(authorizeTransactionJWE);
        return Optional.ofNullable(response.getCommandResult().getSignature())
                .orElseThrow(
                        () ->
                                LoginError.NOT_SUPPORTED.exception(
                                        "missing signature during transaction authorization"));
    }

    @SneakyThrows
    private ChallengeResponse challenge() {
        String jwe = jweManager.genChallengeJWE();
        String jweResponse = apiClient.challenge(jwe);
        JSONObject json = JWSObject.parse(jweResponse).getPayload().toJSONObject();
        return SerializationUtils.deserializeFromString(
                json.toJSONString(), ChallengeResponse.class);
    }

    private CheckRegisterAppResponse checkRegisterApp() {
        String jwe = jweManager.genCheckRegisterJWE();
        return apiClient.checkRegisterApp(jwe);
    }

    private String requestAccessToken(String signature, String transactionId) {
        String jwe = jweManager.genAccessTokenJWE(signature, transactionId);
        Form form = buildForm(jwe);
        return apiClient.performSecondOpenIdAz(form.serialize());
    }

    private Form buildForm(String jweObject) {
        return Form.builder()
                .put("state", UUID.randomUUID().toString().toUpperCase())
                .put("acr_values", "https://idp.poste.it/L2")
                .put("prompt", "none login")
                .put("response_type", "token")
                .put("nonce", UUID.randomUUID().toString().toUpperCase())
                .put("grant_type", "signed_challenge")
                .put("scope", FormValues.SCOPE)
                .put("jti", UUID.randomUUID().toString().toUpperCase())
                .put("credentials", jweObject)
                .put("iss", "https://oidc-proxy.poste.it")
                .put("sub", "posteID")
                .put("aud", "https://idp-poste.poste.it")
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.FINALIZE_AUTH_STEP_ID;
    }
}
