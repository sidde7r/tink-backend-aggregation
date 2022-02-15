package se.tink.backend.aggregation.logmasker;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import io.reactivex.rxjava3.subjects.BehaviorSubject;
import io.reactivex.rxjava3.subjects.Subject;
import java.util.Arrays;
import java.util.Collection;
import java.util.regex.Pattern;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.utils.masker.CredentialsStringValuesProvider;

public class LogMaskerTest {

    private static final String MASK = "\\*\\*HASHED:[-A-Za-z0-9+/=]{2}\\*\\*";

    private Credentials credentials;

    @Before
    public void setup() {
        credentials = mockCredentials();
    }

    @Test
    public void testMaskingWithoutClientConfigurationStringMasker() {
        LogMasker logMasker = new LogMaskerImpl();

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd");

        Assert.assertEquals(
                "String not masked as expected.", "abcd1111abcd1234abcd", maskedString1);
    }

    @Test
    public void testBasicCredentialsMasking() {
        LogMasker logMasker = new LogMaskerImpl();
        logMasker.addNewSensitiveValuesToMasker(
                new CredentialsStringValuesProvider(credentials).getValuesToMask());

        String maskedString1 = logMasker.mask("abcd1010abcd2020abcd");

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd")
                        .matcher(maskedString1)
                        .find());
    }

    @Test
    public void testMaskingWithUpdatedClientConfigurationStringMasker() {
        Subject<Collection<String>> testSecretValuesSubject = BehaviorSubject.create();
        testSecretValuesSubject.onNext(Sets.newHashSet("0000"));
        LogMasker logMasker = new LogMaskerImpl();
        logMasker.addNewSensitiveValuesToMasker(
                new CredentialsStringValuesProvider(credentials).getValuesToMask());
        logMasker.addSensitiveValuesSetObservable(testSecretValuesSubject);

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd1111abcd1234abcd5678abcd" + MASK)
                        .matcher(maskedString1)
                        .find());

        testSecretValuesSubject.onNext(Sets.newHashSet("1111", "1234"));

        String maskedString2 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd5678abcd" + MASK)
                        .matcher(maskedString2)
                        .find());

        testSecretValuesSubject.onNext(Sets.newHashSet("5678"));

        String maskedString3 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd" + MASK + "abcd" + MASK)
                        .matcher(maskedString3)
                        .find());

        String maskedString4 = logMasker.mask("abcd1111abcd2020abcd1010abcd0000");

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd" + MASK + "abcd" + MASK)
                        .matcher(maskedString4)
                        .find());

        testSecretValuesSubject.onComplete();
    }

    @Test
    public void testIsWhiteListed() {
        LogMasker logMasker = new LogMaskerImpl();
        logMasker.addNewSensitiveValuesToMasker(
                Arrays.asList("true", "false", "null", "222", "1", "5555"));

        String unmasked = "true2225555falsealfgoiangoiandg555adlknullga222";
        String masked = logMasker.mask(unmasked);

        Assert.assertTrue(
                "Didn't mask sensitive values as expected.",
                Pattern.compile("true222" + MASK + "falsealfgoiangoiandg555adlknullga222")
                        .matcher(masked)
                        .find());
    }

    @Test
    public void testAgentWhiteListedValue() {
        LogMasker logMasker = new LogMaskerImpl();

        Subject<Collection<String>> testSecretValuesSubject = BehaviorSubject.create();
        testSecretValuesSubject.onNext(Sets.newHashSet("1111", "0000"));
        logMasker.addNewSensitiveValuesToMasker(
                new CredentialsStringValuesProvider(credentials).getValuesToMask());
        logMasker.addSensitiveValuesSetObservable(testSecretValuesSubject);

        String unmasked = "abcd1111abcd2020abcd1010abcd0000";
        String maskedString1 = logMasker.mask(unmasked);

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd" + MASK + "abcd" + MASK)
                        .matcher(maskedString1)
                        .find());

        ImmutableSet<String> whitelistedValues = ImmutableSet.of("0000");
        logMasker.addAgentWhitelistedValues(whitelistedValues);

        String maskedString2 = logMasker.mask(unmasked);

        Assert.assertTrue(
                "String not masked as expected.",
                Pattern.compile("abcd" + MASK + "abcd" + MASK + "abcd" + MASK + "abcd0000")
                        .matcher(maskedString2)
                        .find());
    }

    private Credentials mockCredentials() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getSensitivePayloadAsMap())
                .thenReturn(
                        ImmutableMap.<String, String>builder()
                                .put("test-sensitive-key-1", "1010")
                                .put("test-sensitive-key-2", "2020")
                                .build());

        return credentials;
    }
}
