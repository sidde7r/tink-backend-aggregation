package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class AnonymousInvokeBindRequestData {

    private CollectionResultEntity collectionResult;
    private ParamsEntity params;
    private String policyRequestId;
    private PublicKeyEntity encryptionPublicKey;
    private PublicKeyEntity publicKey;

    public AnonymousInvokeBindRequestData(
            String deviceId, String deviceName, ParamsEntity params, String policyRequestId) {
        this.collectionResult = new CollectionResultEntity(deviceId, deviceName);
        this.params = params;
        this.policyRequestId = policyRequestId;
    }

    public AnonymousInvokeBindRequestData(
            String deviceId,
            String deviceName,
            ParamsEntity params,
            String encodedEncryptionPublicKey,
            String encodedPublicKey) {
        this.collectionResult = new CollectionResultEntity(deviceId, deviceName);
        this.params = params;
        this.encryptionPublicKey = new PublicKeyEntity(encodedEncryptionPublicKey, "rsa");
        this.publicKey = new PublicKeyEntity(encodedPublicKey, "ec");
    }

    public static AnonymousInvokeBindRequestData createAnonymousInvokeData(
            String deviceId, String deviceName, String paramsSessionId) {
        ParamsEntity params =
                new ParamsEntity()
                        .withSessionId(paramsSessionId)
                        .withTransactionType("mobile-registration");
        return new AnonymousInvokeBindRequestData(
                deviceId, deviceName, params, "mobile-start-registration");
    }

    public static AnonymousInvokeBindRequestData createBindData(
            String deviceId,
            String deviceName,
            ParamsEntity params,
            String encodedEncryptionPublicKey,
            String encodedPublicKey) {
        return new AnonymousInvokeBindRequestData(
                deviceId, deviceName, params, encodedEncryptionPublicKey, encodedPublicKey);
    }

    public static AnonymousInvokeBindRequestData createLoginData(
            String deviceId, String deviceName, ParamsEntity params) {
        return new AnonymousInvokeBindRequestData(deviceId, deviceName, params, "mobile-login");
    }
}
