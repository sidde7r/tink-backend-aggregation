package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkAuthenticatorTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/authenticator";

    private WizinkApiClient wizinkApiClient;
    private WizinkAuthenticator wizinkAuthenticator;
    private WizinkStorage wizinkStorage;

    @Before
    public void setup() {
        wizinkApiClient = mock(WizinkApiClient.class);
        wizinkStorage = new WizinkStorage(new PersistentStorage(), new SessionStorage());
        wizinkAuthenticator = new WizinkAuthenticator(wizinkApiClient, wizinkStorage);
    }

    @Test
    public void shouldFetchAndStoreCreditCardsFromLoginResponse() {
        // given
        mockCustomerLoginResponse("customer_login_response.json");

        // when
        wizinkAuthenticator.processLogin("USERNAME", "PASSWORD");

        // then
        assertThat(wizinkStorage.getCreditCardList()).hasSize(3);
    }

    @Test
    public void shouldFetchAndStoreProductsFromLoginResponse() {
        // given
        mockCustomerLoginResponse("customer_login_response.json");

        // when
        wizinkAuthenticator.processLogin("USERNAME", "PASSWORD");

        // then
        assertThat(wizinkStorage.getAccounts()).hasSize(2);
    }

    @Test
    public void shouldFetchAndStoreCreditCardsWithoutProductsFromLoginResponse() {
        // given
        mockCustomerLoginResponse("customer_login_response_without_products.json");

        // when
        wizinkAuthenticator.processLogin("USERNAME", "PASSWORD");

        // then
        assertThat(wizinkStorage.getCreditCardList()).hasSize(3);
        assertThat(wizinkStorage.getAccounts()).isEmpty();
    }

    @Test
    public void shouldFetchAndStoreProductsWithoutCreditCardsFromLoginResponse() {
        // given
        mockCustomerLoginResponse("customer_login_response_without_cards.json");

        // when
        wizinkAuthenticator.processLogin("USERNAME", "PASSWORD");

        // then
        assertThat(wizinkStorage.getAccounts()).hasSize(2);
        assertThat(wizinkStorage.getCreditCardList()).isEmpty();
    }

    private void mockCustomerLoginResponse(String responseFile) {
        when(wizinkApiClient.login(any()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                Paths.get(TEST_DATA_PATH, responseFile).toFile(),
                                CustomerLoginResponse.class));
    }
}
