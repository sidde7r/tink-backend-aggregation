package se.tink.backend.common.utils;

import org.junit.Test;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsTypes;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderTypes;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MetricsUtilsTest {

    @Test
    public void testCredetialName() {
        Credentials credentials = mock(Credentials.class);
        when(credentials.getType()).thenReturn(CredentialsTypes.MOBILE_BANKID);

        assertEquals("mobile_bankid", MetricsUtils.credentialName(credentials));
    }

    @Test
    public void testProviderTypeName() {
        Provider provider = mock(Provider.class);
        when(provider.getType()).thenReturn(ProviderTypes.BANK);

        assertEquals("bank", MetricsUtils.providerTypeName(provider));
    }

    @Test
    public void testCleanMetricName() {
        String input = "'test*'-type(bankid)swedbank";

        String metricName = MetricsUtils.cleanMetricName(input);
        assertEquals("test-type_bankid_swedbank", metricName);
    }
}
