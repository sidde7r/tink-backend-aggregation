package se.tink.backend.main.controllers.abnamro;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.Map;
import org.apache.commons.lang.NotImplementedException;
import se.tink.backend.abnamro.migration.AbnAmroCredentialsMigrator;
import se.tink.backend.common.config.AuthenticationConfiguration;
import se.tink.backend.common.controllers.AnalyticsController;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.main.auth.UserDeviceController;
import se.tink.backend.main.controllers.abnamro.exceptions.PhoneNumberAlreadyInUseException;
import se.tink.backend.main.controllers.abnamro.exceptions.UserAlreadyMigratedException;
import se.tink.backend.main.controllers.exceptions.InvalidSmsOtpStatusException;
import se.tink.backend.rpc.abnamro.AbnAmroMigrationCommand;
import se.tink.backend.sms.otp.controllers.SmsOtpController;
import se.tink.backend.sms.otp.core.exceptions.SmsOtpNotFoundException;
import se.tink.backend.sms.otp.rpc.ConsumeRequest;
import se.tink.backend.sms.otp.rpc.ConsumeResponse;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.phonenumbers.utils.PhoneNumberUtils;

public class AbnAmroMigrationController {
    private static final LogUtils log = new LogUtils(AbnAmroMigrationController.class);
    private static final String USER_MIGRATED_ANALYTICS_EVENT = "user.migrated";
    private final UserRepository userRepository;
    private final SmsOtpController smsOtpController;
    private final HashingAlgorithm hashingAlgorithm;
    private final AnalyticsController analyticsController;
    private final AbnAmroCredentialsMigrator credentialsMigrator;
    private final UserDeviceController userDeviceController;

    @Inject
    public AbnAmroMigrationController(SmsOtpController smsOtpController, UserRepository userRepository,
            AuthenticationConfiguration authenticationConfiguration, AnalyticsController analyticsController,
            AbnAmroCredentialsMigrator credentialsMigrator, UserDeviceController userDeviceController) {
        this.smsOtpController = smsOtpController;
        this.userRepository = userRepository;
        this.hashingAlgorithm = authenticationConfiguration.getUserPasswordHashAlgorithm();
        this.analyticsController = analyticsController;
        this.credentialsMigrator = credentialsMigrator;
        this.userDeviceController = userDeviceController;
    }

    public User migrate(AbnAmroMigrationCommand command) throws SmsOtpNotFoundException, InvalidSmsOtpStatusException,
            PhoneNumberAlreadyInUseException, InvalidPhoneNumberException, UserAlreadyMigratedException {

        ConsumeResponse response = smsOtpController.consume(new ConsumeRequest(command.getSmsOtpVerificationToken()));

        final String phoneNumber = PhoneNumberUtils.normalize(response.getPhoneNumber());

        switch (response.getResult()) {

        case CONSUMED:
            return migrate(command, phoneNumber);
        case INVALID_OTP_STATUS:
            throw new InvalidSmsOtpStatusException(
                    String.format("Sms otp does not have a valid status (PhoneNumber = '%s').", phoneNumber));
        case ALREADY_CONSUMED:
            throw new InvalidSmsOtpStatusException(
                    String.format("Sms otp is already consumed (PhoneNumber = '%s').", phoneNumber));
        default:
            throw new NotImplementedException();
        }
    }

    private User migrate(AbnAmroMigrationCommand command, String phoneNumber) throws UserAlreadyMigratedException,
            PhoneNumberAlreadyInUseException {

        log.info(command.getUser().getId(), "Starting migration.");

        // 1. Start with migrating the user
        User user = migrateUser(command, phoneNumber);

        // 2. Authorize the device
        authorizeDevice(command.getUser(), command.getUserDeviceId(), command.getUserAgent());

        // 3. Migrate the credentials
        credentialsMigrator.migrate(user);

        log.info(user.getId(), "Migration completed.");

        return user;
    }

    private void authorizeDevice(User user, String userDeviceId, String userAgent) {
        UserDevice userDevice = userDeviceController.getAndUpdateUserDeviceOrCreateNew(user, userDeviceId, userAgent);

        if (userDevice == null) {
            throw new RuntimeException("User device must not be null.");
        }

        userDeviceController.authorizeDevice(userDevice);
    }

    private User migrateUser(AbnAmroMigrationCommand command, String phoneNumber) throws UserAlreadyMigratedException,
            PhoneNumberAlreadyInUseException {
        User user = command.getUser();

        Map<String, Object> migrationProperties = new ImmutableMap.Builder<String, Object>()
                .put("old-username", user.getUsername())
                .put("new-username", phoneNumber)
                .build();

        if (PhoneNumberUtils.isValid(user.getUsername())) {
            throw new UserAlreadyMigratedException(user.getUsername());
        }

        if (userRepository.findOneByUsername(phoneNumber) != null) {
            throw new PhoneNumberAlreadyInUseException(phoneNumber);
        }

        user.setUsername(phoneNumber);
        user.setHash(PasswordHash.create(command.getPin6(), hashingAlgorithm));

        user = userRepository.save(user);

        analyticsController
                .trackUserEvent(user, USER_MIGRATED_ANALYTICS_EVENT, migrationProperties, command.getRemoteAddress());

        return user;
    }
}
