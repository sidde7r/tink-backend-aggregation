package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Stream;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import junitparams.converters.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.authenticator.rpc.ScaResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentExpiredException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.exceptions.ConsentRevokedException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.card.rpc.CardResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.BalanceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.util.AccountTestData;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class SparebankAuthenticatorTest {

    private static final AccountResponse SAMPLE_ACCOUNT_RESPONSE =
            AccountTestData.getAccountResponse();
    private static final String FIRST_SAMPLE_ACCOUNT_RESOURCE_ID =
            SAMPLE_ACCOUNT_RESPONSE.getAccounts().get(0).getResourceId();

    private static final CardResponse SAMPLE_CARD_RESPONSE = AccountTestData.getCardResponse();
    private static final String FIRST_SAMPLE_CARD_RESOURCE_ID =
            SAMPLE_CARD_RESPONSE.getCardAccounts().get(0).getResourceId();

    private static final BalanceResponse SAMPLE_BALANCE_RESPONSE =
            AccountTestData.getBalanceResponse();

    private static final String DATE_WHATEVER = "1000-01-01 12:00";

    /*
    Mocks
     */
    private SparebankApiClient apiClient;
    private SparebankStorage storage;
    private Credentials credentials;
    private LocalDateTimeSource localDateTimeSource;

    /*
    Real
     */
    private SparebankAuthenticator authenticator;

    @Before
    public void setup() {
        apiClient = mock(SparebankApiClient.class);
        storage = mock(SparebankStorage.class);
        credentials = mock(Credentials.class);
        localDateTimeSource = mock(LocalDateTimeSource.class);

        authenticator =
                new SparebankAuthenticator(apiClient, storage, credentials, localDateTimeSource);
    }

    private void mockDefaults() {
        mockNow("2021-01-01 12:00");
        mockSessionExpiryDate("2021-01-01 12:01");
        // when session expiry date is set, the consent creation ts is never used so it can be w/e
        mockStorageSessionData("psuId", "sessionId", DATE_WHATEVER);

        mockStorageAccounts(SAMPLE_ACCOUNT_RESPONSE);
        mockFetchBalancesResponse(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID, SAMPLE_BALANCE_RESPONSE);
    }

    @Test
    @Parameters(method = "scaWithoutRedirectParams")
    public void should_throw_when_no_sca_redirect_returned_from_client(ScaResponse scaResponse) {
        // given
        when(apiClient.getScaRedirect(anyString())).thenReturn(scaResponse);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.buildAuthorizeUrl(""));

        // then
        assertThat(throwable)
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(SparebankConstants.ErrorMessages.SCA_REDIRECT_MISSING);
    }

    @SuppressWarnings("unused")
    private Object[] scaWithoutRedirectParams() {
        return Stream.of(
                        "{}",
                        "{'_links': {}}",
                        "{'_links': {'scaRedirect': {}}}",
                        "{'_links': {'scaRedirect': {'href': ''}}}",
                        "{'_links': {'scaRedirect': {'href': '   '}}}")
                .map(json -> json.replaceAll("'", "\""))
                .map(json -> SerializationUtils.deserializeFromString(json, ScaResponse.class))
                .toArray();
    }

    @Test
    public void should_return_proper_sca_redirect_url() {
        // given
        String validScaResponse =
                "{'_links': {'scaRedirect': {'href': 'http://example.com'}}}".replaceAll("'", "\"");
        when(apiClient.getScaRedirect(anyString()))
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                validScaResponse, ScaResponse.class));

        // when
        URL url = authenticator.buildAuthorizeUrl("");

        // then
        assertEquals(new URL("http://example.com"), url.getUrl());
    }

    @Test
    public void should_return_session_has_not_expired_by_fetching_account_balances() {
        // given
        mockDefaults();

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isFalse();

        verifyFetchesBalancesFor(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID);
        verifyNoMoreInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void should_return_session_has_not_expired_by_fetching_card_balances() {
        // given
        mockDefaults();
        mockStorageAccounts(null);
        mockStorageCards(SAMPLE_CARD_RESPONSE);

        mockFetchBalancesResponse(FIRST_SAMPLE_CARD_RESOURCE_ID, SAMPLE_BALANCE_RESPONSE);

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isFalse();

        verifyFetchesBalancesFor(FIRST_SAMPLE_CARD_RESOURCE_ID);
        verifyNoMoreInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void
            should_return_session_has_not_expired_additionally_setting_missing_session_expiry_date() {
        // given
        mockDefaults();
        mockSessionExpiryDate(null);
        mockStorageSessionData("psuId123", "sessionId123", "2020-03-03 13:30");
        mockNow(stringToLocalDateTime("2020-03-03 13:29").plusDays(90));

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isFalse();

        verifyFetchesBalancesFor(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID);
        verifyNoMoreInteractions(apiClient);
        verifyUpdatesSessionExpiryDate(stringToLocalDate("2020-03-03 13:30").plusDays(90));
    }

    @Test
    @Parameters(method = "newSessionExpiryDateNotAfterCurrentTimeParams")
    public void
            should_return_session_has_expired_when_new_session_expiry_date_is_not_after_current_time(
                    String currentTimeMinus90Days, String consentCreationTime) {
        // given
        mockDefaults();
        mockSessionExpiryDate(null);
        mockStorageSessionData("psuId123", "sessionId123", consentCreationTime);
        mockNow(stringToLocalDateTime(currentTimeMinus90Days).plusDays(90));

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyZeroInteractions(apiClient);
        verifyUpdatesSessionExpiryDate(stringToLocalDate(consentCreationTime).plusDays(90));
    }

    @SuppressWarnings("unused")
    private Object[] newSessionExpiryDateNotAfterCurrentTimeParams() {
        return new Object[] {
            new Object[] {"2020-03-03 13:30", "2020-03-03 13:30"},
            new Object[] {"2020-03-03 13:31", "2020-03-03 13:30"}
        };
    }

    @Test
    @Parameters(method = "emptySessionDataParams")
    public void should_return_session_expired_when_some_session_data_are_missing(
            String psuId, String sessionId, String consentCreationTs) {
        // given
        mockDefaults();
        mockStorageSessionData(psuId, sessionId, consentCreationTs);

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyZeroInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @SuppressWarnings("unused")
    private Object[] emptySessionDataParams() {
        return new Object[] {
            new Object[] {null, "sessionId", DATE_WHATEVER},
            new Object[] {"psuId", null, DATE_WHATEVER},
            new Object[] {"psuId", "sessionId", null}
        };
    }

    @Test
    @Parameters(method = "sessionExpiryDateReachedParams")
    public void should_return_session_expired_when_session_expiry_date_was_reached(
            String now, String sessionExpiryDate) {
        // given
        mockDefaults();
        mockNow(now);
        mockSessionExpiryDate(sessionExpiryDate);

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyZeroInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @SuppressWarnings("unused")
    private Object[] sessionExpiryDateReachedParams() {
        return new Object[] {
            new Object[] {"2021-01-01 12:00", "2021-01-01 12:00"},
            new Object[] {"2021-01-01 12:00", "2021-01-01 11:59"}
        };
    }

    @Test
    public void should_return_session_expired_when_there_are_no_stored_accounts_or_cards() {
        // given
        mockDefaults();
        mockStorageAccounts(null);
        mockStorageCards(null);

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyZeroInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void should_return_session_expired_when_stored_accounts_or_cards_are_empty() {
        // given
        mockDefaults();
        mockStorageAccounts(new AccountResponse());
        mockStorageCards(new CardResponse());

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyZeroInteractions(apiClient);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void
            should_return_session_expired_when_fetching_balance_throws_token_expired_exception() {
        // given
        mockDefaults();
        mockFetchBalancesException(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID, new ConsentExpiredException());

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyFetchesBalancesFor(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void
            should_return_session_expired_when_fetching_balance_throws_token_revoked_exception() {
        // given
        mockDefaults();
        mockFetchBalancesException(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID, new ConsentRevokedException());

        // when
        boolean sessionExpired = authenticator.hasSessionExpired();

        // then
        assertThat(sessionExpired).isTrue();

        verifyFetchesBalancesFor(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @Test
    public void should_not_suppress_http_response_exception_when_checking_session_validity() {
        // given
        mockDefaults();

        HttpResponseException hre = mock(HttpResponseException.class);
        mockFetchBalancesException(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID, hre);

        // when
        Throwable throwable = catchThrowable(() -> authenticator.hasSessionExpired());

        // then
        assertThat(throwable).isEqualToComparingFieldByFieldRecursively(hre);

        verifyFetchesBalancesFor(FIRST_SAMPLE_ACCOUNT_RESOURCE_ID);
        verifyDoesntUpdateSessionExpiryDate();
    }

    @SuppressWarnings("SameParameterValue")
    private void mockStorageSessionData(
            @Nullable String psuId,
            @Nullable String tppSessionId,
            @Nullable String consentCreationDate) {
        when(storage.getPsuId()).thenReturn(Optional.ofNullable(psuId));
        when(storage.getSessionId()).thenReturn(Optional.ofNullable(tppSessionId));

        if (consentCreationDate != null) {
            when(storage.getConsentCreationTimestamp())
                    .thenReturn(Optional.of(stringToTimestamp(consentCreationDate)));
        } else {
            when(storage.getConsentCreationTimestamp()).thenReturn(Optional.empty());
        }
    }

    private void mockStorageAccounts(@Nullable AccountResponse accountResponse) {
        when(storage.getStoredAccounts()).thenReturn(Optional.ofNullable(accountResponse));
    }

    private void mockStorageCards(@Nullable CardResponse cardResponse) {
        when(storage.getStoredCards()).thenReturn(Optional.ofNullable(cardResponse));
    }

    private void mockNow(String date) {
        when(localDateTimeSource.now()).thenReturn(stringToLocalDateTime(date));
    }

    private void mockNow(LocalDateTime localDateTime) {
        when(localDateTimeSource.now()).thenReturn(localDateTime);
    }

    private void mockSessionExpiryDate(@Nullable String date) {
        when(credentials.getSessionExpiryDate())
                .thenReturn(date == null ? null : stringToDate(date));
    }

    @SuppressWarnings("SameParameterValue")
    private void mockFetchBalancesException(String resourceId, Exception exception) {
        when(apiClient.fetchBalances(eq(resourceId))).thenThrow(exception);
    }

    @SuppressWarnings("SameParameterValue")
    private void mockFetchBalancesResponse(String resourceId, BalanceResponse balanceResponse) {
        when(apiClient.fetchBalances(eq(resourceId))).thenReturn(balanceResponse);
    }

    private void verifyFetchesBalancesFor(String resourceId) {
        verify(apiClient).fetchBalances(resourceId);
    }

    private void verifyDoesntUpdateSessionExpiryDate() {
        verify(credentials, times(0)).setSessionExpiryDate(any(LocalDate.class));
        verify(credentials, times(0)).setSessionExpiryDate(any(LocalDateTime.class));
        verify(credentials, times(0)).setSessionExpiryDate(any(Date.class));
    }

    private void verifyUpdatesSessionExpiryDate(LocalDate sessionExpiryDate) {
        verify(credentials).setSessionExpiryDate(sessionExpiryDate);
    }

    private LocalDateTime stringToLocalDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDateTime.parse(date, formatter);
    }

    private LocalDate stringToLocalDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return LocalDate.parse(date, formatter);
    }

    private Date stringToDate(String date) {
        return Date.from(stringToLocalDateTime(date).atZone(ZoneId.systemDefault()).toInstant());
    }

    private Long stringToTimestamp(String date) {
        return stringToDate(date).getTime();
    }

    private ScaResponse getScaResponse() {
        return SerializationUtils.deserializeFromString(getScaResponseString(), ScaResponse.class);
    }

    private String getScaResponseString() {
        return "{\"_links\": {\"scaRedirect\": {\"href\": \"http://example.com\"}}}";
    }
}
