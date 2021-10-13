package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankProfileSelector;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultSessionHandlerTest {

    private SwedbankDefaultApiClient spyClient;
    SwedbankDefaultSessionHandler sessionHandler;
    AgentComponentProvider agentComponentProvider;

    @Before
    public void setup() {
        TinkHttpClient tinkHttpClient = mock(TinkHttpClient.class);
        SwedbankConfiguration swedbankConfiguration = mock(SwedbankConfiguration.class);
        SwedbankStorage swedbankStorage = mock(SwedbankStorage.class);
        SwedbankProfileSelector swedbankProfileSelector = mock(SwedbankProfileSelector.class);
        agentComponentProvider = mock(AgentComponentProvider.class);
        setUpUsername();
        when(swedbankConfiguration.getHost()).thenReturn("random host");
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        tinkHttpClient,
                        swedbankConfiguration,
                        swedbankStorage,
                        swedbankProfileSelector,
                        agentComponentProvider);
        spyClient = spy(apiClient);
        sessionHandler = new SwedbankDefaultSessionHandler(spyClient);
    }

    @Test
    public void shouldCompleteAuthIfResponseIsNotNullOrEmpty() {
        TouchResponse response = getNotEmptyResponse();

        doReturn(response).when(spyClient).touch();

        doReturn(getProfileResponse()).when(spyClient).completeAuthentication(any());
        try {
            sessionHandler.keepAlive();
            Assert.assertTrue(Boolean.TRUE);

        } catch (HttpResponseException ex) {
            Assert.fail(ex.getMessage());
        }
    }

    @Test
    public void shouldThrowSessionExpiredIfResponseBankIdIsNullOrEmpty() {
        TouchResponse response = getEmptyBankIdResponse();

        doReturn(response).when(spyClient).touch();

        Throwable throwable = catchThrowable(() -> sessionHandler.keepAlive());

        assertThat(throwable)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    @Test
    public void shouldThrowSessionExpiredIfResponseChosenProfileIsNullOrEmpty() {
        TouchResponse response = getEmptyBankIdResponse();

        doReturn(response).when(spyClient).touch();

        Throwable throwable = catchThrowable(() -> sessionHandler.keepAlive());

        assertThat(throwable)
                .isExactlyInstanceOf(SessionException.class)
                .hasMessage("Cause: SessionError.SESSION_EXPIRED");
    }

    public void setUpUsername() {
        CredentialsRequest credentialsRequest = mock(CredentialsRequest.class);
        when(agentComponentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);
        Credentials credentials = mock(Credentials.class);
        when(credentialsRequest.getCredentials()).thenReturn(credentials);
        when(agentComponentProvider.getCredentialsRequest().getCredentials().getField(Key.USERNAME))
                .thenReturn("random host");
    }

    public TouchResponse getNotEmptyResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"chosenProfileName\":\"identifiedUserName\",\"identifiedUserName\":\"identifiedUser\",\"identifiedUser\":\"***MASKED***\",\"chosenProfile\":\"***MASKED***\",\"authMethodName\":\"BANKID_MOBILE\",\"authMethodDescription\":\"Mobile BankID\",\"authMethodExtendedUsage\":false,\"bankId\":\"08999\",\"bankName\":\"Swedbank AB (publ)\",\"chosenProfileLanguage\":\"sv\",\"client\":{\"software\":{\"name\":\"Tink\",\"version\":\"unknown\",\"family\":\"OTHER\"},\"os\":{\"name\":\"+https://www.tink.se/\",\"version\":\"noc@tink.se)\",\"family\":\"OTHER\"},\"device\":{\"manufacturer\":\"unknown\",\"model\":\"unknown\"}},\"authenticationRole\":\"CUSTOMER\",\"serverTime\":\"20200617 08:15\",\"formattedServerTime\":\"2020-06-17 08:15\"}",
                TouchResponse.class);
    }

    public TouchResponse getChosenProfileEmptyResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"chosenProfileName\":\"identifiedUserName\",\"identifiedUserName\":\"identifiedUser\",\"identifiedUser\":\"***MASKED***\",\"chosenProfile\":\"\",\"authMethodName\":\"BANKID_MOBILE\",\"authMethodDescription\":\"Mobile BankID\",\"authMethodExtendedUsage\":false,\"bankId\":\"08999\",\"bankName\":\"Swedbank AB (publ)\",\"chosenProfileLanguage\":\"sv\",\"client\":{\"software\":{\"name\":\"Tink\",\"version\":\"unknown\",\"family\":\"OTHER\"},\"os\":{\"name\":\"+https://www.tink.se/\",\"version\":\"noc@tink.se)\",\"family\":\"OTHER\"},\"device\":{\"manufacturer\":\"unknown\",\"model\":\"unknown\"}},\"authenticationRole\":\"CUSTOMER\",\"serverTime\":\"20200617 08:15\",\"formattedServerTime\":\"2020-06-17 08:15\"}",
                TouchResponse.class);
    }

    public TouchResponse getEmptyBankIdResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"chosenProfileName\":\"identifiedUserName\",\"identifiedUserName\":\"identifiedUser\",\"identifiedUser\":\"***MASKED***\",\"chosenProfile\":\"***MASKED***\",\"authMethodName\":\"BANKID_MOBILE\",\"authMethodDescription\":\"Mobile BankID\",\"authMethodExtendedUsage\":false,\"bankId\":\"\",\"bankName\":\"Swedbank AB (publ)\",\"chosenProfileLanguage\":\"sv\",\"client\":{\"software\":{\"name\":\"Tink\",\"version\":\"unknown\",\"family\":\"OTHER\"},\"os\":{\"name\":\"+https://www.tink.se/\",\"version\":\"noc@tink.se)\",\"family\":\"OTHER\"},\"device\":{\"manufacturer\":\"unknown\",\"model\":\"unknown\"}},\"authenticationRole\":\"CUSTOMER\",\"serverTime\":\"20200617 08:15\",\"formattedServerTime\":\"2020-06-17 08:15\"}",
                TouchResponse.class);
    }

    public ProfileResponse getProfileResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"userId\":\"identifiedUserId\",\"hasSwedbankProfile\":true,\"hasSavingbankProfile\":true,\"hasSavingbankProfile\":true}",
                ProfileResponse.class);
    }
}
