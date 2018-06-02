package se.tink.backend.main.utils;

import com.google.common.base.Preconditions;
import java.security.interfaces.ECPublicKey;
import se.tink.backend.core.User;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.UserAuthenticationChallenge;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.cryptography.JWTUtils;
import se.tink.libraries.cryptography.VerificationStatus;

public class ChallengeVerificationUtils {
    private static final LogUtils log = new LogUtils(ChallengeVerificationUtils.class);

    public static AuthenticationStatus verifySignedChallenge(User user, String token,
            UserAuthenticationChallenge challenge, ECPublicKey publicKey) {
        Preconditions.checkNotNull(user);
        switch (challenge.getStatus()) {
        case CONSUMED:
            VerificationStatus verificationStatus = JWTUtils.verify(challenge.getChallenge(), token, publicKey);
            return fromVerificationStatusToAuthenticationStatus(user, verificationStatus);
        case EXPIRED:
            log.info(user.getId(), "Challenge has expired.");
            return AuthenticationStatus.AUTHENTICATION_ERROR;
        case VALID:
            log.info(user.getId(), "Challenge must be consumed before being verified.");
            return AuthenticationStatus.AUTHENTICATION_ERROR;
        case INVALID:
        default:
            log.info(user.getId(), "Challenge has already been consumed.");
            return AuthenticationStatus.AUTHENTICATION_ERROR;
        }
    }

    public static AuthenticationStatus fromVerificationStatusToAuthenticationStatus(User user,
            VerificationStatus status) {
        switch (status) {
        case VALID:
            return AuthenticationStatus.AUTHENTICATED;
        case EXPIRED:
            log.info(user.getId(), "Device generated token has expired. Refusing token.");
            return AuthenticationStatus.AUTHENTICATION_ERROR;
        case INVALID:
        default:
            log.info(user.getId(), "Remote token could not be verified.");
            return AuthenticationStatus.AUTHENTICATION_ERROR;
        }
    }
}
