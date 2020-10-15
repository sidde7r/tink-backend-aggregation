package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngDirectApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngStorage;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementRequest;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.rpc.KeyAgreementResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.DerivedKeyOutput;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.crypto.IngCryptoUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.AbstractAuthenticationStep;

public class KeyAgreementStep extends AbstractAuthenticationStep {

    private final IngDirectApiClient ingDirectApiClient;
    private final IngStorage ingStorage;
    private final IngCryptoUtils ingCryptoUtils;

    public KeyAgreementStep(IngConfiguration ingConfiguration) {
        this.ingDirectApiClient = ingConfiguration.getIngDirectApiClient();
        this.ingStorage = ingConfiguration.getIngStorage();
        this.ingCryptoUtils = ingConfiguration.getIngCryptoUtils();
    }

    @Override
    public AuthenticationStepResponse execute(AuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {

        if (!ingStorage.wasEnrolled()) {
            exchangeKeys();

            return AuthenticationStepResponse.executeNextStep();
        } else {
            // goto auto
            return AuthenticationStepResponse.executeStepWithId(DeviceAgreementStep.STEP_ID);
        }
    }

    public void exchangeKeys() {
        KeyPair keyPair = generateAndStoreClientKeys();

        KeyAgreementRequest request =
                KeyAgreementRequest.builder()
                        .clientId(IngConstants.CLIENT_ID)
                        .clientNonce(ingCryptoUtils.getBase64RandomBytes(64))
                        .clientPublicKey(
                                Base64.getEncoder()
                                        .encodeToString(keyPair.getPublic().getEncoded()))
                        .serverSigningKeyId(IngConstants.SERVER_SIGNING_KEY_ID)
                        .build();

        KeyAgreementResponse keyAgreementResponse = ingDirectApiClient.bootstrapKeys(request);

        String accessToken = keyAgreementResponse.getAccessTokens().getAccessToken();

        ingStorage.storeAccessToken(accessToken);

        PublicKey serverPublicKey =
                ingCryptoUtils.getPublicKeyFromBase64(keyAgreementResponse.getServerPublicKey());
        generateAndStoreDerivedKeys(
                keyPair.getPrivate(), serverPublicKey, keyAgreementResponse.getServerNonce());
    }

    private KeyPair generateAndStoreClientKeys() {
        KeyPair keyPair = ingCryptoUtils.generateKeys();
        ingStorage.storeClientPrivateKey(keyPair.getPrivate());
        ingStorage.storeClientPublicKey(keyPair.getPublic());
        return keyPair;
    }

    private void generateAndStoreDerivedKeys(
            PrivateKey clientPrivateKey, PublicKey serverPublicKey, String serverNonce) {
        byte[] salt = Base64.getDecoder().decode(serverNonce);
        byte[] context = IngConstants.CLIENT_ID.getBytes();
        DerivedKeyOutput derivedKeyOutput =
                ingCryptoUtils.deriveKeys(clientPrivateKey, serverPublicKey, salt, context);

        ingStorage.storeEncryptionKey(derivedKeyOutput.getEncryptionKey());
        ingStorage.storeSigningKey(derivedKeyOutput.getSigningKey());
    }
}
