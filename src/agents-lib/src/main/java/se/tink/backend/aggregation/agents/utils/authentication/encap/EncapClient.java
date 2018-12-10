package se.tink.backend.aggregation.agents.utils.authentication.encap;

import java.util.Map;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class EncapClient {
    private TinkHttpClient client;
    private EncapClientHelper encapClientHelper;
    private Map<String, String> encapStorage;
    private final PersistentStorage persistentStorage;
    private final EncapMessageService messageService;

    public EncapClient(EncapConfiguration configuration, PersistentStorage persistentStorage,
            TinkHttpClient client, boolean encryptedMessageExchange, String username) {
        this.client = client;
        this.persistentStorage = persistentStorage;

        String encapStorageString = persistentStorage.get(EncapConstants.Storage.ENCAP_STORAGE);
        encapClientHelper = new EncapClientHelper(client, username, encapStorageString, persistentStorage, configuration);
        encapStorage = encapClientHelper.getEncapStorage();

        this.messageService = encryptedMessageExchange ?
                EncryptedMessageService.build(encapClientHelper, encapStorage) :
                PlainTextMessageService.build( encapClientHelper, encapStorage);
    }

    public String activateAndAuthenticateUser(String activationCode) {
        createAuthenticationSession();
        String activationSessionId = updateActivationSession(activationCode);
        String samlObject = messageService.executeActivationExchange();

        return messageService.finishActivation(activationSessionId, samlObject);
    }

    public String authenticateUser() {

        encapClientHelper.populateEncapStorage(persistentStorage.get(EncapConstants.Storage.ENCAP_STORAGE));
        encapStorage = encapClientHelper.getEncapStorage();
        createAuthenticationSession();
        String samlObject = messageService.executeAuthenticationExchange();

        if (samlObject == null){
            return null;
        }

        return messageService.finishAuthentication(samlObject);
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

        String response = encapClientHelper.postSoapMessage(EncapConstants.Urls.ACTIVATION_SESSION_UPDATE,
                EncapConstants.HttpHeaders.ACTIVATION_SESSION_UPDATE, dataToSend);

        return EncapUtils.getActivationSessionId(response);
    }
}
