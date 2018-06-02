package se.tink.backend.main.controllers.abnamro;

import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
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
import se.tink.backend.sms.otp.core.SmsOtpConsumeResult;
import se.tink.backend.sms.otp.rpc.ConsumeResponse;
import se.tink.libraries.abnamro.utils.AbnAmroLegacyUserUtils;
import se.tink.libraries.auth.encryption.HashingAlgorithm;
import se.tink.libraries.auth.encryption.PasswordHash;
import se.tink.libraries.phonenumbers.InvalidPhoneNumberException;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AbnAmroMigrationControllerTest {
    @Mock
    private SmsOtpController smsOtpController;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnalyticsController analyticsController;
    @Mock
    private UserDeviceController userDeviceController;

    private AuthenticationConfiguration authenticationConfiguration;

    private AbnAmroMigrationController abnAmroMigrationController;

    @Mock
    private AbnAmroCredentialsMigrator credentialsMigrator;

    @Before
    public void setUp() {
        authenticationConfiguration = new AuthenticationConfiguration();
        authenticationConfiguration.setUserPasswordHashAlgorithm(HashingAlgorithm.BCRYPT);

        abnAmroMigrationController = new AbnAmroMigrationController(smsOtpController, userRepository,
                authenticationConfiguration, analyticsController, credentialsMigrator, userDeviceController);

        doAnswer(returnsFirstArg()).when(userRepository).save(any(User.class));
        
        when(userDeviceController.getAndUpdateUserDeviceOrCreateNew(any(), any(), any())).thenReturn(new UserDevice());
    }

    @Test
    public void testThatMigrationFailsIfSmsOtpIsInvalid() throws Exception {
        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber("+46111111111");
        response.setResult(SmsOtpConsumeResult.INVALID_OTP_STATUS);

        when(smsOtpController.consume(any())).thenReturn(response);

        assertThatThrownBy(() -> abnAmroMigrationController.migrate(getValidMigrationCommand(new User())))
                .isInstanceOf(InvalidSmsOtpStatusException.class);
    }

    @Test
    public void testThatMigrationFailsIfSmsOtpAlreadyIsConsumed() throws Exception {
        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber("+46111111111");
        response.setResult(SmsOtpConsumeResult.ALREADY_CONSUMED);

        when(smsOtpController.consume(any())).thenReturn(response);

        assertThatThrownBy(() -> abnAmroMigrationController.migrate(getValidMigrationCommand(new User())))
                .isInstanceOf(InvalidSmsOtpStatusException.class);
    }

    @Test
    public void testThatMigrationFailsPhoneNumberIsInvalid() throws Exception {
        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber("013-12121212"); // invalid
        response.setResult(SmsOtpConsumeResult.CONSUMED);

        when(smsOtpController.consume(any())).thenReturn(response);

        assertThatThrownBy(() -> abnAmroMigrationController.migrate(getValidMigrationCommand(new User())))
                .isInstanceOf(InvalidPhoneNumberException.class);
    }

    @Test
    public void testThatMigrationFailsIfUserAlreadyIsMigrated() throws Exception {
        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber("+46709202541");
        response.setResult(SmsOtpConsumeResult.CONSUMED);

        when(smsOtpController.consume(any())).thenReturn(response);

        User user = new User();
        user.setUsername("+46709200255"); // This user has already migrated

        assertThatThrownBy(() -> abnAmroMigrationController.migrate(getValidMigrationCommand(user)))
                .isInstanceOf(UserAlreadyMigratedException.class);
    }

    @Test
    public void testThatMigrationFailsPhoneNumberAlreadyIsInUse() throws Exception {
        String phoneNumber = "+46709202541";

        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber(phoneNumber);
        response.setResult(SmsOtpConsumeResult.CONSUMED);

        when(smsOtpController.consume(any())).thenReturn(response);

        User alreadyRegisteredUser = new User();
        alreadyRegisteredUser.setUsername(phoneNumber);

        when(userRepository.findOneByUsername(phoneNumber)).thenReturn(alreadyRegisteredUser);

        User user = createUser(AbnAmroLegacyUserUtils.getUsername("123456789"));

        assertThatThrownBy(() -> abnAmroMigrationController.migrate(getValidMigrationCommand(user)))
                .isInstanceOf(PhoneNumberAlreadyInUseException.class);
    }

    @Test
    public void testCorrectMigration() throws Exception {
        String phoneNumber = "+46709202541";

        ConsumeResponse response = new ConsumeResponse();
        response.setPhoneNumber(phoneNumber);
        response.setResult(SmsOtpConsumeResult.CONSUMED);

        when(smsOtpController.consume(any())).thenReturn(response);

        User user = createUser(AbnAmroLegacyUserUtils.getUsername("123456789"));

        AbnAmroMigrationCommand migrationCommand = getValidMigrationCommand(user);

        user = abnAmroMigrationController.migrate(migrationCommand);

        // The username should be updated with the new phone number
        assertThat(user.getUsername()).isEqualTo(phoneNumber);

        // User should have a PIN6
        assertThat(PasswordHash.check(migrationCommand.getPin6(), user.getHash(),
                authenticationConfiguration.getUserPasswordHashAlgorithm())).isTrue();
    }

    private static AbnAmroMigrationCommand getValidMigrationCommand(User user) throws Exception {
        return AbnAmroMigrationCommand.builder()
                .withSmsOtpVerificationToken(UUID.randomUUID().toString())
                .withRemoteAddress(Optional.of("127.0.0.1"))
                .withPin6("121212")
                .withUserDeviceId("123456")
                .withUserAgent("Grip/4.0.0 (iOS; 8.1, iPhone Simulator)")
                .withUser(user)
                .build();
    }

    private static User createUser(String username) {
        User user = new User();
        user.setId(UUIDUtils.generateUUID());
        user.setUsername(username);
        return user;
    }
}
