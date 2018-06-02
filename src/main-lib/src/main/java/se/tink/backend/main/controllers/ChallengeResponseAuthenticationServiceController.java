package se.tink.backend.main.controllers;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.IOException;
import java.security.interfaces.ECPublicKey;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.common.dao.AuthenticationTokenDao;
import se.tink.backend.common.repository.mysql.main.UserAuthenticationChallengeRepository;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.common.repository.mysql.main.UserPublicKeyRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.UserEventHelper;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserEventTypes;
import se.tink.backend.core.UserPublicKey;
import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.core.auth.UserAuthenticationChallenge;
import se.tink.backend.core.auth.UserPublicKeyType;
import se.tink.backend.core.exceptions.AuthenticationKeyNotFoundException;
import se.tink.backend.core.exceptions.AuthenticationTokenExpiredException;
import se.tink.backend.core.exceptions.AuthenticationTokenNotFoundException;
import se.tink.backend.core.exceptions.ChallengeExpiredException;
import se.tink.backend.core.exceptions.ChallengeNotFoundException;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.validators.MarketAuthenticationMethodValidator;
import se.tink.backend.main.utils.ChallengeVerificationUtils;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.abnamro.SignedChallengeAuthenticationCommand;
import se.tink.backend.rpc.auth.keys.AuthenticationKeyResponse;
import se.tink.backend.rpc.auth.keys.DeleteAuthenticationKeyCommand;
import se.tink.backend.rpc.auth.keys.StoreAuthenticationKeyCommand;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.auth.ChallengeStatus;
import se.tink.libraries.auth.utils.ChallengeGenerator;
import se.tink.libraries.cryptography.ECDSAUtils;
import se.tink.libraries.cryptography.JWTUtils;

public class ChallengeResponseAuthenticationServiceController {
    private static final LogUtils log = new LogUtils(ChallengeResponseAuthenticationServiceController.class);

    private final AuthenticationTokenDao authenticationTokenDao;
    private final MarketAuthenticationMethodValidator marketAuthenticationMethodValidator;
    private final MarketServiceController marketServiceController;
    private final UserPublicKeyRepository userPublicKeyRepository;
    private final UserDeviceRepository userDeviceRepository;
    private final UserDeviceController userDeviceController;
    private final UserAuthenticationChallengeRepository userAuthenticationChallengeRepository;
    private final UserRepository userRepository;
    private final UserEventHelper userEventHelper;

    @Inject
    public ChallengeResponseAuthenticationServiceController(
            AuthenticationTokenDao authenticationTokenDao,
            MarketAuthenticationMethodValidator marketAuthenticationMethodValidator,
            MarketServiceController marketServiceController,
            UserPublicKeyRepository userPublicKeyRepository,
            UserDeviceRepository userDeviceRepository,
            UserDeviceController userDeviceController,
            UserAuthenticationChallengeRepository userAuthenticationChallengeRepository,
            UserRepository userRepository, UserEventHelper userEventHelper) {
        this.authenticationTokenDao = authenticationTokenDao;
        this.marketAuthenticationMethodValidator = marketAuthenticationMethodValidator;
        this.marketServiceController = marketServiceController;
        this.userPublicKeyRepository = userPublicKeyRepository;
        this.userDeviceRepository = userDeviceRepository;
        this.userDeviceController = userDeviceController;
        this.userAuthenticationChallengeRepository = userAuthenticationChallengeRepository;
        this.userRepository = userRepository;
        this.userEventHelper = userEventHelper;
    }

    public List<AuthenticationKeyResponse> listAuthenticationKeys(String userId) {
        return userPublicKeyRepository.findByUserId(userId).stream()
                .filter(UserPublicKey::isActive)
                .map(AuthenticationKeyResponse::new)
                .collect(Collectors.toList());
    }

    public String storeAuthenticationKey(StoreAuthenticationKeyCommand command)
            throws AuthenticationTokenNotFoundException, AuthenticationTokenExpiredException {
        // TODO Support RSA
        if (command.getType() != UserPublicKeyType.ECDSA) {
            throw new UnsupportedOperationException("ECDSA is the only supported key type.");
        }

        AuthenticationToken authenticationToken = authenticationTokenDao.consume(command.getAuthenticationToken());

        UserPublicKey key = createECKey(authenticationToken.getUserId(), command.getDeviceId(), command.getKey(),
                command.getSource(), command.getType());

        userPublicKeyRepository.save(key);
        return key.getId();
    }

    public void deleteAuthenticationKey(DeleteAuthenticationKeyCommand command) {
        // Fetch the public key from the database first to verify that the user is the legitimate owner of the key
        userPublicKeyRepository.findOptionalByIdAndUserId(command.getKeyId(), command.getUserId())
                .ifPresent(userPublicKeyRepository::delete);
    }

    public String createAuthenticationChallenge(String keyId) throws AuthenticationKeyNotFoundException {
        UserPublicKey userPublicKey = userPublicKeyRepository.findOne(keyId);

        if (userPublicKey == null || !userPublicKey.isActive()) {
            throw new AuthenticationKeyNotFoundException();
        }

        String userId = userPublicKey.getUserId();
        UserAuthenticationChallenge challenge = createChallenge(userId, keyId);

        challenge = userAuthenticationChallengeRepository.save(challenge);

        return challenge.getChallenge();
    }

    public AuthenticationResponse signedChallengeAuthentication(SignedChallengeAuthenticationCommand command)
            throws ChallengeNotFoundException, ChallengeExpiredException {
        Market market = getMarketOrDefault(command.getMarket());
        marketAuthenticationMethodValidator.validateForLogin(market, AuthenticationMethod.CHALLENGE_RESPONSE);
        String challengeId = JWTUtils.readChallenge(command.getToken());
        UserAuthenticationChallenge challenge = userAuthenticationChallengeRepository.findOne(challengeId);

        if (challenge == null) {
            throw new ChallengeNotFoundException();
        }
        challenge = consumeChallenge(challenge);

        if (challenge.getStatus() == ChallengeStatus.EXPIRED) {
            throw new ChallengeExpiredException();
        }

        AuthenticationStatus status = AuthenticationStatus.AUTHENTICATION_ERROR;
        String userId = null;

        UserPublicKey userPublicKey = userPublicKeyRepository.findOne(challenge.getKeyId());

        if (userPublicKey == null) {
            log.warn(String.format("Could not find public key with ID: %s", challenge.getKeyId()));
        } else {
            User user = userRepository.findOne(userPublicKey.getUserId());
            if (user == null) {
                log.warn(String.format("Public key without valid user found: %s.", userPublicKey.getId()));
                status = AuthenticationStatus.NO_USER;
            } else if (user.isBlocked()) {
                log.info(user.getId(), "User is blocked. Refusing login.");
                status = AuthenticationStatus.USER_BLOCKED;
            } else if (!Objects.equals(userPublicKey.getDeviceId(), command.getUserDeviceId())) {
                log.info(user.getId(), "Given device id didn't match expected.");
                status = authenticationFailed(user, command.getRemoteAddress());
            } else {
                userDeviceController.checkIfDeviceIsAuthorized(user, command.getUserDeviceId(), command.getUserAgent());

                try {
                    ECPublicKey ecPublicKey = ECDSAUtils.getPublicKey(userPublicKey.getPublicKey());
                    status = ChallengeVerificationUtils.verifySignedChallenge(user, command.getToken(), challenge,
                            ecPublicKey);
                } catch (IOException e) {
                    log.error(user.getId(), "Failed to use the stored public key to verify token.", e);
                }

                if (status == AuthenticationStatus.AUTHENTICATED) {
                    userId = userPublicKey.getUserId();
                    userEventHelper
                            .save(userId, UserEventTypes.AUTHENTICATION_SUCCESSFUL, command.getRemoteAddress());
                } else {
                    status = authenticationFailed(user, command.getRemoteAddress());
                }
            }
        }

        AuthenticationToken authenticationToken = createAuthenticationToken(status, userId, command.getClientKey(),
                command.getOauthClientId());
        authenticationToken = authenticationTokenDao.save(authenticationToken);

        AuthenticationResponse response = new AuthenticationResponse();
        response.setStatus(status);
        response.setAuthenticationToken(authenticationToken.getToken());
        return response;
    }

    @VisibleForTesting
    UserPublicKey createECKey(String userId, String deviceId, String publicKey, AuthenticationSource source,
            UserPublicKeyType type) {
        Preconditions.checkState(type == UserPublicKeyType.ECDSA);
        Preconditions.checkState(!Strings.isNullOrEmpty(userId));
        Preconditions.checkState(!Strings.isNullOrEmpty(deviceId));
        Preconditions.checkState(!Strings.isNullOrEmpty(publicKey));

        UserDevice userDevice = userDeviceRepository.findOneByUserIdAndDeviceId(userId, deviceId);
        if (userDevice == null) {
            throw new UnauthorizedDeviceException("Could not find valid device binding.");
        }

        if (!ECDSAUtils.isValidPublicKey(publicKey)) {
            throw new IllegalArgumentException("Public key is not a valid ECDSA key.");
        }

        UserPublicKey key = new UserPublicKey();
        key.setUserId(userId);
        key.setDeviceId(deviceId);
        key.setPublicKey(publicKey);
        key.setAuthenticationSource(source);
        key.setType(type);
        key.setActive(true);

        return key;
    }

    @VisibleForTesting
    UserAuthenticationChallenge createChallenge(String userId, String keyId) {
        Preconditions.checkNotNull(userId);
        UserAuthenticationChallenge.Builder challengeBuilder = UserAuthenticationChallenge.create();
        ChallengeGenerator generator = new ChallengeGenerator();

        challengeBuilder.withKeyId(keyId);
        challengeBuilder.withUserId(userId);
        challengeBuilder.withChallenge(generator.getRandomChallenge());

        return challengeBuilder.build();
    }

    @VisibleForTesting
    AuthenticationToken createAuthenticationToken(AuthenticationStatus status, String userId, String clientKey,
            String OauthClientId) {
        AuthenticationToken.AuthenticationTokenBuilder builder = AuthenticationToken.builder();
        builder.withMethod(AuthenticationMethod.CHALLENGE_RESPONSE)
                .withClientKey(clientKey)
                .withOAuth2ClientId(OauthClientId)
                .withStatus(status);
        if (status == AuthenticationStatus.AUTHENTICATED) {
            builder.withUserId(userId);
        }
        return builder.build();
    }

    private UserAuthenticationChallenge consumeChallenge(UserAuthenticationChallenge challenge) {
        Preconditions.checkNotNull(challenge);
        return userAuthenticationChallengeRepository.save(challenge.consume());
    }

    private AuthenticationStatus authenticationFailed(User user, Optional<String> remoteAddress) {
        userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_ERROR, remoteAddress);

        // Block the user because of too many failed login attempts
        if (userEventHelper.shouldBlockUser(user.getId())) {
            log.info(user.getId(), "Blocking user, too many sequential authentication errors");

            userEventHelper.save(user.getId(), UserEventTypes.BLOCKED, remoteAddress);

            user.setBlocked(true);
            userRepository.save(user);

            return AuthenticationStatus.USER_BLOCKED;
        }
        return AuthenticationStatus.AUTHENTICATION_ERROR;
    }

    private Market getMarketOrDefault(String market) {
        return !Strings.isNullOrEmpty(market) ?
                marketServiceController.getMarket(market) :
                marketServiceController.getDefaultMarket();
    }
}
