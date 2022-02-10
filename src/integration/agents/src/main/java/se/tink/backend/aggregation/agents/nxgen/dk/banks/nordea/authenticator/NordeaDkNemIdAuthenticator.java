package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.contexts.MetricContext;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdError;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.NordeaDkConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.AuthenticationsPatchResponse;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc.NemIdParamsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.MultiFactorAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParameters;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdParametersFetcher;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializerModule;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

@Slf4j
@AgentDependencyModules(modules = NemIdIFrameControllerInitializerModule.class)
public class NordeaDkNemIdAuthenticator
        implements MultiFactorAuthenticator, NemIdParametersFetcher {

    private static final String NEM_ID_SCRIPT_FORMAT =
            "<script type=\"text/x-nemid\" id=\"nemid_parameters\">%s</script>";

    private final NordeaDkApiClient bankClient;
    private final NemIdIFrameController iFrameController;
    private final NordeaDkAuthenticatorUtils authenticatorUtils;

    private final SessionStorage sessionStorage;

    public NordeaDkNemIdAuthenticator(
            NordeaDkApiClient bankClient,
            SessionStorage sessionStorage,
            Catalog catalog,
            StatusUpdater statusUpdater,
            SupplementalInformationController supplementalInformationController,
            MetricContext metricContext,
            AgentTemporaryStorage agentTemporaryStorage,
            NemIdIFrameControllerInitializer iFrameControllerInitializer,
            NordeaDkAuthenticatorUtils authenticatorUtils) {
        this.bankClient = Objects.requireNonNull(bankClient);
        this.sessionStorage = Objects.requireNonNull(sessionStorage);
        this.iFrameController =
                iFrameControllerInitializer.initNemIdIframeController(
                        this,
                        NemIdCredentialsProvider.defaultProvider(),
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext,
                        agentTemporaryStorage);
        this.authenticatorUtils = authenticatorUtils;
    }

    public void authenticate(final Credentials credentials) throws AuthenticationException {
        String nemIdToken = iFrameController.logInWithCredentials(credentials);
        String authorizationCode = exchangeNemIdToken(nemIdToken);
        authenticatorUtils.exchangeOauthToken(authorizationCode);
    }

    @Override
    public NemIdParameters getNemIdParameters() {
        OAuthSessionData sessionData = authenticatorUtils.prepareOAuthSessionData();

        String referer =
                bankClient.initOauthForNemId(
                        sessionData.getCodeChallenge(),
                        sessionData.getState(),
                        sessionData.getNonce());
        sessionStorage.put(NordeaDkConstants.StorageKeys.REFERER, referer);

        NemIdParamsResponse nemIdParamsResponse =
                bankClient.getNemIdParams(
                        sessionData.getCodeChallenge(),
                        sessionData.getState(),
                        sessionData.getNonce());
        String sessionId = nemIdParamsResponse.getSessionId();
        sessionStorage.put(StorageKeys.SESSION_ID, sessionId);

        ObjectMapper mapper = new ObjectMapper();
        String params;
        try {
            params = mapper.writeValueAsString(nemIdParamsResponse.getNemidParams());
        } catch (JsonProcessingException e) {
            throw NemIdError.INTERRUPTED.exception();
        }
        return new NemIdParameters(
                String.format(NEM_ID_SCRIPT_FORMAT, params)
                        + String.format(
                                NemIdConstants.NEM_ID_IFRAME_FORMAT,
                                NemIdConstants.NEM_ID_INIT_URL + Instant.now().toEpochMilli()));
    }

    private String exchangeNemIdToken(String nemIdToken) {
        String referer = sessionStorage.get(StorageKeys.REFERER);
        AuthenticationsPatchResponse authenticationsPatchResponse =
                bankClient.authenticationsPatch(
                        nemIdToken, sessionStorage.get(StorageKeys.SESSION_ID), referer);
        sessionStorage.put(StorageKeys.NEMID_TOKEN, authenticationsPatchResponse.getNemIdToken());
        String code = authenticationsPatchResponse.getCode();
        return bankClient.codeExchange(code, referer).getCode();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
