package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeTestHelper.createCbiGlobeApiClient;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeTestHelper.createPersistentStorage;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeTestHelper.mockHttpClient;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.fetcher.transactionalaccount.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class AccountFetchingStepTest {
    private AccountFetchingStep accountStep;
    private TinkHttpClient client;
    private RequestBuilder requestBuilder;
    private CbiUserState userState;

    private final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/serviceproviders/openbanking/cbiglobe/resources";

    @Before
    public void init() {
        HttpResponse response = mock(HttpResponse.class);
        this.requestBuilder = mock(RequestBuilder.class);
        this.client = mockHttpClient(response, requestBuilder);
        this.userState = new CbiUserState(createPersistentStorage(), mock(Credentials.class));
        CbiGlobeApiClient apiClient = createCbiGlobeApiClient(client);
        accountStep = new AccountFetchingStep(apiClient, userState);
    }

    @Test
    public void executeShouldReturnAuthenticationSuccededIfNoAccountsFetched() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(Mockito.mock(Credentials.class));
        when(requestBuilder.get(GetAccountsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response.json").toFile(),
                                GetAccountsResponse.class));

        // when
        AuthenticationStepResponse response = accountStep.execute(request);

        // then
        assertThat(userState.getAccountsResponseFromStorage()).isNull();
        assertThat(response.isAuthenticationFinished()).isTrue();
    }

    @Test
    public void executeShouldReturnEmptyOptionIfAccountsAvailable() {
        // given
        AuthenticationRequest request = new AuthenticationRequest(Mockito.mock(Credentials.class));
        when(requestBuilder.get(GetAccountsResponse.class))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, "accounts_response_not_empty.json")
                                        .toFile(),
                                GetAccountsResponse.class));

        // when
        AuthenticationStepResponse response = accountStep.execute(request);

        // then
        assertThat(userState.getAccountsResponseFromStorage().getAccounts()).hasSize(2);
        assertThat(response.isAuthenticationFinished()).isFalse();
    }
}
