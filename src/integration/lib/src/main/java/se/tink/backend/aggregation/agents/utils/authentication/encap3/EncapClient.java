package se.tink.backend.aggregation.agents.utils.authentication.encap3;

import javax.annotation.Nullable;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapConstants.Urls;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.IdentificationEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.entities.RegistrationResultEntity;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.enums.AuthenticationMethod;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceAuthenticationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.models.DeviceRegistrationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.IdentificationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.RegistrationResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.rpc.SamlResponse;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapMessageUtils;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.utils.EncapSoapUtils;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
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

        this.httpClient =
                NextGenTinkHttpClient.builder(
                                context.getLogMasker(),
                                LogMaskerImpl.shouldLog(request.getProvider()))
                        .setAggregatorInfo(context.getAggregatorInfo())
                        .setMetricRegistry(context.getMetricRegistry())
                        .setLogOutputStream(context.getLogOutputStream())
                        .setSignatureKeyPair(signatureKeyPair)
                        .setProvider(request.getProvider())
                        .build();

        // Encap does not like our signature header :(
        this.httpClient.disableSignatureRequestHeader();

        this.storage = new EncapStorage(persistentStorage);
        this.soapUtils = new EncapSoapUtils(configuration, storage);
        this.messageUtils =
                new EncapMessageUtils(configuration, storage, httpClient, deviceProfile);
    }

    public DeviceRegistrationResponse registerDevice(String username, String activationCode) {
        storage.seedStorage(username);

        String authenticationBody = soapUtils.buildAuthSessionCreateRequest();
        messageUtils.encryptSoapAndSend(Urls.SOAP_AUTHENTICATION, authenticationBody);

        String activationBody = soapUtils.buildActivationSessionUpdateRequest(activationCode);
        final String soapResponse =
                messageUtils.encryptSoapAndSend(Urls.SOAP_ACTIVATION, activationBody);

        String activationSessionId = EncapSoapUtils.getActivationSessionId(soapResponse);

        String registrationMessage = messageUtils.buildRegistrationMessage();
        RegistrationResponse registrationResponse =
                messageUtils.encryptAndSend(registrationMessage, RegistrationResponse.class);
        if (!registrationResponse.isValid()) {
            throw new IllegalStateException("ActivationResponse is not valid.");
        }

        RegistrationResultEntity registrationResultEntity = registrationResponse.getResult();
        storeRegistrationResult(registrationResultEntity);

        String activationMessage = messageUtils.buildActivationMessage(registrationResultEntity);
        SamlResponse samlResponse =
                messageUtils.encryptAndSend(activationMessage, SamlResponse.class);
        if (!samlResponse.isValid()) {
            throw new IllegalStateException("SamlResponse is not valid.");
        }
        String samlObjectB64 = samlResponse.getResult().getSamlObjectAsBase64();

        String activateDeviceBody =
                soapUtils.buildActivationCreateRequest(
                        username, activationSessionId, samlObjectB64);
        String activateDeviceSoapResponse =
                messageUtils.encryptSoapAndSend(Urls.SOAP_ACTIVATION, activateDeviceBody);

        return createDeviceRegistrationResponse(activateDeviceSoapResponse);
    }

    public DeviceAuthenticationResponse authenticateDevice(
            AuthenticationMethod authenticationMethod) {
        return authenticateDevice(authenticationMethod, null);
    }

    public DeviceAuthenticationResponse authenticateDevice(
            AuthenticationMethod authenticationMethod, @Nullable String authenticationId) {
        if (!storage.load()) {
            throw new IllegalStateException("Storage is not valid.");
        }

        String username = storage.getUsername();

        String authenticationBody = soapUtils.buildAuthSessionCreateRequest();
        messageUtils.encryptSoapAndSend(Urls.SOAP_AUTHENTICATION, authenticationBody);

        String identificationMessage = messageUtils.buildIdentificationMessage(authenticationId);
        IdentificationResponse identificationResponse =
                messageUtils.encryptAndSend(identificationMessage, IdentificationResponse.class);
        if (!identificationResponse.isValid()) {
            throw new IllegalStateException("IdentificationResponse is not valid.");
        }

        IdentificationEntity identificationEntity = identificationResponse.getResult();
        storeIdentificationResult(identificationEntity);

        String authenticationMessage =
                messageUtils.buildAuthenticationMessage(identificationEntity, authenticationMethod);
        SamlResponse samlResponse =
                messageUtils.encryptAndSend(authenticationMessage, SamlResponse.class);
        if (!samlResponse.isValid()) {
            throw new IllegalStateException("SamlResponse is not valid.");
        }

        String samlObjectB64 = samlResponse.getResult().getSamlObjectAsBase64();

        String authenticateDeviceBody =
                soapUtils.buildAuthSessionReadRequest(username, samlObjectB64);
        String soapResponse =
                messageUtils.encryptSoapAndSend(Urls.SOAP_AUTHENTICATION, authenticateDeviceBody);

        return createDeviceAuthenticationResponse(soapResponse);
    }

    public void saveDevice() {
        storage.save();
    }

    private DeviceRegistrationResponse createDeviceRegistrationResponse(String soapResponse) {
        String securityToken =
                EncapSoapUtils.getSecurityToken(soapResponse)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find securityToken in activation response."));

        String samUserId =
                EncapSoapUtils.getSamUserId(soapResponse)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find samUserId in activation response."));

        storage.setSamUserId(samUserId);

        return new DeviceRegistrationResponse(samUserId, securityToken);
    }

    private DeviceAuthenticationResponse createDeviceAuthenticationResponse(String soapResponse) {
        String securityToken =
                EncapSoapUtils.getSecurityToken(soapResponse)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find securityToken in activation response."));

        String samUserId =
                EncapSoapUtils.getSamUserId(soapResponse)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Could not find samUserId in activation response."));

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
}
