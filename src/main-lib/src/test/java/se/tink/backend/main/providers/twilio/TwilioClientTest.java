package se.tink.backend.main.providers.twilio;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.common.config.TwilioConfiguration;

public class TwilioClientTest {

    private TwilioClient twilio;

    @Before
    public void setup() {
        TwilioConfiguration config = Mockito.mock(TwilioConfiguration.class);
        Mockito.when(config.getAccountSid()).thenReturn("AC123456789");
        Mockito.when(config.getAuthToken()).thenReturn("abcdefghijklmnopqrstuvwxyzåäö1234567890");
        twilio = new TwilioClient(config);
    }

    @Test
    public void ensureExtrapolate_replace_07_with_plus46() {
        String phoneNumber = twilio.extrapolatePhoneNumber("0712345678");

        Assert.assertEquals("+46712345678", phoneNumber);
    }

    @Test
    public void ensureExtrapolate_removeWhitespace() {
        String phoneNumber = twilio.extrapolatePhoneNumber("+46712 34 56 78");

        Assert.assertEquals("+46712345678", phoneNumber);
    }

    @Test
    public void ensureExtrapolate_removeHyphens() {
        String phoneNumber = twilio.extrapolatePhoneNumber("+46712-345678");

        Assert.assertEquals("+46712345678", phoneNumber);
    }

    @Test
    public void ensureExtrapolate_returnsNull_whenPhoneNumber_doesNotStartWith_07_or_plus467() {
        String phoneNumber = twilio.extrapolatePhoneNumber("0812345678");

        Assert.assertNull(phoneNumber);

        phoneNumber = twilio.extrapolatePhoneNumber("+45712345678");

        Assert.assertNull(phoneNumber);
    }

    @Test
    public void ensureExtrapolate_returnsNull_whenPhoneNumberLength_notEqual_toTwelve() {
        String phoneNumber = twilio.extrapolatePhoneNumber("+4671234567");

        Assert.assertNull(phoneNumber);
    }
}
