package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius;

import agents_platform_agents_framework.org.springframework.http.RequestEntity;
import agents_platform_agents_framework.org.springframework.http.RequestEntity.BodyBuilder;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants.Url;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.AuthenticateWithCodeResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CheckStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.CloseSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.FeedStructureRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.OpenSessionRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.OpenSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareAuthenticationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareAuthenticationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareDeviceRegistrationRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareDeviceRegistrationResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.PrepareLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.RegisterDeviceSignResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator.rpc.SendCardNumberResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.fetcher.transactional.rpc.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.payments.rpc.EntitySelect;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.BelfiusResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.MessageResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.SessionOpenedResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.rpc.StartFlowRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.KeepAliveRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler.rpc.TechnicalResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AgentHttpClient;
import se.tink.libraries.serialization.utils.SerializationUtils;

@AllArgsConstructor
public class AgentPlatformBelfiusApiClient {

    private final AgentHttpClient client;
    private final String locale;

    public void requestConfigIos() {
        client.exchange(
                RequestEntity.get(Url.CONFIG_IOS.toUri())
                        .header("Accept", "*/*")
                        .header("Accept-Encoding", "br, gzip, deflate")
                        .header("Accept-Language", "nl-be")
                        .header("Connection", "keep-alive")
                        .header(
                                "User-Agent",
                                "Belfius%20Mobile/192811614 CFNetwork/974.2.1 Darwin/18.0.0")
                        .build(),
                String.class,
                null);
    }

    public SessionOpenedResponse openSession(String machineId) {
        return post(
                        buildGepaRenderingUrl(machineId),
                        OpenSessionResponse.class,
                        OpenSessionRequest.create(this.locale))
                .getSessionData();
    }

    public void keepAlive(String sessionId, String machineId, String requestCounterAggregated) {
        post(
                        buildGepaRenderingUrl(machineId),
                        BelfiusResponse.class,
                        KeepAliveRequest.create()
                                .setSessionId(sessionId)
                                .setRequestCounter(requestCounterAggregated))
                .filter(TechnicalResponse.class)
                .forEach(TechnicalResponse::checkSessionExpired);
    }

    public void startFlow(String sessionId, String machineId, String requestCounterAggregated) {
        post(
                buildGepaRenderingUrl(machineId),
                BelfiusResponse.class,
                BelfiusRequest.builder()
                        .setRequests(StartFlowRequest.create())
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated));
    }

    public PrepareAuthenticationResponse prepareAuthentication(
            String sessionId, String machineId, String requestCounterAggregated, String panNumber)
            throws AuthenticationException {
        return post(
                buildGepaRenderingUrl(machineId),
                PrepareAuthenticationResponse.class,
                PrepareAuthenticationRequest.create(panNumber)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated),
                false);
    }

    public AuthenticateWithCodeResponse authenticateWithCode(
            String sessionId, String machineId, String requestCounterAggregated, String code)
            throws AuthenticationException {
        return post(
                buildGepaRenderingUrl(machineId),
                AuthenticateWithCodeResponse.class,
                AuthenticateWithCodeRequest.create(code)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated));
    }

    public boolean isDeviceRegistered(
            String sessionId,
            String machineId,
            String requestCounterServices,
            String panNumber,
            String deviceTokenHash) {
        return post(
                        buildGepaServiceUrl(machineId),
                        CheckStatusResponse.class,
                        CheckStatusRequest.create(panNumber, deviceTokenHash)
                                .setSessionId(sessionId)
                                .setRequestCounter(requestCounterServices))
                .isDeviceRegistered();
    }

    public void sendIsDeviceRegistered(
            String sessionId,
            String machineId,
            String requestCounterServices,
            String panNumber,
            String deviceTokenHash) {
        post(
                buildGepaServiceUrl(machineId),
                CheckStatusResponse.class,
                CheckStatusRequest.create(panNumber, deviceTokenHash)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterServices));
    }

    public void consultClientSettings(
            String sessionId, String machineId, String requestCounterServices) {
        post(
                buildGepaServiceUrl(machineId),
                CheckStatusResponse.class,
                CheckStatusRequest.createConsultClientSettings()
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterServices));
    }

    public RegisterDeviceSignResponse registerDevice(
            String sessionId, String machineId, String requestCounterAggregated, String signature)
            throws AuthenticationException {
        return post(
                buildGepaRenderingUrl(machineId),
                RegisterDeviceSignResponse.class,
                RegisterDeviceRequest.create(signature)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated));
    }

    public String prepareDeviceRegistration(
            String sessionId,
            String machineId,
            String requestCounterAggregated,
            String deviceToken,
            String deviceBrand,
            String deviceName) {
        PrepareDeviceRegistrationResponse response =
                post(
                        buildGepaRenderingUrl(machineId),
                        PrepareDeviceRegistrationResponse.class,
                        PrepareDeviceRegistrationRequest.create(
                                        deviceToken, deviceBrand, deviceName)
                                .setSessionId(sessionId)
                                .setRequestCounter(requestCounterAggregated));
        return response.getChallenge();
    }

    public void closeSession(String sessionId, String machineId, String requestCounterAggregated) {
        post(
                buildGepaRenderingUrl(machineId),
                BelfiusResponse.class,
                CloseSessionRequest.create(sessionId)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated));
    }

    public PrepareLoginResponse prepareLogin(
            String sessionId, String machineId, String requestCounterAggregated, String panNumber)
            throws LoginException {
        return post(
                buildGepaRenderingUrl(machineId),
                PrepareLoginResponse.class,
                PrepareLoginRequest.create(panNumber)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated),
                false);
    }

    public LoginResponse login(
            String sessionId,
            String machineId,
            String requestCounterAggregated,
            String deviceTokenHashed,
            String deviceTokenHashedIosComparison,
            String signature) {
        return post(
                buildGepaRenderingUrl(machineId),
                LoginResponse.class,
                LoginRequest.create(deviceTokenHashed, deviceTokenHashedIosComparison, signature)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated),
                false);
    }

    public void bacProductList(String sessionId, String machineId, String requestCounterServices) {
        post(
                buildGepaServiceUrl(machineId),
                BelfiusResponse.class,
                FeedStructureRequest.createBacProductList()
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterServices));
    }

    public SendCardNumberResponse sendCardNumber(
            String sessionId,
            String machineId,
            String requestCounterAggregated,
            String cardNumber) {
        return post(
                buildGepaRenderingUrl(machineId),
                SendCardNumberResponse.class,
                EntitySelect.createWithCardNumber(sessionId, cardNumber)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated));
    }

    public LoginResponse loginPw(
            String sessionId,
            String machineId,
            String requestCounterAggregated,
            String deviceTokenHashed,
            String deviceTokenHashedIosComparison,
            String signature) {
        return post(
                buildGepaRenderingUrl(machineId),
                LoginResponse.class,
                LoginRequest.createPw(deviceTokenHashed, deviceTokenHashedIosComparison, signature)
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterAggregated),
                false);
    }

    public BelfiusResponse actorInformation(
            String sessionId, String machineId, String requestCounterServices) {
        return post(
                buildGepaServiceUrl(machineId),
                FetchTransactionsResponse.class,
                CheckStatusRequest.createActor()
                        .setSessionId(sessionId)
                        .setRequestCounter(requestCounterServices));
    }

    private <T extends BelfiusResponse> T post(
            URI uri, Class<T> c, BelfiusRequest.Builder belfiusBuilder) {
        return post(uri, c, belfiusBuilder, true);
    }

    private <T extends BelfiusResponse> T post(
            URI uri, Class<T> c, BelfiusRequest.Builder belfiusBuilder, boolean withValidation) {
        String body = belfiusBuilder.build().urlEncode();

        BodyBuilder builder =
                RequestEntity.post(uri)
                        .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                        .header("Accept", MediaType.WILDCARD);
        BelfiusConstants.HEADERS.forEach((name, val) -> builder.header(name, val));

        String responseBody = client.exchange(builder.body(body), String.class, null).getBody();
        T response = SerializationUtils.deserializeFromString(responseBody, c);

        // once all error mapping is done get rid of the check here
        if (withValidation) {
            MessageResponse.validate(response);
        }
        return response;
    }

    private URI buildGepaRenderingUrl(String machineId) {
        return Url.GEPA_RENDERING_URL
                .parameter(BelfiusConstants.UrlParameter.MACHINE_IDENTIFIER, machineId)
                .toUri();
    }

    private URI buildGepaServiceUrl(String machineId) {
        return Url.GEPA_SERVICE_URL
                .parameter(BelfiusConstants.UrlParameter.MACHINE_IDENTIFIER, machineId)
                .toUri();
    }
}
