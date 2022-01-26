package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankIdAutostartTokenResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.BankiIdResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CrossKeyBankIdAuthenticatorTest {
    private CrossKeyBankIdAuthenticator authenticator;
    private CrossKeyApiClient apiClient;

    @Before
    public void setup() {
        SessionStorage sessionStorage = new SessionStorage();
        this.apiClient = mock(CrossKeyApiClient.class);
        this.authenticator =
                new CrossKeyBankIdAuthenticator(
                        apiClient,
                        mock(CrossKeyConfiguration.class),
                        sessionStorage,
                        mock(Credentials.class));
    }

    @Test
    public void shouldThrowBankServiceErrorBankSideFailure() {
        BankIdAutostartTokenResponse token = getBankIdAutostartTokenResponse();
        BankiIdResponse bankiIdResponse = getBankiIdResponse();
        // when
        when(apiClient.collectBankId()).thenReturn(bankiIdResponse);

        // then
        assertThatThrownBy(() -> authenticator.collect(token))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
        assertEquals(
                "The bank service has temporarily failed; please try again later.",
                BankServiceError.BANK_SIDE_FAILURE.exception().getError().userMessage().get());
    }

    @Test
    public void shouldThrowLoginErrorNotCustomer() {
        BankIdAutostartTokenResponse token = getBankIdAutostartTokenResponse();
        BankiIdResponse bankiIdResponse = getBankiIdResponseUserLocked();
        // when
        when(apiClient.collectBankId()).thenReturn(bankiIdResponse);

        // then
        assertThatThrownBy(() -> authenticator.collect(token))
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
        assertEquals(
                "You don't have any commitments in the selected bank.",
                LoginError.NOT_CUSTOMER.exception().getError().userMessage().get());
    }

    private BankIdAutostartTokenResponse getBankIdAutostartTokenResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"autostartToken\":\"autoStartToken\"}", BankIdAutostartTokenResponse.class);
    }

    private BankiIdResponse getBankiIdResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"status\":{\"success\":false,\"errors\":[\"AUTHENTICATION_FAILURE\",\"INTERNAL_SERVER_ERROR\"],\"infos\":[],\"jSessionId\":\"zl4uG4k-bbOjHNHe4NXva4yj\"},\"approvalNeeded\":false,\"amlAnswersNeeded\":false,\"pinChangeNeeded\":false,\"sessionKey\":null}\n",
                BankiIdResponse.class);
    }

    private BankiIdResponse getBankiIdResponseUserLocked() {
        return SerializationUtils.deserializeFromString(
                "{\"status\":{\"success\":false,\"errors\":[\"AUTHENTICATION_FAILURE\",\"USER_LOCKED\"],\"infos\":[],\"jSessionId\":\"zl4uG4k-bbOjHNHe4NXva4yj\"},\"approvalNeeded\":false,\"amlAnswersNeeded\":false,\"pinChangeNeeded\":false,\"sessionKey\":null}\n",
                BankiIdResponse.class);
    }
}
