package se.tink.backend.main.controllers;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang.NotImplementedException;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.dao.AuthenticationTokenDao;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.resources.UserEventHelper;
import se.tink.backend.core.Market;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserDeviceStatuses;
import se.tink.backend.core.UserEventTypes;
import se.tink.backend.core.auth.AuthenticationStatus;
import se.tink.backend.core.auth.AuthenticationToken;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.auth.validators.MarketAuthenticationMethodValidator;
import se.tink.backend.main.controllers.exceptions.InvalidSmsOtpStatusException;
import se.tink.backend.main.controllers.exceptions.UserNotFoundException;
import se.tink.backend.rpc.UpdatePin6Command;
import se.tink.backend.rpc.auth.AuthenticationResponse;
import se.tink.backend.rpc.auth.PhoneNumberAndPin6AuthenticationCommand;
import se.tink.backend.rpc.auth.ResetPin6Command;
import se.tink.backend.rpc.auth.SmsOtpAndPin6AuthenticationCommand;
import se.tink.backend.rpc.auth.UpdatePhoneNumberCommand;
import se.tink.backend.sms.otp.controllers.SmsOtpController;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpNotFoundException;
import se.tink.backend.sms.otp.rpc.ConsumeRequest;
import se.tink.backend.sms.otp.rpc.ConsumeResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.auth.AuthenticationMethod;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;
import se.tink.libraries.validation.exceptions.InvalidPin6Exception;
import se.tink.libraries.validation.validators.Pin6Validator;

public class PhoneNumberAndPin6AuthenticationServiceController {
    private static final MetricId UNAUTHORIZED_PASSWORD = MetricId.newId("unauthorized_users")
            .label("reason", "password");

    private static final LogUtils log = new LogUtils(PhoneNumberAndPin6AuthenticationServiceController.class);
    private Counter unauthorizedPasswordMeter;

    private final MarketServiceController marketServiceController;
    private final MarketAuthenticationMethodValidator authenticationMethodValidator;
    private final HashingAlgorithm hashingAlgorithm;
    private UserEventHelper userEventHelper;
    private AnalyticsController analyticsController;
    private UserSessionController userSessionController;
    private final AuthenticationTokenDao authenticationTokenDao;
    private final SmsOtpController smsOtpController;
    private final UserRepository userRepository;
    private final UserDeviceController userDeviceController;

    @Inject
    public PhoneNumberAndPin6AuthenticationServiceController(UserRepository userRepository,
             MarketServiceController marketServiceController, SmsOtpController smsOtpController,
             MarketAuthenticationMethodValidator authenticationMethodValidator,
             AuthenticationTokenDao authenticationTokenDao,
             AuthenticationConfiguration authenticationConfiguration,
             AnalyticsController analyticsController,
             UserSessionController userSessionController,
             UserEventHelper userEventHelper,
             MetricRegistry registry, UserDeviceController userDeviceController) {
        this.authenticationTokenDao = authenticationTokenDao;
        this.smsOtpController = smsOtpController;
        this.userRepository = userRepository;
        this.marketServiceController = marketServiceController;
        this.authenticationMethodValidator = authenticationMethodValidator;
        this.hashingAlgorithm = authenticationConfiguration.getUserPasswordHashAlgorithm();
        this.userEventHelper = userEventHelper;
        this.analyticsController = analyticsController;
        this.userSessionController = userSessionController;

        unauthorizedPasswordMeter = registry.meter(UNAUTHORIZED_PASSWORD);
        this.userDeviceController = userDeviceController;
    }

    /**
     * Create a authentication token based on SMS OTP and PIN6.
     */
    public AuthenticationResponse smsOtpAndPin6Authentication(SmsOtpAndPin6AuthenticationCommand command)
            throws InvalidPin6Exception {
        final Market market = getMarketOrDefault(command.getMarket());
        authenticationMethodValidator.validateForRegistration(market, AuthenticationMethod.SMS_OTP_AND_PIN6);

        AuthenticationToken.AuthenticationTokenBuilder builder = AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.SMS_OTP_AND_PIN6)
                .withClientKey(command.getClientKey())
                .withOAuth2ClientId(command.getOauthClientId());

        validateOtpAndPin6(builder, command);

        AuthenticationToken authenticationToken = authenticationTokenDao.save(builder.build());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAuthenticationToken(authenticationToken.getToken());
        authenticationResponse.setStatus(authenticationToken.getStatus());
        return authenticationResponse;
    }

    private void validateOtpAndPin6(AuthenticationToken.AuthenticationTokenBuilder builder,
            SmsOtpAndPin6AuthenticationCommand command) throws InvalidPin6Exception {
        ConsumeRequest request = new ConsumeRequest(command.getSmsOtpVerificationToken());

        try {
            final String phoneNumber = PhoneNumberUtils.normalize(
                    smsOtpController.getPhonenumberById(command.getSmsOtpVerificationToken()).orElse(null));

            User user = userRepository.findOneByUsername(phoneNumber);

            if (user == null) {
                Pin6Validator.validate(command.getPin6());
                String hashedPin6 = PasswordHash.create(command.getPin6(), hashingAlgorithm);

                consumeOtpAndThen(request, () -> {
                    builder.withStatus(AuthenticationStatus.NO_USER);
                    builder.withHashedPassword(hashedPin6);
                    builder.withUsername(phoneNumber);
                    builder.withAuthenticatedDeviceId(command.getDeviceId());
                }).ifPresent(builder::withStatus);
            } else {
                boolean authenticationSuccessful = validateAuthentication(user, command.getPin6());

                if (authenticationSuccessful) {
                    consumeOtpAndThen(request, () -> {
                        builder.withStatus(AuthenticationStatus.AUTHENTICATED);
                        builder.withUserId(user.getId());
                        builder.withUsername(user.getUsername());
                        builder.withAuthenticatedDeviceId(command.getDeviceId());
                        userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_SUCCESSFUL,
                                command.getRemoteAddress());
                    }).ifPresent(builder::withStatus);
                } else {
                    AuthenticationStatus status = authenticationFailed(user, command.getRemoteAddress());
                    builder.withStatus(status);

                    if (status == AuthenticationStatus.USER_BLOCKED) {
                        smsOtpController.consume(request);
                    }
                }
            }

        } catch (SmsOtpNotFoundException | InvalidPhoneNumberException e) {
            log.error("Could not create sms otp and pin6 authentication", e);
            builder.withStatus(AuthenticationStatus.AUTHENTICATION_ERROR);
        }
    }

    /**
     * Read a request for consumption of OTP and run a function on success
     *
     * Runs the Runnable onSuccess and returns Empty if successfully consumed OTP
     * otherwise return an optional with the status code
     */
    private Optional<AuthenticationStatus> consumeOtpAndThen(ConsumeRequest request,
            Runnable onSuccess) throws SmsOtpNotFoundException, InvalidPhoneNumberException {
        ConsumeResponse response = smsOtpController.consume(request);
        final String phoneNumber = PhoneNumberUtils.normalize(response.getPhoneNumber());

        switch (response.getResult()) {
        case CONSUMED:
            onSuccess.run();
            return Optional.empty();
        case INVALID_OTP_STATUS:
            log.error(
                    String.format("Sms otp does not have a valid status (Phone Number = '%s').", phoneNumber));
            break;
        case ALREADY_CONSUMED:
            log.error(String.format("Sms otp is already consumed (Phone Number = '%s').", phoneNumber));
            break;
        }

        return Optional.of(AuthenticationStatus.AUTHENTICATION_ERROR_UNAUTHORIZED_DEVICE);
    }

    private Market getMarketOrDefault(String market) {
        return !Strings.isNullOrEmpty(market) ?
                marketServiceController.getMarket(market) :
                marketServiceController.getDefaultMarket();
    }

    public AuthenticationResponse phoneNumberAndPin6Authentication(PhoneNumberAndPin6AuthenticationCommand command)
            throws UnauthorizedDeviceException {
        final Market market = getMarketOrDefault(command.getMarket());
        authenticationMethodValidator.validateForLogin(market, AuthenticationMethod.PHONE_NUMBER_AND_PIN6);

        AuthenticationToken.AuthenticationTokenBuilder tokenBuilder = AuthenticationToken.builder()
                .withMethod(AuthenticationMethod.PHONE_NUMBER_AND_PIN6)
                .withClientKey(command.getClientKey())
                .withOAuth2ClientId(command.getOauthClientId());

        User user = userRepository.findOneByUsername(command.getPhoneNumber());

        if (user == null) {
            tokenBuilder.withStatus(AuthenticationStatus.NO_USER);
        } else if (user.isBlocked()) {
            log.info(user.getId(), "Trying to authenticate a user that is BLOCKED"
                    + (command.getRemoteAddress().isPresent() ?
                    " from address '" + command.getRemoteAddress().get() + "'." :
                    ""));
            tokenBuilder.withStatus(AuthenticationStatus.USER_BLOCKED);
        } else {
            checkIfDeviceIsPinned(user, command.getUserDeviceId(), command.getUserAgent());

            boolean authenticationSuccessful = validateAuthentication(user, command.getPin6());

            if (authenticationSuccessful) {
                tokenBuilder.withUserId(user.getId());
                tokenBuilder.withStatus(AuthenticationStatus.AUTHENTICATED);
                userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_SUCCESSFUL, command.getRemoteAddress());
            } else {
                AuthenticationStatus status = authenticationFailed(user, command.getRemoteAddress().orElse(null));
                tokenBuilder.withStatus(status);
            }
        }

        AuthenticationToken token = authenticationTokenDao.save(tokenBuilder.build());

        AuthenticationResponse authenticationResponse = new AuthenticationResponse();
        authenticationResponse.setAuthenticationToken(token.getToken());
        authenticationResponse.setStatus(token.getStatus());
        return authenticationResponse;
    }

    private boolean validateAuthentication(User user, String pin6) {
        if (user.isBlocked()) {
            return false;
        }

        return PasswordHash.check(pin6, user.getHash(), hashingAlgorithm);
    }

    private AuthenticationStatus authenticationFailed(User user, String remoteAddress) {
        userEventHelper.save(user.getId(), UserEventTypes.AUTHENTICATION_ERROR, remoteAddress);

        unauthorizedPasswordMeter.inc();

        // Block the user because of too many failed login attempts
        if (userEventHelper.shouldBlockUser(user.getId())) {
            log.info(user.getId(), "Blocking user, too many sequential authentication errors");

            userEventHelper.save(user.getId(), UserEventTypes.BLOCKED, Optional.empty());

            user.setBlocked(true);
            userRepository.save(user);

            return AuthenticationStatus.USER_BLOCKED;
        }
        return AuthenticationStatus.AUTHENTICATION_ERROR;
    }

    public void updatePin6(User user, UpdatePin6Command command) throws InvalidPin6Exception {
        if (!PasswordHash.check(command.getOldPin6(), user.getHash(), Lists.newArrayList(hashingAlgorithm))) {
            throw new InvalidPin6Exception("Invalid pin6.");
        }

        user.setHash(PasswordHash.create(command.getNewPin6(), hashingAlgorithm));
        userRepository.save(user);

        userEventHelper.save(user.getId(), UserEventTypes.PIN6_CHANGED, command.getRemoteAddress());
        analyticsController.trackUserEvent(user, "user.update", command.getRemoteAddress());

        // Expire any other sessions.
        userSessionController.expireSessionsExcept(user.getId(), command.getSessionId());
    }

    public void updatePhoneNumber(UpdatePhoneNumberCommand command) throws SmsOtpNotFoundException,
            InvalidPhoneNumberException, InvalidSmsOtpStatusException, InvalidPin6Exception {
        final User user = command.getUser();

        // Verify PIN6 before the SMS OTP token since the token only can be consumed once.
        if (!PasswordHash.check(command.getPin6(), user.getHash(), Lists.newArrayList(hashingAlgorithm))) {
            throw new InvalidPin6Exception("Invalid pin6.");
        }

        ConsumeRequest request = new ConsumeRequest(command.getSmsOtpVerificationToken());

        ConsumeResponse response = smsOtpController.consume(request);

        final String phoneNumber = PhoneNumberUtils.normalize(response.getPhoneNumber());

        switch (response.getResult()) {

        case CONSUMED:
            user.setUsername(phoneNumber); // Update with the verified phone number
            userRepository.save(user);

            analyticsController.trackUserEvent(user, "user.change-phone-number", command.getRemoteAddress());

            // Expire any other sessions.
            userSessionController.expireSessionsExcept(user.getId(), command.getSessionId().orElse(null));
            return;
        case INVALID_OTP_STATUS:
            throw new InvalidSmsOtpStatusException(
                    String.format("Sms otp does not have a valid status (Phone Number = '%s').", phoneNumber));
        case ALREADY_CONSUMED:
            throw new InvalidSmsOtpStatusException(
                    String.format("Sms otp is already consumed (Phone Number = '%s').", phoneNumber));
        }
    }

    public AuthenticationResponse resetPin6(ResetPin6Command command) throws SmsOtpNotFoundException,
            InvalidSmsOtpStatusException, UserNotFoundException {

        ConsumeRequest request = new ConsumeRequest(command.getSmsOtpVerificationToken());

        ConsumeResponse consumeResponse = smsOtpController.consume(request);

        switch (consumeResponse.getResult()) {

        case CONSUMED:
            final String phoneNumber = consumeResponse.getPhoneNumber();

            User user = userRepository.findOneByUsername(phoneNumber);

            if (user == null) {
                throw new UserNotFoundException(String.format("User not found (UserName = '%s')", phoneNumber));
            }

            // Check if the call is made from an authorized device
            checkIfDeviceIsPinned(user, command.getUserDeviceId(), command.getUserAgent());

            user.setHash(PasswordHash.create(command.getPin6(), hashingAlgorithm));
            user.setBlocked(false);

            user = userRepository.save(user);

            // Expire all sessions.
            userSessionController.expireSessions(user.getId());

            // Log the reset.
            userEventHelper.save(user.getId(), UserEventTypes.PIN6_RESET, command.getRemoteAddress());
            analyticsController.trackUserEvent(user, "user.reset-pin6", command.getRemoteAddress());

            // Create a authentication token that is used for login
            AuthenticationToken authenticationToken = AuthenticationToken.builder()
                    .withMethod(AuthenticationMethod.PHONE_NUMBER_AND_PIN6)
                    .withClientKey(command.getClientKey())
                    .withOAuth2ClientId(command.getOauthClientId())
                    .withStatus(AuthenticationStatus.AUTHENTICATED)
                    .withUserId(user.getId())
                    .build();

            authenticationToken = authenticationTokenDao.save(authenticationToken);

            AuthenticationResponse response = new AuthenticationResponse();
            response.setAuthenticationToken(authenticationToken.getToken());
            response.setStatus(authenticationToken.getStatus());

            return response;
        case INVALID_OTP_STATUS:
            throw new InvalidSmsOtpStatusException("Sms otp does not have a valid status.");
        case ALREADY_CONSUMED:
            throw new InvalidSmsOtpStatusException("Sms otp is already consumed.");
        default:
            throw new NotImplementedException();
        }
    }

    private void checkIfDeviceIsPinned(User user, String deviceId, String userAgent)
            throws UnauthorizedDeviceException {
        if (Strings.isNullOrEmpty(deviceId)) {
            throw new UnauthorizedDeviceException("");
        }

        UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, deviceId, userAgent);
        if (userDevice == null) {
            throw new UnauthorizedDeviceException("");
        }
        if (!Objects.equals(userDevice.getStatus(), UserDeviceStatuses.AUTHORIZED)) {
            throw new UnauthorizedDeviceException("");
        }
    }
}
