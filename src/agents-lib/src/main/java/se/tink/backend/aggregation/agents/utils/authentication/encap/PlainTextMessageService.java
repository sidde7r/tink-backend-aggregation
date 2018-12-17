package se.tink.backend.aggregation.agents.utils.authentication.encap;

import java.util.Map;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.ActivationSecondRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationFirstRequestBody;
import se.tink.backend.aggregation.agents.utils.authentication.encap.rpc.plaintext.AuthenticationSecondRequestBody;

public class PlainTextMessageService {

    static ActivationFirstRequestBody createActivationFirstRequestBody(Map<String, String> encapStorage) {
        return new ActivationFirstRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION)
        );
    }

    static ActivationSecondRequestBody createActivationSecondRequestBody(Map<String, String> encapStorage) {
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

    static AuthenticationFirstRequestBody createAuthenticationFirstRequestBody(Map<String, String> encapStorage) {
        return new AuthenticationFirstRequestBody(
                encapStorage.get(EncapConstants.Storage.B64_DEVICE_HASH),
                encapStorage.get(EncapConstants.Storage.DEVICE_UUID),
                encapStorage.get(EncapConstants.Storage.APPLICATION_VERSION),
                encapStorage.get(EncapConstants.Storage.ENCAP_API_VERSION),
                encapStorage.get(EncapConstants.Storage.REGISTRATION_ID)
        );
    }

    static AuthenticationSecondRequestBody createAuthenticationSecondRequestBody(Map<String, String> encapStorage) {
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
