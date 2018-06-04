package se.tink.backend.abnamro.utils;

import com.google.common.collect.ImmutableList;
import org.junit.Test;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.CredentialsStatus;
import se.tink.libraries.abnamro.utils.AbnAmroUtils;
import static org.assertj.core.api.Assertions.assertThat;

public class AbnAmroCredentialsUtilsTest {

    @Test
    public void testHistoricalTransactions() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.UPDATING);

        assertThat(AbnAmroCredentialsUtils.isEligibleForHistoryTransactions(credentials)).isTrue();
    }

    @Test
    public void testRealTimeTransactions() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.UPDATED);

        assertThat(AbnAmroCredentialsUtils.isEligibleForSingleTransactions(credentials)).isTrue();
    }

    @Test
    public void testBlockedRealTime() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.UPDATED);
        credentials.setPayload(AbnAmroUtils.CREDENTIALS_BLOCKED_PAYLOAD);

        assertThat(AbnAmroCredentialsUtils.isEligibleForSingleTransactions(credentials)).isFalse();
    }

    @Test
    public void testBlockedHistorical() throws Exception {
        Credentials credentials = new Credentials();
        credentials.setStatus(CredentialsStatus.UPDATING);
        credentials.setPayload(AbnAmroUtils.CREDENTIALS_BLOCKED_PAYLOAD);

        assertThat(AbnAmroCredentialsUtils.isEligibleForHistoryTransactions(credentials)).isFalse();
    }

    @Test
    public void testAggregationCredentials() throws Exception {
        Credentials oldConnectorCredential = new Credentials();
        oldConnectorCredential.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME);

        Credentials oldAggregationCredential = new Credentials();
        oldAggregationCredential.setProviderName(AbnAmroUtils.ABN_AMRO_ICS_PROVIDER_NAME);

        Credentials newConnectorAndAggregationCredential = new Credentials();
        newConnectorAndAggregationCredential.setProviderName(AbnAmroUtils.ABN_AMRO_PROVIDER_NAME_V2);

        assertThat(AbnAmroCredentialsUtils.getAggregationCredentials(ImmutableList.of(oldConnectorCredential,
                oldAggregationCredential, newConnectorAndAggregationCredential)))
                .containsOnly(oldAggregationCredential, newConnectorAndAggregationCredential);
    }
}
