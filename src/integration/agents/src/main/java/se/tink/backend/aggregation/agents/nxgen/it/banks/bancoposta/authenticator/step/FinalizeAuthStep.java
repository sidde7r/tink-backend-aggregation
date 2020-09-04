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
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.FormParams;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
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
    private final BancoPostaStorage storage;
    private final FinalizeAuthJWEManager jweManager;

    public FinalizeAuthStep(BancoPostaApiClient apiClient, BancoPostaStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.jweManager = new FinalizeAuthJWEManager(storage);
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        CheckRegisterAppResponse checkRegisterAppResponse = checkRegisterApp();

        if (!checkRegisterAppResponse.getCommandResult().isValid()) {
            storage.clearStorage();
            throw SessionError.SESSION_EXPIRED.exception();
        }
        ChallengeResponse challengeResponse = challenge();

        String signature = authorizeTransaction(challengeResponse);

        String basicToken = requestAccessToken(signature, challengeResponse.getTransactionId());
        storage.saveToPersistentStorage(Storage.ACCESS_BASIC_TOKEN, basicToken);

        String dataToken = apiClient.performJwtAuthorization();
        storage.saveToPersistentStorage(Storage.ACCESS_DATA_TOKEN, dataToken);

        storage.saveToPersistentStorage(Storage.MANUAL_AUTH_FINISH_FLAG, true);
        storage.removeDataUsedOnlyForManualAuth();
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
                .put(FormParams.STATE, UUID.randomUUID().toString().toUpperCase())
                .put(FormParams.ACR_VALUES, FormValues.POSTE_URL_L2)
                .put(FormParams.PROMPT, FormValues.NONE_LOGIN)
                .put(FormParams.RESPONSE_TYPE, FormValues.TOKEN)
                .put(FormParams.NONCE, UUID.randomUUID().toString().toUpperCase())
                .put(FormParams.GRANT_TYPE, FormValues.SIGNED_CHALLENGE)
                .put(FormParams.SCOPE, FormValues.SCOPE)
                .put(FormParams.JTI, UUID.randomUUID().toString().toUpperCase())
                .put(FormParams.CREDENTIALS, jweObject)
                .put(FormParams.ISS, FormValues.OIDC_URL)
                .put(FormParams.SUB, FormValues.POSTE_ID)
                .put(FormParams.AUD, FormValues.IDP_URL)
                .build();
    }

    @Override
    public String getIdentifier() {
        return BancoPostaAuthenticator.FINALIZE_AUTH_STEP_ID;
    }
}
