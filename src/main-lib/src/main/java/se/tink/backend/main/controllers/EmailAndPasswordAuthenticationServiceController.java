package se.tink.backend.main.controllers;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.auth.BasicAuthenticationDetails;
import se.tink.backend.common.cache.CacheClient;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.mail.MailSender;
import se.tink.backend.common.mail.MailTemplate;
import se.tink.backend.common.repository.mysql.main.UserForgotPasswordTokenRepository;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.repository.mysql.main.UserStateRepository;
import se.tink.backend.common.resources.UserEventHelper;
import se.tink.backend.common.utils.LogUtils;
import se.tink.backend.core.User;
import se.tink.backend.core.UserEventTypes;
import se.tink.backend.core.UserForgotPasswordToken;
import se.tink.backend.main.auth.session.UserSessionController;
import se.tink.backend.main.utils.ForgotPasswordHelper;
import se.tink.backend.rpc.ForgotPasswordCommand;
import se.tink.backend.rpc.ResetPasswordCommand;
import se.tink.backend.rpc.UpdatePasswordCommand;
import se.tink.backend.rpc.UpdateEmailCommand;
import se.tink.backend.utils.StringUtils;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;

public class EmailAndPasswordAuthenticationServiceController {
    private static final LogUtils log = new LogUtils(EmailAndPasswordAuthenticationServiceController.class);

    private final AnalyticsController analyticsController;
    private final UserSessionController userSessionController;

    private final ForgotPasswordHelper forgotPasswordHelper;
    private final UserEventHelper userEventHelper;
    private final MailSender mailSender;

    private final UserRepository userRepository;
    private final UserForgotPasswordTokenRepository forgotPasswordTokenRepository;
    private final HashingAlgorithm hashingAlgorithm;

    private final CacheClient cacheClient;
    private final UserStateRepository userStateRepository;

    @Inject
    public EmailAndPasswordAuthenticationServiceController(
            AnalyticsController analyticsController,
            UserSessionController userSessionController,
            ForgotPasswordHelper forgotPasswordHelper,
            UserEventHelper userEventHelper, MailSender mailSender,
            UserRepository userRepository,
            UserForgotPasswordTokenRepository forgotPasswordTokenRepository,
            AuthenticationConfiguration authenticationConfiguration,
            CacheClient cacheClient,
            UserStateRepository userStateRepository) {
        this.analyticsController = analyticsController;
        this.userSessionController = userSessionController;
        this.forgotPasswordHelper = forgotPasswordHelper;
        this.userEventHelper = userEventHelper;
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.forgotPasswordTokenRepository = forgotPasswordTokenRepository;
        this.hashingAlgorithm = authenticationConfiguration.getUserPasswordHashAlgorithm();
        this.cacheClient = cacheClient;
        this.userStateRepository = userStateRepository;
    }

    public void forgotPassword(ForgotPasswordCommand command, MailTemplate template) throws NoSuchElementException {
        String username = StringUtils.trim(command.getUsername());

        User user = userRepository.findOneByUsername(username);

        if (user == null) {
            throw new NoSuchElementException("Could not find user in forgot password");
        }

        analyticsController.trackUserEvent(user, "user.forgot-password", command.getRemoteAddress());

        forgotPasswordHelper.forgotPassword(user, template, command.getRemoteAddress(), command.getUserAgent());
    }

    public void resetPassword(ResetPasswordCommand command) throws NoSuchElementException {
        UserForgotPasswordToken token = forgotPasswordTokenRepository.findOne(command.getTokenId());

        if (token == null) {
            log.error("Could not find reset token: " + command.getTokenId());
            throw new NoSuchElementException();
        }

        forgotPasswordTokenRepository.delete(token);

        User user = userRepository.findOne(token.getUserId());
        BasicAuthenticationDetails authenticationDetails = new BasicAuthenticationDetails(user.getUsername(),
                command.getPassword());

        user.setHash(authenticationDetails.getHashedPassword(hashingAlgorithm));
        user.setBlocked(false);

        userRepository.save(user);

        // Expire any existing sessions.

        userSessionController.expireSessions(token.getUserId());

        // Log the reset.

        userEventHelper.save(user.getId(), UserEventTypes.PASSWORD_RESET, command.getRemoteAddress());
        analyticsController.trackUserEvent(user, "user.reset-password", command.getRemoteAddress());
    }

    public void updatePassword(User user, UpdatePasswordCommand command) {
        if (!PasswordHash.check(command.getOldPassword(), user.getHash(), Lists.newArrayList(hashingAlgorithm))) {
            throw new IllegalArgumentException("Invalid old password.");
        }

        user.setHash(PasswordHash.create(command.getNewPassword(), hashingAlgorithm));
        userRepository.save(user);

        userEventHelper.save(user.getId(), UserEventTypes.PASSWORD_CHANGED, command.getRemoteAddress());
        analyticsController.trackUserEvent(user, "user.update", command.getRemoteAddress());

        // Expire any other sessions.

        userSessionController.expireSessionsExcept(user.getId(), command.getSessionId());

        // Send mail notifying the user that someone has updated his information. Not checking subscription settings
        // here because this is security related.

        mailSender.sendMessageWithTemplate(user, MailTemplate.INFORM_USER_CHANGED);
    }

    public User updateEmail(User user, UpdateEmailCommand updateEmailCommand,
            Optional<String> remoteAddress) {
        updateUserProfileProperties(user, updateEmailCommand);
        userRepository.save(user);

        analyticsController.trackUserEvent(user, "user.update-profile", remoteAddress);
        userStateRepository.updateContextTimestampByUserId(user.getId(), cacheClient);

        return user;
    }

    private void updateUserProfileProperties(User user, UpdateEmailCommand command) {
        if (isModifiedField(command.getUsername(), user.getUsername())) {
            user.setUsername(command.getUsername());
        }
    }

    private <T> boolean isModifiedField(T newField, T oldField) {
        return newField != null && !Objects.equals(newField, oldField);
    }
}
