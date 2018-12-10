package se.tink.backend.aggregation.agents.utils.authentication.encap;

import java.util.Map;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationSecondRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationSecondRequestBody;

public class PlainTextMessageService implements EncapMessageService {
    private final EncapClientHelper encapClientHelper;
    private final Map<String, String> encapStorage;

    private PlainTextMessageService(EncapClientHelper encapClientHelper, Map<String, String> encapStorage) {
        this.encapClientHelper = encapClientHelper;
        this.encapStorage = encapStorage;
    }

    public static PlainTextMessageService build(EncapClientHelper encapClientHelper,
            Map<String, String> encapStorage) {
        return new PlainTextMessageService(encapClientHelper, encapStorage);
    }

    @Override
    public String executeActivationExchange() {
        ActivationFirstRequestBody firstRequestBody = createActivationFirstRequestBody(encapStorage);
        String responseMessage = encapClientHelper.getPlainTextRequest().post(String.class, firstRequestBody);

        encapClientHelper.updateEncapParamsActivation(responseMessage);

        ActivationSecondRequestBody secondRequestBody = createActivationSecondRequestBody(encapStorage);
        responseMessage = encapClientHelper.getPlainTextRequest().post(String.class, secondRequestBody);

        return EncapUtils.getSamlObject(responseMessage);
    }

    @Override
    public String finishActivation(String activationSessionId, String samlObject) {
        String dataToSend = encapClientHelper.buildUserCreateRequest(activationSessionId, samlObject);
        String response = encapClientHelper.postSoapMessage(EncapConstants.Urls.USER_CREATE,
                EncapConstants.HttpHeaders.USER_CREATE, dataToSend);

        return EncapUtils.getSecurityToken(response);
    }

    @Override
    public String executeAuthenticationExchange() {
        AuthenticationFirstRequestBody firstRequestBody = createAuthenticationFirstRequestBody(encapStorage);
        String responseMessage = encapClientHelper.getPlainTextRequest().post(String.class, firstRequestBody);
        if (!encapClientHelper.updateEncapParamsAuthentication(false, responseMessage)){
            return null;
        }

        AuthenticationSecondRequestBody secondRequestBody = createAuthenticationSecondRequestBody(encapStorage);
        responseMessage = encapClientHelper.getPlainTextRequest().post(String.class, secondRequestBody);

        return EncapUtils.getSamlObject(responseMessage);
    }

    @Override
    public String finishAuthentication(String samlObject) {
        String dataToSend = encapClientHelper.buildAuthSessionReadRequest(samlObject);
        String response = encapClientHelper.postSoapMessage(EncapConstants.Urls.AUTHENTICATION_SESSION_READ_SERVICE,
                EncapConstants.HttpHeaders.AUTHENTICATION_SESSION_READ, dataToSend);

        return EncapUtils.getSecurityToken(response);
    }

    private ActivationFirstRequestBody createActivationFirstRequestBody(Map<String, String> encapStorage) {
        return new ActivationFirstRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION)
        );
    }

    private ActivationSecondRequestBody createActivationSecondRequestBody(Map<String, String> encapStorage) {
        return new ActivationSecondRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_AUTHENTICATION_KEY),
                encapStorage.get(EncapConstants.Storage.B64_AUTHENTICATION_KEY_WITHOUT_PIN),
                encapStorage.get(EncapConstants.Storage.B64_CHALLENGE_RESPONSE),
                encapStorage.get(EncapConstants.Storage.B64_CHALLENGE_RESPONSE_WITHOUT_PIN),
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION),
                encapStorage.get(EncapConstants.Storage.REGISTRATION_ID),
                encapStorage.get(EncapConstants.Storage.B64_SALT_HASH)
        );
    }

    private AuthenticationFirstRequestBody createAuthenticationFirstRequestBody(Map<String, String> encapStorage) {
        return new AuthenticationFirstRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION),
                encapStorage.get(EncapConstants.Storage.REGISTRATION_ID)
        );
    }

    private AuthenticationSecondRequestBody createAuthenticationSecondRequestBody(Map<String, String> encapStorage) {
        return new AuthenticationSecondRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_RESPONSE_CURRENT),
                encapStorage.get(EncapConstants.Storage.B64_RESPONSE_CURRENT_WITHOUT_PIN),
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION),
                encapStorage.get(EncapConstants.Storage.REGISTRATION_ID),
                encapStorage.get(EncapConstants.Storage.B64_SALT_HASH)
        );
    }
}
