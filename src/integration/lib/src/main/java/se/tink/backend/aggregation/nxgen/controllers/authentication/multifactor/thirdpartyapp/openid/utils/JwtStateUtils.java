package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import com.google.common.base.Strings;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.configuration.CallbackJwtSignatureKeyPair;
import se.tink.libraries.cryptography.ECDSAUtils;
import se.tink.libraries.cryptography.JWTUtils;

public class JwtStateUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtStateUtils.class);

    /**
     * Introduce complex/structured data on the state The state is then passed along by the bank
     * through the callback uri. We use this complex dataset to be able to introduce more details on
     * the user/client. We used the Elliptic Curve algorithm in order to reduce the size of the
     * actual JWToken signature.
     *
     * @return signed jwt state or pseudoId
     */
    public static String tryCreateJwtState(
            final CallbackJwtSignatureKeyPair callbackJWTSignatureKeyPair,
            final String pseudoId,
            final String appUriId) {
        if (callbackJWTSignatureKeyPair == null || !callbackJWTSignatureKeyPair.isEnabled()) {
            logger.info("Callback JWT not enabled, using pseudoId as state. State: {}", pseudoId);
            return pseudoId;
        }

        ECPublicKey publicKey =
                ECDSAUtils.getPublicKeyByPath(callbackJWTSignatureKeyPair.getPublicKeyPath());
        ECPrivateKey privateKey =
                ECDSAUtils.getPrivateKeyByPath(callbackJWTSignatureKeyPair.getPrivateKeyPath());
        String signedState = JWTUtils.createJWTState(publicKey, privateKey, pseudoId, appUriId);

        logger.info("JWT state: {}", signedState);
        return signedState;
    }

    public static String generatePseudoId(String appUriId) {
        if (Strings.isNullOrEmpty(appUriId)) {
            logger.warn("appUriId is empty!");
            return RandomUtils.generateRandomHexEncoded(8);
        }

        return appUriId;
    }
}
