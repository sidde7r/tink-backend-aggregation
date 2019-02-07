package se.tink.backend.aggregation.agents.utils.authentication.encap2;

import java.util.Optional;
import javax.annotation.Nullable;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.models.DeviceRegistrationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc.IdentificationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc.RegistrationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.rpc.SamlResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap2.utils.EncapSoapUtils;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfile;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class EncapClient {
    private final TinkHttpClient httpClient;
    private final EncapStorage storage;
    private final EncapSoapUtils soapUtils;
    private final EncapMessageUtils messageUtils;

    public EncapClient(
            AgentContext context,
            CredentialsRequest request,
            SignatureKeyPair signatureKeyPair,
            PersistentStorage persistentStorage,
            EncapConfiguration configuration,
            DeviceProfile deviceProfile) {

        this.httpClient = new TinkHttpClient(context.getAggregatorInfo(), context.getMetricRegistry(),
                context.getLogOutputStream(), signatureKeyPair, request.getProvider());

        // Encap does not like it when we send our signature header.
        this.httpClient.disableSignatureRequestHeader();

        this.storage = new EncapStorage(persistentStorage);
        this.soapUtils = new EncapSoapUtils(configuration, storage);
        this.messageUtils = new EncapMessageUtils(configuration, storage, httpClient, deviceProfile);
    }

    public DeviceRegistrationResponse registerDevice(String username, String activationCode) {
        storage.seedStorage(username);

        if (!soapCreateAuthenticatedSession(username)) {
            throw new IllegalStateException("Could not create an authenticated session.");
        }

        String activationSessionId = soapGetActivationSessionId(username, activationCode)
                .orElseThrow(() -> new IllegalStateException("Could not get activationSessionId"));

        String registrationMessage = messageUtils.buildRegistrationMessage();
        RegistrationResponse registrationResponse = messageUtils.encryptAndSend(registrationMessage,
                RegistrationResponse.class);
        if (!registrationResponse.isValid()) {
            throw new IllegalStateException("ActivationResponse is not valid.");
        }

        RegistrationResultEntity registrationResultEntity = registrationResponse.getResult();
        storeRegistrationResult(registrationResultEntity);

        String activationMessage = messageUtils.buildActivationMessage(registrationResultEntity);
        SamlResponse samlResponse = messageUtils.encryptAndSend(activationMessage, SamlResponse.class);
        if (!samlResponse.isValid()) {
            throw new IllegalStateException("SamlResponse is not valid.");
        }

        String samlObjectB64 = samlResponse.getResult().getSamlObjectAsBase64();

        String soapResponse = soapActivateDevice(username, activationSessionId, samlObjectB64);

        return createDeviceRegistrationResponse(soapResponse);
    }

    public DeviceAuthenticationResponse authenticateDevice(AuthenticationMethod authenticationMethod) {
        return authenticateDevice(authenticationMethod, null);
    }

    public DeviceAuthenticationResponse authenticateDevice(AuthenticationMethod authenticationMethod,
            @Nullable String authenticationId) {
        if (!storage.load()) {
            throw new IllegalStateException("Storage is not valid.");
        }

        String username = storage.getUsername();
        if (!soapCreateAuthenticatedSession(username)) {
            throw new IllegalStateException("Could not create an authenticated session.");
        }

        String identificationMessage = messageUtils.buildIdentificationMessage(authenticationId);
        IdentificationResponse identificationResponse = messageUtils.encryptAndSend(identificationMessage,
                IdentificationResponse.class);
        if (!identificationResponse.isValid()) {
            throw new IllegalStateException("IdentificationResponse is not valid.");
        }

        IdentificationEntity identificationEntity = identificationResponse.getResult();

        storeIdentificationResult(identificationEntity);

        String authenticationMessage = messageUtils.buildAuthenticationMessage(identificationEntity,
                authenticationMethod);
        SamlResponse samlResponse = messageUtils.encryptAndSend(authenticationMessage, SamlResponse.class);
        if (!samlResponse.isValid()) {
            throw new IllegalStateException("SamlResponse is not valid.");
        }

        String samlObjectB64 = samlResponse.getResult().getSamlObjectAsBase64();

        String soapResponse = soapAuthenticateDevice(username, samlObjectB64);

        return createDeviceAuthenticationResponse(soapResponse);
    }

    public void saveDevice() {
        storage.save();
    }

    private DeviceRegistrationResponse createDeviceRegistrationResponse(String soapResponse) {
        String securityToken = soapUtils.getSecurityToken(soapResponse)
                .orElseThrow(() -> new IllegalStateException("Could not find securityToken in activation response."));

        String samUserId = soapUtils.getSamUserId(soapResponse)
                .orElseThrow(() -> new IllegalStateException("Could not find samUserId in activation response."));

        storage.setSamUserId(samUserId);

        return new DeviceRegistrationResponse(samUserId, securityToken);
    }

    private DeviceAuthenticationResponse createDeviceAuthenticationResponse(String soapResponse) {
        String securityToken = soapUtils.getSecurityToken(soapResponse)
                .orElseThrow(() -> new IllegalStateException("Could not find securityToken in activation response."));

        String samUserId = soapUtils.getSamUserId(soapResponse)
                .orElseThrow(() -> new IllegalStateException("Could not find samUserId in activation response."));

        storage.setSamUserId(samUserId);
        return new DeviceAuthenticationResponse(samUserId, securityToken, storage.getHardwareId());
    }

    private void storeIdentificationResult(IdentificationEntity identificationEntity) {
        // Update storage with next keyId and key
        storage.setClientSaltKey(identificationEntity.getB64ClientSaltNextKey());
        storage.setClientSaltKeyId(identificationEntity.getClientSaltNextKeyId());
    }

    private void storeRegistrationResult(RegistrationResultEntity registrationResultEntity) {
        int saltNextKeyId = registrationResultEntity.getClientSaltNextKeyId();
        storage.setClientSaltKeyId(saltNextKeyId);

        String saltNextKey = registrationResultEntity.getB64ClientSaltNextKey();
        storage.setClientSaltKey(saltNextKey);

        String registrationId = registrationResultEntity.getRegistration().getRegistrationId();
        storage.setRegistrationId(registrationId);

        String signingKeyPhrase = registrationResultEntity.getSigningKeyPhrase();
        storage.setSigningKeyPhrase(signingKeyPhrase);
    }

    private boolean soapCreateAuthenticatedSession(String username) {
        String dataToSend = soapUtils.buildAuthenticationSessionCreateV1Body(username);

        String response = postSoapMessage(EncapConstants.Urls.AUTHENTICATION_SESSION_CREATE,
                EncapConstants.HttpHeaders.AUTHENTICATION_SESSION_CREATE, dataToSend);

        return true;
    }

    private String soapActivateDevice(String username, String activationSessionId, String samlObjectB64) {
        String dataToSend = soapUtils.buildActivationCreateV1Body(username, activationSessionId, samlObjectB64);

        return postSoapMessage(EncapConstants.Urls.ACTIVATION_SERVICE,
                "\"\"", dataToSend);
    }

    private String soapAuthenticateDevice(String username, String samlObjectB64) {
        String dataToSend = soapUtils.buildAuthenticationV2Body(username, samlObjectB64);

        return postSoapMessage(EncapConstants.Urls.AUTHENTICATION_SERVICE,
                "\"\"", dataToSend);
    }

    private Optional<String> soapGetActivationSessionId(String username, String activationCode) {
        String dataToSend = soapUtils.buildActivationSessionUpdateV1Body(username, activationCode);

        String response = postSoapMessage(EncapConstants.Urls.ACTIVATION_SESSION_UPDATE,
                EncapConstants.HttpHeaders.ACTIVATION_SESSION_UPDATE, dataToSend);

        // Todo: throw correct exception on certain error codes.
        // However, they should not happen since there is no user interaction for these steps..
        return soapUtils.getActivationSessionId(response);
    }

    private String postSoapMessage(URL url, String soapAction, String body) {
        return httpClient.request(url)
                .header("SOAPAction", soapAction)
                .type("text/xml; charset=utf-8")
                .accept(MediaType.WILDCARD)
                .post(String.class, body);
    }
}
