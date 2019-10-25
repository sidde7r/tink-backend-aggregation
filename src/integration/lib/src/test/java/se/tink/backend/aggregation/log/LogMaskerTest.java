package se.tink.backend.aggregation.log;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.nxgen.controllers.configuration.AgentConfigurationController;
import se.tink.backend.aggregation.utils.ClientConfigurationStringMaskerBuilder;
import se.tink.backend.aggregation.utils.CredentialsStringMaskerBuilder;

public class LogMaskerTest {

    private Credentials credentials;

    @Before
    public void setUp() {
        credentials = mockCredentials();
    }

    @Test
    public void testMaskingWithoughClientConfigurationStringMasker() {
        LogMasker logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd");

        Assert.assertEquals(
                "String not masked as expected.", "abcd1111abcd1234abcd", maskedString1);
    }

    @Test
    public void testBasicCredentialsMasking() {
        LogMasker logMasker =
                LogMasker.builder()
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
        class TestClassWithPropertyChangeSupport {
            private final PropertyChangeSupport propertyChangeSupport =
                    new PropertyChangeSupport(this);
            private Collection<String> secretValues;

            public void addPropertyChangeListener(PropertyChangeListener listener) {
                this.propertyChangeSupport.addPropertyChangeListener(listener);
            }

            public void simulateNewValue(Collection<String> newSecretValues) {
                this.propertyChangeSupport.firePropertyChange(
                        AgentConfigurationController.SECRET_VALUES_PROPERTY_NAME,
                        secretValues,
                        newSecretValues);
                this.secretValues = newSecretValues;
            }
        }
        TestClassWithPropertyChangeSupport testClassWithPropertyChangeSupport =
                new TestClassWithPropertyChangeSupport();

        LogMasker logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(new CredentialsStringMaskerBuilder(credentials))
                        .build();
        testClassWithPropertyChangeSupport.addPropertyChangeListener(logMasker);

        String maskedString1 = logMasker.mask("abcd1111abcd1234abcd5678");

        Assert.assertEquals(
                "String not masked as expected.", "abcd1111abcd1234abcd5678", maskedString1);

        testClassWithPropertyChangeSupport.simulateNewValue(Sets.newHashSet("1111", "1234"));

        String maskedString2 = logMasker.mask("abcd1111abcd1234abcd5678");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd5678",
                maskedString2);

        testClassWithPropertyChangeSupport.simulateNewValue(Sets.newHashSet("5678"));

        String maskedString3 = logMasker.mask("abcd1111abcd1234abcd5678");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***",
                maskedString3);

        String maskedString4 = logMasker.mask("abcd1111abcd2020abcd1010");

        Assert.assertEquals(
                "String not masked as expected.",
                "abcd***MASKED***abcd***MASKED***abcd***MASKED***",
                maskedString4);
    }

    @Test
    public void testIsWhiteListed() {
        LogMasker logMasker =
                LogMasker.builder()
                        .addStringMaskerBuilder(
                                new ClientConfigurationStringMaskerBuilder(
                                        Arrays.asList("true", "false", "222", "1", "5555")))
                        .build();

        String unmasked = "true2225555falsealfgoiangoiandg555adlkga222";
        String masked = logMasker.mask(unmasked);
        Assert.assertEquals(
                "Didn't mask sensitive values as expected.",
                "true222***MASKED***falsealfgoiangoiandg555adlkga222",
                masked);
    }

    private Credentials mockCredentials() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getSensitivePayload())
                .thenReturn(
                        ImmutableMap.<String, String>builder()
                                .put("test-sensitive-key-1", "1010")
                                .put("test-sensitive-key-2", "2020")
                                .build());

        return credentials;
    }
}
