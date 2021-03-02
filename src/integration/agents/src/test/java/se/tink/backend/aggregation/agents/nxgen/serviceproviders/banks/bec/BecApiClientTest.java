package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Answers;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.nemid.NemIdException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.BaseBecRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.CodeAppTokenEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.LoggedInEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.PayloadAndroidEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.ScaOptionsEncryptedPayload;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.entities.SecondFactorOperationsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.EncryptedResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc.NemIdPollResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.rpc.BecErrorResponse;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.libraries.i18n.Catalog;
import se.tink.libraries.serialization.utils.SerializationUtils;
import src.integration.nemid.NemIdSupportedLanguageCode;

public class BecApiClientTest {

    private static final String PAYLOAD = "payload";
    private static final String ENCRYPTED_PAYLOAD = "encrypted payload";

    private static final String TOKEN_KEY = "token";
    private static final String TOKEN_VALUE = "sample token";

    private static final String USERNAME = "sample username";
    private static final String PASSWORD = "sample password";

    private static final String CODEAPP_2FA = "codeapp";

    private static final String DEVICE_ID = "deviceId";

    private static final String WRONG_CREDENTIALS = "Cause: LoginError.INCORRECT_CREDENTIALS";

    private static final String URL_PREFIX = "https://eticket";
    private static final String APP_SYNC_URL =
            URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/appsync";
    private static final String NEM_ID_POLL_URL =
            URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/logon/challenge/pollstate";
    private static final String PREPARE_SCA_URL =
            URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/logon/SCAprepare";
    private static final String SCA_URL = URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/logon/SCA";
    private static final String FETCH_ACCOUNTS_URL =
            URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/konto";
    private static final String FETCH_ACCOUNT_DETAILS_URL =
            URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/konto/kontodetaljer";
    private static final String LOGOUT_URL = URL_PREFIX + PAYLOAD + ".prod.bec.dk/mobilbank/logoff";

    private static final String SECURITY_KEY = "sample security key";

    private static final String LOGGED_IN_ENTITY =
            "{\n"
                    + "  \"lastUsed\": \"2020-08-07\",\n"
                    + "  \"username\": \"Name Name\",\n"
                    + "  \"bankReference\": \"1234567890\",\n"
                    + "  \"scaToken\": \"AAAA1111A1A111AA11A111111A11A1\",\n"
                    + "  \"authCodes\": [1, 2]\n"
                    + "}";

    private BecApiClient becApiClient;

    private BecSecurityHelper securityHelper;
    private TinkHttpClient client;
    private Catalog catalog;

    private RequestBuilder requestBuilder;

    @Before
    public void setUp() {
        securityHelper = mock(BecSecurityHelper.class);
        client = mock(TinkHttpClient.class, Answers.RETURNS_DEEP_STUBS);
        catalog = mock(Catalog.class);
        requestBuilder = mock(RequestBuilder.class);

        becApiClient =
                new BecApiClient(securityHelper, client, new BecUrlConfiguration(PAYLOAD), catalog);

        given(client.request(APP_SYNC_URL)).willReturn(requestBuilder);
        given(client.request(NEM_ID_POLL_URL)).willReturn(requestBuilder);
        given(client.request(PREPARE_SCA_URL)).willReturn(requestBuilder);
        given(client.request(SCA_URL)).willReturn(requestBuilder);
        given(client.request(FETCH_ACCOUNTS_URL)).willReturn(requestBuilder);
        given(client.request(FETCH_ACCOUNT_DETAILS_URL)).willReturn(requestBuilder);
        given(client.request(LOGOUT_URL)).willReturn(requestBuilder);
        // and
        given(
                        requestBuilder.header(
                                BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE))
                .willReturn(requestBuilder);
        given(requestBuilder.type(MediaType.APPLICATION_JSON_TYPE)).willReturn(requestBuilder);
        // and
        given(securityHelper.getKey()).willReturn(SECURITY_KEY);
    }

    @Test
    public void appSync() {
        // given
        BaseBecRequest request = baseBecRequest();
        request.setPayload(payloadAndroidEntity());

        // when
        becApiClient.appSync();

        // then
        verify(client).request(APP_SYNC_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).post(EncryptedResponse.class, request);
    }

    @Test
    public void pollNemIdShouldSucceed() throws AuthenticationException {
        // given
        given(requestBuilder.queryParam(TOKEN_KEY, TOKEN_VALUE)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":1}", NemIdPollResponse.class));
        // when
        becApiClient.pollNemId(TOKEN_VALUE);

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam(TOKEN_KEY, TOKEN_VALUE);
        verify(requestBuilder).get(NemIdPollResponse.class);
    }

    @Test
    public void pollNemIdShouldThrowTimeOutExceptionWhenUserRejectsNemid() {
        // given
        given(requestBuilder.queryParam(TOKEN_KEY, TOKEN_VALUE)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":2}", NemIdPollResponse.class));
        // when
        Throwable t = catchThrowable(() -> becApiClient.pollNemId(TOKEN_VALUE));

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam(TOKEN_KEY, TOKEN_VALUE);
        verify(requestBuilder).get(NemIdPollResponse.class);
        // and
        assertThat(t).isInstanceOf(NemIdException.class).hasMessage("Cause: NemIdError.REJECTED");
    }

    @Test
    public void pollNemIdShouldThrowNemIdExceptionWhenRequestsTimeout() {
        // given
        given(requestBuilder.queryParam(TOKEN_KEY, TOKEN_VALUE)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":4}", NemIdPollResponse.class));
        // when
        Throwable t = catchThrowable(() -> becApiClient.pollNemId(TOKEN_VALUE));

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam(TOKEN_KEY, TOKEN_VALUE);
        verify(requestBuilder).get(NemIdPollResponse.class);
        // and
        assertThat(t).isInstanceOf(NemIdException.class).hasMessage("Cause: NemIdError.TIMEOUT");
    }

    @Test
    public void pollNemIdShouldThrowTimeOutExceptionWhenResponseContainsUnknownState() {
        // given
        given(requestBuilder.queryParam(TOKEN_KEY, TOKEN_VALUE)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(NemIdPollResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"state\":345}", NemIdPollResponse.class));
        // when
        Throwable t = catchThrowable(() -> becApiClient.pollNemId(TOKEN_VALUE));

        // then
        verify(client).request(NEM_ID_POLL_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam(TOKEN_KEY, TOKEN_VALUE);
        verify(requestBuilder).get(NemIdPollResponse.class);
        // and
        assertThat(t)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Unknown error occured.");
    }

    @Test
    public void getScaOptionsShouldReturnPossible2faOptions()
            throws LoginException, NemIdException {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"" + ENCRYPTED_PAYLOAD + "\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD))
                .willReturn("{\"secondFactorOptions\":[\"" + CODEAPP_2FA + "\"]}");

        // when
        ScaOptionsEncryptedPayload result =
                becApiClient.getScaOptions(USERNAME, PASSWORD, DEVICE_ID);

        // then
        assertThat(result.getSecondFactorOptions()).containsOnly(CODEAPP_2FA);
    }

    @Test
    public void getScaOptionsShouldThrowLoginExceptionWhenUserProvidesWrongCredentials() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willThrow(new BecAuthenticationException(WRONG_CREDENTIALS));

        // when
        Throwable t =
                catchThrowable(() -> becApiClient.getScaOptions(USERNAME, PASSWORD, DEVICE_ID));

        // then
        assertThat(t).isInstanceOf(LoginException.class).hasMessage(WRONG_CREDENTIALS);
    }

    @Test
    public void
            getScaOptionsShouldThrowNemIdExceptionWhenLockedPinIsAMessageOfBecAuthenticationException() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        String pinLockedMsg =
                "Your chosen PIN code is locked. The PIN code must be changed in your Netbank before you can log on.";
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willThrow(new BecAuthenticationException(pinLockedMsg));

        // when
        Throwable t =
                catchThrowable(() -> becApiClient.getScaOptions(USERNAME, PASSWORD, DEVICE_ID));

        // then
        assertThat(t).isInstanceOf(NemIdException.class).hasMessage("Cause: NemIdError.LOCKED_PIN");
    }

    @Test
    public void
            getScaOptionsShouldThrowNemIdExceptionWhenBlockedNemIdIsAMessageOfBecAuthenticationException() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        String pinLockedMsg = "NemID is blocked. Contact support.";
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willThrow(new BecAuthenticationException(pinLockedMsg));

        // when
        Throwable t =
                catchThrowable(() -> becApiClient.getScaOptions(USERNAME, PASSWORD, DEVICE_ID));

        // then
        assertThat(t)
                .isInstanceOf(NemIdException.class)
                .hasMessage("Cause: NemIdError.NEMID_BLOCKED");
    }

    @Test
    public void getNemIdTokenShouldReturnPossible2faOptions() throws NemIdException {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"encrypted payload\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD))
                .willReturn("{\"codeapp\":{\"token\": \"sample token x\", \"pollTimeout\": 300}}");

        // when
        CodeAppTokenEncryptedPayload result =
                becApiClient.getNemIdToken(USERNAME, PASSWORD, DEVICE_ID);

        // then
        assertThat(result.getCodeappTokenDetails().getPollTimeout()).isEqualTo(300);
        assertThat(result.getCodeappTokenDetails().getToken()).isEqualTo("sample token x");
    }

    @Test
    public void
            getNemIdToken2ShouldThrowCredentialsVerificationExceptionWhen2ndScaRequestThrowsBecAuthException() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willThrow(new BecAuthenticationException(""));

        // when
        Throwable t =
                catchThrowable(() -> becApiClient.getNemIdToken(USERNAME, PASSWORD, DEVICE_ID));

        // then
        assertThat(t).isInstanceOf(LoginException.class);
    }

    @Test
    public void authCodeAppPostBaseRequest() throws ThirdPartyAppException {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);

        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"encrypted payload\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD)).willReturn(LOGGED_IN_ENTITY);

        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);

        // when
        LoggedInEntity loggedInEntity =
                becApiClient.authCodeApp(USERNAME, PASSWORD, TOKEN_VALUE, DEVICE_ID);

        assertLoggedInEntity(loggedInEntity);
    }

    @Test
    public void authCodeAppShouldThrowThirdPartyAppExceptionWhenScaRespondWithAuthException() {
        // given

        when(requestBuilder.post(eq(EncryptedResponse.class), any()))
                .thenThrow(new BecAuthenticationException("auth exception"));

        // when
        Throwable t =
                catchThrowable(
                        () -> becApiClient.authCodeApp(USERNAME, PASSWORD, TOKEN_VALUE, DEVICE_ID));

        // then
        assertThat(t).isInstanceOf(ThirdPartyAppException.class).hasMessage("auth exception");
    }

    @Test
    public void postKeyCardPrepareAndDecryptResponseShouldGetChallengeValue() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"encrypted payload\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD))
                .willReturn(
                        "{\n"
                                + "  \"secondFactorOptions\": [\n"
                                + "    \"keycard\"\n"
                                + "  ],\n"
                                + "  \"keycard\": {\n"
                                + "    \"keycardNo\": \"F123-123-123\",\n"
                                + "    \"nemidChallenge\": \"1234\"\n"
                                + "  }\n"
                                + "}");
        SecondFactorOperationsEntity secondFactorOperationsEntity =
                becApiClient.postKeyCardValuesAndDecryptResponse(USERNAME, PASSWORD, DEVICE_ID);

        assertThat(secondFactorOperationsEntity.getSecondFactorOptions().size()).isEqualTo(1);
        assertThat(secondFactorOperationsEntity.getSecondFactorOptions().get(0))
                .isEqualTo("keycard");
        assertThat(secondFactorOperationsEntity.getKeycard().getKeycardNo())
                .isEqualTo("F123-123-123");
        assertThat(secondFactorOperationsEntity.getKeycard().getNemidChallenge()).isEqualTo("1234");
    }

    @Test
    public void authKeyCardShouldReturnScaToken() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"encrypted payload\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD)).willReturn(LOGGED_IN_ENTITY);

        // when
        LoggedInEntity loggedInEntity =
                becApiClient.authKeyCard(
                        USERNAME, PASSWORD, "challengeValue", "nemidChallenge", DEVICE_ID);

        assertLoggedInEntity(loggedInEntity);
    }

    @Test
    public void authScaTokenShouldReturnScaToken() {
        // given
        given(securityHelper.encrypt(any())).willReturn(ENCRYPTED_PAYLOAD);
        // and
        BaseBecRequest baseBecRequest = baseBecRequest();
        baseBecRequest.setEncryptedPayload(ENCRYPTED_PAYLOAD);
        // and
        given(requestBuilder.post(eq(EncryptedResponse.class), eq(baseBecRequest)))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"encryptedPayload\":\"encrypted payload\"}",
                                EncryptedResponse.class));
        // and
        given(securityHelper.decrypt(ENCRYPTED_PAYLOAD)).willReturn(LOGGED_IN_ENTITY);

        // when
        LoggedInEntity loggedInEntity =
                becApiClient.authScaToken(USERNAME, PASSWORD, "scaToken", DEVICE_ID);

        assertLoggedInEntity(loggedInEntity);
    }

    @Test
    public void parseBodyAsError() {
        // given
        BecErrorResponse becErrorResponse =
                SerializationUtils.deserializeFromString(
                        "{\"action\": \"sample action\", \"message\": \"sample message\"}",
                        BecErrorResponse.class);
        // and
        HttpResponse response = mock(HttpResponse.class);
        given(response.getBody(BecErrorResponse.class)).willReturn(becErrorResponse);

        // when
        BecErrorResponse result = becApiClient.parseBodyAsError(response);

        // then
        assertThat(result.getAction()).isEqualTo("sample action");
        assertThat(result.getMessage()).isEqualTo("sample message");
    }

    @Test
    public void fetchAccounts() {
        // given
        given(requestBuilder.get(FetchAccountResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "[{\"accountId\":1}, {\"accountId\":2}]",
                                FetchAccountResponse.class));

        // when
        FetchAccountResponse respone = becApiClient.fetchAccounts();

        // then
        verify(client).request(FETCH_ACCOUNTS_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).get(FetchAccountResponse.class);
        // and
        assertThat(respone.get(0).getAccountId()).isEqualTo("1");
        assertThat(respone.get(1).getAccountId()).isEqualTo("2");
    }

    @Test
    public void fetchAccountDetails() {
        // given
        String accountId = "sample account id";
        // and
        given(requestBuilder.queryParam("accountId", accountId)).willReturn(requestBuilder);
        // and
        given(requestBuilder.get(AccountDetailsResponse.class))
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                "{\"iban\":\"sample iban\"}", AccountDetailsResponse.class));
        // when
        AccountDetailsResponse response = becApiClient.fetchAccountDetails(accountId);

        // then
        verify(client).request(FETCH_ACCOUNT_DETAILS_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).queryParam("accountId", accountId);
        verify(requestBuilder).get(AccountDetailsResponse.class);
        // and
        assertThat(response.getIban()).isEqualTo("sample iban");
    }

    @Test
    public void logout() {
        // given

        // when
        becApiClient.logout();

        // then
        verify(client).request(LOGOUT_URL);
        verify(requestBuilder)
                .header(BecConstants.Header.PRAGMA_KEY, BecConstants.Header.PRAGMA_VALUE);
        verify(requestBuilder).type(MediaType.APPLICATION_JSON_TYPE);
        verify(requestBuilder).post();
    }

    private BaseBecRequest baseBecRequest() {
        BaseBecRequest request = new BaseBecRequest();
        request.setLabel(BecConstants.Meta.LABEL);
        request.setCipher(BecConstants.Meta.CIPHER);
        request.setKey(SECURITY_KEY);
        return request;
    }

    private PayloadAndroidEntity payloadAndroidEntity() {
        PayloadAndroidEntity payloadAndroidEntity = new PayloadAndroidEntity();

        payloadAndroidEntity.setAppType(BecConstants.Meta.APP_TYPE);
        payloadAndroidEntity.setAppVersion(BecConstants.Meta.APP_VERSION);
        payloadAndroidEntity.setLocale(
                NemIdSupportedLanguageCode.DEFAULT_LANGUAGE_CODE.getIsoLanguageCode());
        payloadAndroidEntity.setOsVersion(BecConstants.Meta.OS_VERSION);
        payloadAndroidEntity.setDeviceType(BecConstants.Meta.DEVICE_TYPE);
        return payloadAndroidEntity;
    }

    private void assertLoggedInEntity(LoggedInEntity loggedInEntity) {
        assertThat(loggedInEntity.getBankReference()).isEqualTo("1234567890");
        assertThat(loggedInEntity.getLastUsed()).isEqualTo("2020-08-07");
        assertThat(loggedInEntity.getScaToken()).isEqualTo("AAAA1111A1A111AA11A111111A11A1");
    }
}
