package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityFetcherTest {

    private IspApiClient client;
    private IdentityFetcher identityFetcher;

    @Before
    public void setup() {
        this.client = mock(IspApiClient.class);
        this.identityFetcher = new IdentityFetcher(client);
    }

    @Test
    public void shouldFetchAndMapIdentityForMaleCustomer() {
        // given
        when(client.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.maleIdentityDataResponse());
        // when
        IdentityData identityData = identityFetcher.fetchIdentityData();
        // then
        assertThat(identityData).isNotNull();
        assertThat(identityData.getFullName()).isEqualTo("TESTNAME TESTSURNAME");
        assertThat(identityData.getDateOfBirth()).isEqualTo("1990-08-31");
    }

    @Test
    public void shouldFetchAndMapIdentityForFemaleCustomer() {
        // given
        when(client.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.femaleIdentityDataResponse());
        // when
        IdentityData identityData = identityFetcher.fetchIdentityData();
        // then
        assertThat(identityData).isNotNull();
        assertThat(identityData.getFullName()).isEqualTo("TESTNAME TESTSURNAME");
        assertThat(identityData.getDateOfBirth()).isEqualTo("1990-08-03");
    }

    @Test
    public void shouldFetchNameAndSurnameIfDateOfBirthNBotAvailable() {
        // given
        when(client.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.incorrectFiscalCodeIdentityDataResponse());
        // when
        IdentityData identityData = identityFetcher.fetchIdentityData();
        // then
        assertThat(identityData).isNotNull();
        assertThat(identityData.getFullName()).isEqualTo("TESTNAME TESTSURNAME");
        assertThat(identityData.getDateOfBirth()).isNull();
    }

    @Test
    public void shouldThrowExceptionIf2IdentityDataEntitiesPresent() {
        // given
        when(client.fetchAccountsAndIdentities())
                .thenReturn(FetchersTestData.multipleIdentityDataResponse());
        // when
        ThrowableAssert.ThrowingCallable fetchIdentitiesCallable =
                identityFetcher::fetchIdentityData;
        // then
        assertThatCode(fetchIdentitiesCallable).isInstanceOf(IllegalStateException.class);
    }
}
