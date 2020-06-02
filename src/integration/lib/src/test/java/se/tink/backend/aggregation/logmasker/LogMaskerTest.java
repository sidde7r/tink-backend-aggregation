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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.utils.masker.CredentialsStringMaskerBuilder;
import se.tink.backend.aggregation.utils.masker.SensitiveValuesCollectionStringMaskerBuilder;

public class LogMaskerTest {

    private Credentials credentials;

    @Before
    public void setup() {
        credentials = mockCredentials();
    }

    @Test
    public void testMaskingWithoutClientConfigurationStringMasker() {
        LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd");

        Assert.assertEquals(
                "String not masked as expected.", "abcd1111abcd1234abcd", maskedString1);
    }

    @Test
    public void testBasicCredentialsMasking() {
        LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();

        String maskedString1 = logMasker.mask("abcd1010abcd2020abcd");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd",
                maskedString1);
    }

    @Test
    public void testMaskingWithUpdatedClientConfigurationStringMasker() {
        Subject<Collection<String>> testSecretValuesSubject = BehaviorSubject.create();
        testSecretValuesSubject.onNext(Sets.newHashSet("0000"));
        LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();
        logMasker.addSensitiveValuesSetObservable(testSecretValuesSubject);

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd1111abcd1234abcd5678abcd***MASKED***",
                maskedString1);

        testSecretValuesSubject.onNext(Sets.newHashSet("1111", "1234"));

        String maskedString2 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd5678abcd***MASKED***",
                maskedString2);

        testSecretValuesSubject.onNext(Sets.newHashSet("5678"));

        String maskedString3 = logMasker.mask("abcd1111abcd1234abcd5678abcd0000");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***abcd***MASKED***",
                maskedString3);

        String maskedString4 = logMasker.mask("abcd1111abcd2020abcd1010abcd0000");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***abcd***MASKED***",
                maskedString4);

        testSecretValuesSubject.onComplete();
    }

    @Test
    public void testIsWhiteListed() {
        LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(
                                new SensitiveValuesCollectionStringMaskerBuilder(
                                        Arrays.asList("true", "false", "222", "1", "5555")))
                        .build();

        String unmasked = "true2225555falsealfgoiangoiandg555adlkga222";
        String masked = logMasker.mask(unmasked);
        Assert.assertEquals(
                "Didn't mask sensitive values as expected.",
                "true222***MASKED***falsealfgoiangoiandg555adlkga222",
                masked);
    }

    @Test
    public void testAgentWhiteListedValue() {
        LogMasker logMasker =
                LogMaskerImpl.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();

        Subject<Collection<String>> testSecretValuesSubject = BehaviorSubject.create();
        testSecretValuesSubject.onNext(Sets.newHashSet("1111", "0000"));
        logMasker.addSensitiveValuesSetObservable(testSecretValuesSubject);

        String unmasked = "abcd1111abcd2020abcd1010abcd0000";
        String maskedString1 = logMasker.mask(unmasked);
        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***abcd***MASKED***",
                maskedString1);

        ImmutableSet<String> whitelistedValues = ImmutableSet.of("0000");
        logMasker.addAgentWhitelistedValues(whitelistedValues);

        String maskedString2 = logMasker.mask(unmasked);
        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***abcd0000",
                maskedString2);
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
