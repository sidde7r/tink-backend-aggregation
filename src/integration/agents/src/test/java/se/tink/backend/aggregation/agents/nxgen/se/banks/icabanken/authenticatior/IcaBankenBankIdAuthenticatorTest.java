package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticatior;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.http.HttpStatus;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.bankid.status.BankIdStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.IcaBankenBankIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.BankIdAuthPollBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.authenticator.entities.SessionBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.identitydata.entities.CustomerBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IcaBankenBankIdAuthenticatorTest {

    private static final String DUMMY_SSN = "19990101011234";
    private static final String DUMMY_REF = "12345678-1234-1234-1234-123456789012";

    private IcaBankenApiClient apiClient;
    private IcaBankenSessionStorage sessionStorage;
    private IcaBankenBankIdAuthenticator authenticator;
    private HttpResponse httpResponse;
    private HttpResponseException httpResponseException;

    @Before
    public void setup() {
        apiClient = mock(IcaBankenApiClient.class);
        sessionStorage = mock(IcaBankenSessionStorage.class);
        authenticator = new IcaBankenBankIdAuthenticator(apiClient, sessionStorage);

        httpResponse = mock(HttpResponse.class);
        httpResponseException = mock(HttpResponseException.class);

        when(httpResponseException.getResponse()).thenReturn(httpResponse);
    }

    @Test
    public void shouldThrowLoginBankIdErrorAlreadyInProgressWhenResponseIsInConflict() {
        when(apiClient.initBankId(DUMMY_SSN)).thenThrow(httpResponseException);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_CONFLICT);

        assertThatThrownBy(() -> authenticator.init(DUMMY_SSN))
                .isInstanceOf(BankIdError.ALREADY_IN_PROGRESS.exception().getClass());
    }

    @Test
    public void shouldThrowLoginBankSideFailureWhenResponseIsInternalServerError() {
        when(apiClient.initBankId(DUMMY_SSN)).thenThrow(httpResponseException);
        when(httpResponse.getStatus()).thenReturn(HttpStatus.SC_INTERNAL_SERVER_ERROR);

        assertThatThrownBy(() -> authenticator.init(DUMMY_SSN))
                .isInstanceOf(BankServiceError.BANK_SIDE_FAILURE.exception().getClass());
    }

    @Test
    public void assertBankIdStatusIsWaitingWhenOrderStatusIsPending() {
        when(apiClient.pollBankId(anyString())).thenReturn(getPendingBankIdReponse());
        assertEquals(BankIdStatus.WAITING, authenticator.collect(DUMMY_REF));
    }

    @Test
    public void shouldThrowNotACustomerWhenIsCustomerIsFalse() {
        when(apiClient.pollBankId(anyString())).thenReturn(getOkBankIdReponse());
        when(apiClient.authenticateBankId(anyString())).thenReturn(getSessionBodyEntity());
        when(apiClient.fetchCustomer()).thenReturn(getCustomerBodyEntity("false", "true", "true"));

        assertThatThrownBy(() -> authenticator.collect(DUMMY_REF))
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
    }

    @Test
    public void shouldThrowNotACustomerByNoActiveBankWhenHasActiveBankIsFalse() {
        when(apiClient.pollBankId(anyString())).thenReturn(getOkBankIdReponse());
        when(apiClient.authenticateBankId(anyString())).thenReturn(getSessionBodyEntity());
        when(apiClient.fetchCustomer()).thenReturn(getCustomerBodyEntity("true", "false", "true"));

        assertThatThrownBy(() -> authenticator.collect(DUMMY_REF))
                .isInstanceOf(LoginError.NOT_CUSTOMER.exception().getClass());
    }

    @Test
    public void shouldThrowNoAccessToMobileBankingWhenUpdatedKDKisFalse() {
        when(apiClient.pollBankId(anyString())).thenReturn(getOkBankIdReponse());
        when(apiClient.authenticateBankId(anyString())).thenReturn(getSessionBodyEntity());
        when(apiClient.fetchCustomer()).thenReturn(getCustomerBodyEntity("true", "true", "false"));

        assertThatThrownBy(() -> authenticator.collect(DUMMY_REF))
                .isInstanceOf(LoginError.NO_ACCESS_TO_MOBILE_BANKING.exception().getClass());
    }

    private BankIdAuthPollBodyEntity getPendingBankIdReponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "        \"OrderStatus\": \"Pending\",\n"
                        + "        \"OrderRef\": \"12345678-1234-1234-1234-123456789012\"\n"
                        + "    }",
                BankIdAuthPollBodyEntity.class);
    }

    private BankIdAuthPollBodyEntity getOkBankIdReponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "        \"OrderStatus\": \"Ok\",\n"
                        + "        \"OrderRef\": \"12345678-1234-1234-1234-123456789012\"\n"
                        + "    }",
                BankIdAuthPollBodyEntity.class);
    }

    private SessionBodyEntity getSessionBodyEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "        \"SessionId\": \"AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA\",\n"
                        + "        \"ClientSessionTimeToLive\": 300,\n"
                        + "        \"HeartbeatInterval\": 120,\n"
                        + "        \"UserInstallationId\": \"12345678-1234-1234-1234-123456789012\"\n"
                        + "    }",
                SessionBodyEntity.class);
    }

    private CustomerBodyEntity getCustomerBodyEntity(
            String isCustomer, String hasActiveBank, String updatedKDK) {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"PersonalIdentityNumber\": \"19990101011234\",\n"
                        + "    \"FirstName\": \"FIRSTNAME\",\n"
                        + "    \"LastName\": \"LASTNAME\",\n"
                        + "    \"Address\":\n"
                        + "    {\n"
                        + "        \"IsProtectedAddress\": false,\n"
                        + "        \"CoName\": \"\",\n"
                        + "        \"Street\": \"RIKEMANSVÄGEN 1\",\n"
                        + "        \"City\": \"ANKEBORG\",\n"
                        + "        \"Country\": \"SVERIGE\",\n"
                        + "        \"PostalCode\": \"11111\",\n"
                        + "        \"IsSwedishAddress\": true\n"
                        + "    },\n"
                        + "    \"MobilePhone\": \"1111111111\",\n"
                        + "    \"HomePhone\": \"1111111111\",\n"
                        + "    \"Email\": \"firstname.lastname.@mail.com\",\n"
                        + "    \"IsStudent\": false,\n"
                        + "    \"IsDeceased\": false,\n"
                        + "    \"IsExForexCustomer\": false,\n"
                        + "    \"Engagement\":\n"
                        + "    {\n"
                        + "        \"HasEGiro\": false,\n"
                        + "        \"HasHomeLoan\": false,\n"
                        + "        \"HasUnsecuredPrivateLoan\": false,\n"
                        + "        \"HasFund\": false,\n"
                        + "        \"HasFundWithDisposition\": false,\n"
                        + "        \"HasInsuranceICA\": true,\n"
                        + "        \"HasInsuranceExternal\": false,\n"
                        + "        \"HasCard\": true,\n"
                        + "        \"HadActiveBank\": false,\n"
                        + "        \"HasActiveBank\": "
                        + hasActiveBank
                        + ",\n"
                        + "        \"HasAccount\": true,\n"
                        + "        \"HasAccountWithDisposition\": false,\n"
                        + "        \"HasActiveAccountsInActiveBank\": true,\n"
                        + "        \"HasCardICALoyalty\": false,\n"
                        + "        \"HasCardICALoyaltyDebitPayment\": true\n"
                        + "    },\n"
                        + "    \"IsUnderAdministration\": false,\n"
                        + "    \"MainFrameCustomerId\": \"111111111\",\n"
                        + "    \"ReceiveOffers\": true,\n"
                        + "    \"IsCustomer\": "
                        + isCustomer
                        + ",\n"
                        + "    \"VerifiedIdentification\": true,\n"
                        + "    \"UpdatedKDK\": "
                        + updatedKDK
                        + ",\n"
                        + "    \"IsFullyLoaded\": true,\n"
                        + "    \"HasCustodianApprovalToApplyForMobileBankId\": false,\n"
                        + "    \"IsMBIDApproved\": true,\n"
                        + "    \"ExpectKDK\": true\n"
                        + "}",
                CustomerBodyEntity.class);
    }
}
