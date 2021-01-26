package se.tink.backend.aggregation.agents.abnamro.utils;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.Sets;
import org.junit.Test;
import se.tink.backend.aggregationcontroller.v1.rpc.enums.CredentialsStatus;

public class AbnAmroIcsCredentialsTest {

    @Test
    public void testCreateAbnAmroIcsCredentials() throws Exception {
        AbnAmroIcsCredentials credentials = AbnAmroIcsCredentials.create("userid", "1234");

        credentials.addContractNumbers(Sets.newHashSet(1L, 2L));

        assertThat(AbnAmroIcsCredentials.isAbnAmroIcsCredentials(credentials.getCredentials()))
                .isTrue();
        assertThat(credentials.getBcNumber()).isEqualTo("1234");
        assertThat(credentials.getContractNumbers()).contains(1L, 2L);
        assertThat(credentials.hasContracts()).isTrue();

        // Status of the credential should be `created if contracts are added
        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.CREATED);
    }

    @Test
    public void testCreateWithoutContracts() throws Exception {
        AbnAmroIcsCredentials credentials = AbnAmroIcsCredentials.create("userid", "1234");

        assertThat(credentials.getContractNumbers()).isEmpty();
        assertThat(credentials.hasContracts()).isFalse();

        // Status of the credential should be `disabled` if no contracts are added
        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.DISABLED);
    }

    /** Test to add contract numbers to verify that they are persisted correctly */
    @Test
    public void testAddContractNumber() throws Exception {
        AbnAmroIcsCredentials credentials = AbnAmroIcsCredentials.create("userid", "1234");

        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.DISABLED);

        credentials.addContractNumber(1L);

        assertThat(credentials.getContractNumbers()).contains(1L);
        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.CREATED);

        credentials.addContractNumber(2L);

        assertThat(credentials.getContractNumbers()).contains(1L, 2L);
        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.CREATED);

        credentials.addContractNumber(1L);

        assertThat(credentials.getContractNumbers()).contains(1L, 2L);
        assertThat(credentials.getCredentials().getStatus()).isEqualTo(CredentialsStatus.CREATED);
    }
}
