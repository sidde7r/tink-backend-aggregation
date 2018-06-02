package se.tink.backend.aggregation.agents.utils.authentication.encap;

import java.util.List;
import java.util.Map;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationSecondRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationSecondRequestBody;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class EncapClient {
    private TinkHttpClient client;
    private boolean encryptedMessageExchange;
    private EncapClientHelper encapClientHelper;
    private Map<String, String> encapStorage;
    private final PersistentStorage persistentStorage;

    public EncapClient(EncapConfiguration configuration, PersistentStorage persistentStorage,
            TinkHttpClient client, boolean encryptedMessageExchange, String username) {
        this.client = client;
        this.encryptedMessageExchange = encryptedMessageExchange;
        this.persistentStorage = persistentStorage;

        String encapStorageString = persistentStorage.get(EncapConstants.Storage.ENCAP_STORAGE);
        encapClientHelper = new EncapClientHelper(username, encapStorageString, persistentStorage, configuration);
        encapStorage = encapClientHelper.getEncapStorage();
    }

    public String activateAndAuthenticateUser(String activationCode) {
        createAuthenticationSession();
        String activationSessionId = updateActivationSession(activationCode);
        String samlObject;

        if (encryptedMessageExchange) {
            samlObject = executeEncryptedMessageExchangeForActivation();
        } else {
            samlObject = executePlainTextMessageExchangeForActivation();
        }

        return finishMobileActivation(activationSessionId, samlObject);
    }

    public String authenticateUser() {

        encapClientHelper.populateEncapStorage(persistentStorage.get(EncapConstants.Storage.ENCAP_STORAGE));
        encapStorage = encapClientHelper.getEncapStorage();
        createAuthenticationSession();
        String samlObject;

        if (encryptedMessageExchange) {
            samlObject = executeEncryptedMessageExchangeForAuthentication();
        } else {
            samlObject = executePlainTextMessageExchangeForAuthentication();
        }
        if (samlObject == null){
            return null;
        }

        return finishMobileAuthenticationService(samlObject);
    }

    public String getEncapHardwareId() {
        return encapStorage.get(EncapConstants.Storage.HARDWARE_ID);
    }

    private void createAuthenticationSession() {
        String dataToSend = encapClientHelper.buildAuthenticationSessionCreateV1String();

        client.request(EncapConstants.Urls.AUTHENTICATION_SESSION_CREATE)
                .header("SOAPAction", EncapConstants.HttpHeaders.AUTHENTICATION_SESSION_CREATE)
                .type("text/xml; charset=utf-8")
                .post(dataToSend);
    }

    private String updateActivationSession(String activationCode) {
        String dataToSend = encapClientHelper.buildActivationSessionUpdateV1String(activationCode);

        String response = postSoapMessage(EncapConstants.Urls.ACTIVATION_SESSION_UPDATE,
                EncapConstants.HttpHeaders.ACTIVATION_SESSION_UPDATE, dataToSend);

        return EncapUtils.getActivationSessionId(response);
    }

    private String executePlainTextMessageExchangeForActivation() {
        ActivationFirstRequestBody firstRequestBody = PlainTextMessageService
                .createActivationFirstRequestBody(encapStorage);
        String responseMessage = getPlainTextRequest().post(String.class, firstRequestBody);

        encapClientHelper.updateEncapParamsActivation(responseMessage);

        ActivationSecondRequestBody secondRequestBody =
                PlainTextMessageService.createActivationSecondRequestBody(encapStorage);
        responseMessage = getPlainTextRequest().post(String.class, secondRequestBody);

        return EncapUtils.getSamlObject(responseMessage);
    }

    private String executePlainTextMessageExchangeForAuthentication() {
        AuthenticationFirstRequestBody firstRequestBody = PlainTextMessageService
                .createAuthenticationFirstRequestBody(encapStorage);
        String responseMessage = getPlainTextRequest().post(String.class, firstRequestBody);
        if (!encapClientHelper.updateEncapParamsAuthentication(encryptedMessageExchange, responseMessage)){
            return null;
        }

        AuthenticationSecondRequestBody secondRequestBody = PlainTextMessageService
                .createAuthenticationSecondRequestBody(encapStorage);
        responseMessage = getPlainTextRequest().post(String.class, secondRequestBody);

        return EncapUtils.getSamlObject(responseMessage);
    }

    private String executeEncryptedMessageExchangeForActivation() {
        String message = EncryptedMessageService.buildFirstMessageForActivation(encapStorage);
        String decryptedResponseMessage = EncryptedMessageService.encryptAndSendMessage(client, message);

        encapClientHelper.updateEncapParamsActivation(decryptedResponseMessage);

        message = EncryptedMessageService.buildSecondMessageForActivation(encapStorage);
        decryptedResponseMessage = EncryptedMessageService.encryptAndSendMessage(client, message);

        return EncapUtils.getSamlObject(decryptedResponseMessage);
    }

    private String executeEncryptedMessageExchangeForAuthentication() {
        String message = EncryptedMessageService.buildFirstMessageForAuthentication(encapStorage);
        String decryptedResponseMessage = EncryptedMessageService.encryptAndSendMessage(client, message);

        encapClientHelper.updateEncapParamsAuthentication(encryptedMessageExchange, decryptedResponseMessage);

        message = EncryptedMessageService.buildSecondMessageForAuthentication(encapStorage);
        decryptedResponseMessage = EncryptedMessageService.encryptAndSendMessage(client, message);

        return EncapUtils.getSamlObject(decryptedResponseMessage);
    }

    // Final SOAP request in the session creation chain (activation of new device)
    private String finishMobileActivation(String activationSessionId, String samlObject) {
        if (encryptedMessageExchange) {
            String dataToSend = encapClientHelper.buildActivationCreateRequest(activationSessionId, samlObject);
            String response = postSoapMessage(EncapConstants.Urls.ACTIVATION_SERVICE, "\"\"", dataToSend);

            // Returns [securityToken, samUserId]. samUserId is not currently used, saving it in case needed in future
            List<String> securityValuesList = EncapUtils.getSecurityValuesList(response);
            encapClientHelper.saveSamUserId(securityValuesList.get(1));

            return securityValuesList.get(0);
        } else {
            String dataToSend = encapClientHelper.buildUserCreateRequest(activationSessionId, samlObject);
            String response = postSoapMessage(EncapConstants.Urls.USER_CREATE,
                    EncapConstants.HttpHeaders.USER_CREATE, dataToSend);

            return EncapUtils.getSecurityToken(response);
        }
    }

    // Final SOAP request in the session creation chain (authentication)
    private String finishMobileAuthenticationService(String samlObject) {
        if (encryptedMessageExchange) {
            String dataToSend = encapClientHelper.buildAuthServiceRequest(samlObject);
            String response = postSoapMessage(EncapConstants.Urls.AUTHENTICATION_SERVICE, "\"\"", dataToSend);

            // Returns [securityToken, samUserId]. samUserId is not currently used, saving it in case needed in future
            List<String> securityValuesList = EncapUtils.getSecurityValuesList(response);
            encapClientHelper.saveSamUserId(securityValuesList.get(1));

            return securityValuesList.get(0);
        } else {
            String dataToSend = encapClientHelper.buildAuthSessionReadRequest(samlObject);
            String response = postSoapMessage(EncapConstants.Urls.AUTHENTICATION_SESSION_READ_SERVICE,
                    EncapConstants.HttpHeaders.AUTHENTICATION_SESSION_READ, dataToSend);

            return EncapUtils.getSecurityToken(response);
        }
    }

    private String postSoapMessage(URL url, String soapAction, String body) {
        return client.request(url)
                .header("SOAPAction", soapAction)
                .type("text/xml; charset=utf-8")
                .accept(MediaType.WILDCARD)
                .post(String.class, body);
    }

    private RequestBuilder getPlainTextRequest() {
        return client.request(EncapConstants.Urls.PLAIN_TEXT_EXCHANGE)
                .accept(MediaType.WILDCARD)
                .type(MediaType.APPLICATION_FORM_URLENCODED);
    }
}
