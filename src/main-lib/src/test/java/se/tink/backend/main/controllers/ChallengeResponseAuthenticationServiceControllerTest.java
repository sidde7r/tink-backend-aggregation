package se.tink.backend.main.controllers;

import java.util.Date;
import junitparams.JUnitParamsRunner;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.common.repository.mysql.main.UserDeviceRepository;
import se.tink.backend.core.User;
import se.tink.backend.core.UserDevice;
import se.tink.backend.core.UserPublicKey;
import se.tink.backend.core.auth.AuthenticationSource;
import se.tink.backend.core.auth.UserAuthenticationChallenge;
import se.tink.backend.core.auth.UserPublicKeyType;
import se.tink.backend.main.auth.exceptions.UnauthorizedDeviceException;
import se.tink.libraries.auth.ChallengeStatus;
import se.tink.libraries.uuid.UUIDUtils;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class ChallengeResponseAuthenticationServiceControllerTest {
    private static final String USER_ID = "c1a8a157212240c4b9f170d122c4fc65";
    private static final String KEY_ID = "205fc3173d994b528ffee857bd1a084e";
    private static final String DEVICE_ID = "5D00A8B0-4853-4A61-BF5D-9BFE34449D30";
    private static final String PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQB+BROz0DQMG17liTc5yhNvgh1sJdI\n"
            + "MdZi0/1k8ax03ciZumBU0BDqM0/1MAUyrekFdXG+LZ0pM87rhYNrcRLu2WQBJrS6\n"
            + "ELr/XdJM+fty+7yvNmZ4sp6+dXPIwi+454zvujYuxxaMj6HyGforJUuszgOAEPOP\n"
            + "l1Aa/3c2ncA7lZNxkFk=\n"
            + "-----END PUBLIC KEY-----\n";
    private static final String NOT_PUBLIC_KEY = "-----BEGIN PUBLIC KEY-----\n"
            + "5173DA7A"
            + "-----END PUBLIC KEY-----\n";

    private static final int ONE_HOUR_IN_MS = 60 * 60 * 1000;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();
    @Mock
    private UserDeviceRepository userDeviceRepository;
    @Mock
    private User user;

    @InjectMocks
    private ChallengeResponseAuthenticationServiceController controller;

    @Before
    public void setUp() {
        UserDevice userDevice = mock(UserDevice.class);
        when(userDeviceRepository.findOneByUserIdAndDeviceId(USER_ID, DEVICE_ID)).thenReturn(userDevice);
        when(user.getId()).thenReturn(USER_ID);
    }

    @Test
    public void createKey_successful() {
        UserPublicKey key = controller.createECKey(USER_ID, DEVICE_ID, PUBLIC_KEY, AuthenticationSource.TOUCHID,
                UserPublicKeyType.ECDSA);

        assertThat(key).isNotNull();
        assertThat(key.getDeviceId()).isEqualTo(DEVICE_ID);
        assertThat(key.getPublicKey()).isEqualTo(PUBLIC_KEY);
    }

    @Test(expected = IllegalStateException.class)
    public void createKey_notEllipticCurve() {
        controller.createECKey(USER_ID, DEVICE_ID, PUBLIC_KEY, AuthenticationSource.TOUCHID, UserPublicKeyType.RSA);
    }

    @Test(expected = UnauthorizedDeviceException.class)
    public void createKey_invalidDeviceId() {
        controller.createECKey(UUIDUtils.generateUUID(), DEVICE_ID, PUBLIC_KEY, AuthenticationSource.TOUCHID,
                UserPublicKeyType.ECDSA);
    }

    @Test(expected = IllegalStateException.class)
    public void createKey_withNullDevice() {
        controller.createECKey(USER_ID, null, PUBLIC_KEY, AuthenticationSource.TOUCHID, UserPublicKeyType.ECDSA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createKey_withIllegalPublicKey() {
        controller.createECKey(USER_ID, DEVICE_ID, NOT_PUBLIC_KEY, AuthenticationSource.TOUCHID,
                UserPublicKeyType.ECDSA);
    }

    @Test
    public void createChallenge_successful() {
        UserAuthenticationChallenge challenge = controller.createChallenge(USER_ID, KEY_ID);
        Date now = new Date();

        assertThat(challenge).isNotNull();
        assertThat(challenge.getCreated()).isCloseTo(now, ONE_HOUR_IN_MS);
        assertThat(challenge.getExpiry()).isCloseTo(now, ONE_HOUR_IN_MS);
        assertThat(challenge.getStatus()).isEqualTo(ChallengeStatus.VALID);
        assertThat(challenge.getChallenge()).isNotEmpty();
        assertThat(challenge.getUserId()).isEqualTo(USER_ID);
        assertThat(challenge.getKeyId()).isEqualTo(KEY_ID);
    }

    @Test(expected = NullPointerException.class)
    public void createChallenge_noUser() {
        UserAuthenticationChallenge challenge = controller.createChallenge(null, KEY_ID);
    }
}
