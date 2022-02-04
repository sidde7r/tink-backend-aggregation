package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankProfileSelector;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.LinkEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileParameters;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.credentials.service.CreateCredentialsRequest;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultApiClientTest {

    private TinkHttpClient client;
    private SwedbankConfiguration configuration;
    private SwedbankStorage swedbankStorage;
    private SwedbankProfileSelector profileSelector;
    private AgentComponentProvider componentProvider;
    private LinkEntity linkEntity;

    @Before
    public void setUp() {
        client = mock(TinkHttpClient.class);
        swedbankStorage = mock(SwedbankStorage.class);
        profileSelector = mock(SwedbankProfileSelector.class);
        componentProvider = mock(AgentComponentProvider.class);
        Credentials credentials = new Credentials();
        credentials.setField(Key.USERNAME, "username");
        CredentialsRequest credentialsRequest = new CreateCredentialsRequest();
        credentialsRequest.setCredentials(credentials);
        when(componentProvider.getCredentialsRequest()).thenReturn(credentialsRequest);
        linkEntity = new LinkEntity();
    }

    @Test
    public void
            shouldThrowNotCustomerExceptionWhenFallbackBothBanksButNoPrivateProfileInChosenBankSavingsBank() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("savingsbank-fallback", true), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getProfileResponseBothBanksPrivateInSwedbank())
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        Throwable throwable =
                Assertions.catchThrowable(() -> spyClient.completeAuthentication(linkEntity));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");

        Assert.assertEquals(
                "You do not have any accounts at Sparbankerna. Use Swedbank (Mobile BankID) instead.",
                ((LoginException) throwable).getUserMessage().get());
    }

    @Test
    public void
            shouldThrowNotCustomerExceptionWhenFallbackBothBanksButNoPrivateProfileInChosenBankSwedbank() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("swedbank-fallback", false), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getProfileResponseBothBanksPrivateInSavingsbank())
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        Throwable throwable =
                Assertions.catchThrowable(() -> spyClient.completeAuthentication(linkEntity));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");

        Assert.assertEquals(
                "You do not have any accounts at Swedbank. Use Sparbankerna (Mobile BankID) instead.",
                ((LoginException) throwable).getUserMessage().get());
    }

    @Test
    public void shouldReturnProfileWhenFallbackBothBanksAndPrivateProfileInChosenBank() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("swedbank-fallback", true), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getProfileResponseBothBanksPrivateInSwedbank())
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        ProfileResponse result = spyClient.completeAuthentication(linkEntity);

        Assert.assertTrue(result.isHasSavingbankProfile());
        Assert.assertTrue(result.isHasSwedbankProfile());
        Optional<BankEntity> bankEntity =
                result.getBanks().stream()
                        .filter(bank -> bank.getPrivateProfile() != null)
                        .findFirst();

        Assert.assertTrue(
                bankEntity
                        .get()
                        .getPrivateProfile()
                        .getBankName()
                        .toLowerCase()
                        .contains("swedbank"));
    }

    @Test
    public void shouldThrowNotCustomerExceptionWhenBusinessOneBankButWrongChosenBank() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("savingsbank-business-ob", true), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getProfileResponseOneBank(true, false))
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        Throwable throwable =
                Assertions.catchThrowable(() -> spyClient.completeAuthentication(linkEntity));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");

        Assert.assertEquals(
                "You do not have any accounts at Sparbankerna. Use Swedbank (Mobile BankID) instead.",
                ((LoginException) throwable).getUserMessage().get());
    }

    @Test
    public void shouldThrowNotCustomerExceptionWhenReOneBankButWrongChosenBank() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("swedbank-bankid", false), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getProfileResponseOneBank(false, true))
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        Throwable throwable =
                Assertions.catchThrowable(() -> spyClient.completeAuthentication(linkEntity));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");

        Assert.assertEquals(
                "You do not have any accounts at Swedbank. Use Sparbankerna (Mobile BankID) instead.",
                ((LoginException) throwable).getUserMessage().get());
    }

    @Test
    public void shouldThrowNotCustomerExceptionWhenFallbackOneBankButNoPrivateProfileAtAll() {
        configuration =
                new SwedbankConfiguration(
                        getProfileParameters("swedbank-fallback", false), "host", true);
        SwedbankDefaultApiClient apiClient =
                new SwedbankDefaultApiClient(
                        client, configuration, swedbankStorage, profileSelector, componentProvider);
        SwedbankDefaultApiClient spyClient = spy(apiClient);

        doReturn(getOnlyServiceProfile())
                .when(spyClient)
                .makeRequest(linkEntity, ProfileResponse.class, false);

        Throwable throwable =
                Assertions.catchThrowable(() -> spyClient.completeAuthentication(linkEntity));

        assertThat(throwable)
                .isExactlyInstanceOf(LoginException.class)
                .hasMessage("Cause: LoginError.NOT_CUSTOMER");

        Assert.assertEquals(
                "You don't have any commitments in the selected bank.",
                ((LoginException) throwable).getUserMessage().get());
    }

    private ProfileParameters getProfileParameters(String providerName, boolean isSavingsbank) {
        return new ProfileParameters(providerName, "apiKey", isSavingsbank, "userAgent");
    }

    private ProfileResponse getProfileResponseBothBanksPrivateInSavingsbank() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "    \"banks\":"
                        + "    ["
                        + "        {"
                        + "            \"name\": \"Bergslagens Sparbank AB\","
                        + "            \"bankId\": \"08999\","
                        + "            \"url\": \"https://www.bergslagenssparbank.se\","
                        + "            \"privateProfile\":"
                        + "            {"
                        + "                \"activeProfileLanguage\": \"sv\","
                        + "                \"targetType\": \"PRIVATE\","
                        + "                \"customerName\": \"Esbjorn Fakename\","
                        + "                \"customerNumber\": \"personalNumber\","
                        + "                \"id\": \"Id1\","
                        + "                \"bankId\": \"08999\","
                        + "                \"bankName\": \"Bergslagens Sparbank AB\","
                        + "                \"url\": \"https://www.bergslagenssparbank.se\","
                        + "                \"customerInternational\": false,"
                        + "                \"youthProfile\": false,"
                        + "                \"links\":"
                        + "                {"
                        + "                    \"edit\":"
                        + "                    {"
                        + "                        \"method\": \"PUT\","
                        + "                        \"uri\": \"/v5/profile/subscription/Id1\""
                        + "                    },"
                        + "                    \"next\":"
                        + "                    {"
                        + "                        \"method\": \"POST\","
                        + "                        \"uri\": \"/v5/profile/Id1\""
                        + "                    }"
                        + "                }"
                        + "            },"
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Entreprenad AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber\","
                        + "                    \"id\": \"Id2\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id2\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id2\""
                        + "                        }"
                        + "                    }"
                        + "                },"
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Entreprenad Kommanditbolag\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber2\","
                        + "                    \"id\": \"Id3\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id3\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id3\""
                        + "                        }"
                        + "                    }"
                        + "                },"
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Rats AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber3\","
                        + "                    \"id\": \"Id4\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id4\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id4\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        },"
                        + "        {"
                        + "            \"name\": \"Bergslagens Sparbank AB\","
                        + "            \"bankId\": \"08191\","
                        + "            \"url\": \"https://www.bergslagenssparbank.se\","
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Cat Entreprenad AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber4\","
                        + "                    \"id\": \"Id5\","
                        + "                    \"bankId\": \"08191\","
                        + "                    \"bankName\": \"Bergslagens Sparbank AB\","
                        + "                    \"url\": \"https://www.bergslagenssparbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id5\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id5\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        },"
                        + "        {"
                        + "            \"name\": \"Ulricehamns Sparbank\","
                        + "            \"bankId\": \"08380\","
                        + "            \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Fastigheter AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber5\","
                        + "                    \"id\": \"Id6\","
                        + "                    \"bankId\": \"08380\","
                        + "                    \"bankName\": \"Ulricehamns Sparbank\","
                        + "                    \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id6\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id6\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        }"
                        + "    ],"
                        + "    \"hasSwedbankProfile\": true,"
                        + "    \"hasSavingbankProfile\": true,"
                        + "    \"userId\": \"**HASHED:XH**\""
                        + "}",
                ProfileResponse.class);
    }

    private ProfileResponse getProfileResponseBothBanksPrivateInSwedbank() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "    \"banks\":"
                        + "    ["
                        + "        {"
                        + "            \"name\": \"Swedbank AB (publ)\","
                        + "            \"bankId\": \"08999\","
                        + "            \"url\": \"https://www.swedbank.se\","
                        + "            \"privateProfile\":"
                        + "            {"
                        + "                \"activeProfileLanguage\": \"sv\","
                        + "                \"targetType\": \"PRIVATE\","
                        + "                \"customerName\": \"Esbjorn Fakename\","
                        + "                \"customerNumber\": \"personalNumber\","
                        + "                \"id\": \"Id1\","
                        + "                \"bankId\": \"08999\","
                        + "                \"bankName\": \"Swedbank AB (publ)\","
                        + "                \"url\": \"https://www.swedbank.se\","
                        + "                \"customerInternational\": false,"
                        + "                \"youthProfile\": false,"
                        + "                \"links\":"
                        + "                {"
                        + "                    \"edit\":"
                        + "                    {"
                        + "                        \"method\": \"PUT\","
                        + "                        \"uri\": \"/v5/profile/subscription/Id1\""
                        + "                    },"
                        + "                    \"next\":"
                        + "                    {"
                        + "                        \"method\": \"POST\","
                        + "                        \"uri\": \"/v5/profile/Id1\""
                        + "                    }"
                        + "                }"
                        + "            },"
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Entreprenad AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber\","
                        + "                    \"id\": \"Id2\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id2\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id2\""
                        + "                        }"
                        + "                    }"
                        + "                },"
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Entreprenad Kommanditbolag\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber2\","
                        + "                    \"id\": \"Id3\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id3\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id3\""
                        + "                        }"
                        + "                    }"
                        + "                },"
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Rats AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber3\","
                        + "                    \"id\": \"Id4\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id4\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id4\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        },"
                        + "        {"
                        + "            \"name\": \"Bergslagens Sparbank AB\","
                        + "            \"bankId\": \"08191\","
                        + "            \"url\": \"https://www.bergslagenssparbank.se\","
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Cat Entreprenad AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber4\","
                        + "                    \"id\": \"Id5\","
                        + "                    \"bankId\": \"08191\","
                        + "                    \"bankName\": \"Bergslagens Sparbank AB\","
                        + "                    \"url\": \"https://www.bergslagenssparbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id5\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id5\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        },"
                        + "        {"
                        + "            \"name\": \"Ulricehamns Sparbank\","
                        + "            \"bankId\": \"08380\","
                        + "            \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Fastigheter AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CorpNumber5\","
                        + "                    \"id\": \"Id6\","
                        + "                    \"bankId\": \"08380\","
                        + "                    \"bankName\": \"Ulricehamns Sparbank\","
                        + "                    \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id6\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id6\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        }"
                        + "    ],"
                        + "    \"hasSwedbankProfile\": true,"
                        + "    \"hasSavingbankProfile\": true,"
                        + "    \"userId\": \"**HASHED:XH**\""
                        + "}",
                ProfileResponse.class);
    }

    private ProfileResponse getProfileResponseOneBank(boolean isSwedbank, boolean isSavingsbank) {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "    \"banks\":"
                        + "    ["
                        + "        {"
                        + "            \"name\": \"Swedbank AB (publ)\","
                        + "            \"bankId\": \"08999\","
                        + "            \"url\": \"https://www.swedbank.se\","
                        + "            \"privateProfile\":"
                        + "            {"
                        + "                \"activeProfileLanguage\": \"sv\","
                        + "                \"targetType\": \"PRIVATE\","
                        + "                \"customerName\": \"Esbjorn Fakename\","
                        + "                \"customerNumber\": \"CustomerNumber1\","
                        + "                \"id\": \"Id1\","
                        + "                \"bankId\": \"08999\","
                        + "                \"bankName\": \"Swedbank AB (publ)\","
                        + "                \"url\": \"https://www.swedbank.se\","
                        + "                \"customerInternational\": false,"
                        + "                \"youthProfile\": false,"
                        + "                \"links\":"
                        + "                {"
                        + "                    \"edit\":"
                        + "                    {"
                        + "                        \"method\": \"PUT\","
                        + "                        \"uri\": \"/v5/profile/subscription/Id1\""
                        + "                    },"
                        + "                    \"next\":"
                        + "                    {"
                        + "                        \"method\": \"POST\","
                        + "                        \"uri\": \"/v5/profile/Id1\""
                        + "                    }"
                        + "                }"
                        + "            },"
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Screen AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CustomerNumber2\","
                        + "                    \"id\": \"Id2\","
                        + "                    \"bankId\": \"08999\","
                        + "                    \"bankName\": \"Swedbank AB (publ)\","
                        + "                    \"url\": \"https://www.swedbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/id2\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/id2\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        },"
                        + "        {"
                        + "            \"name\": \"Ulricehamns Sparbank\","
                        + "            \"bankId\": \"08380\","
                        + "            \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "            \"corporateProfiles\":"
                        + "            ["
                        + "                {"
                        + "                    \"activeProfileName\": \"Esbjorn Fastigheter AB\","
                        + "                    \"activeProfileLanguage\": \"sv\","
                        + "                    \"targetType\": \"CORPORATE\","
                        + "                    \"customerName\": \"Esbjorn Fakename\","
                        + "                    \"customerNumber\": \"CustomerNumber3\","
                        + "                    \"id\": \"Id3\","
                        + "                    \"bankId\": \"08380\","
                        + "                    \"bankName\": \"Ulricehamns Sparbank\","
                        + "                    \"url\": \"https://www.ulricehamnssparbank.se\","
                        + "                    \"customerInternational\": false,"
                        + "                    \"youthProfile\": false,"
                        + "                    \"links\":"
                        + "                    {"
                        + "                        \"edit\":"
                        + "                        {"
                        + "                            \"method\": \"PUT\","
                        + "                            \"uri\": \"/v5/profile/subscription/Id3\""
                        + "                        },"
                        + "                        \"next\":"
                        + "                        {"
                        + "                            \"method\": \"POST\","
                        + "                            \"uri\": \"/v5/profile/Id3\""
                        + "                        }"
                        + "                    }"
                        + "                }"
                        + "            ]"
                        + "        }"
                        + "    ],"
                        + "    \"hasSwedbankProfile\": "
                        + isSwedbank
                        + "    ,\"hasSavingbankProfile\": "
                        + isSavingsbank
                        + "    ,\"userId\": \"**HASHED:XH**\""
                        + "}",
                ProfileResponse.class);
    }

    private ProfileResponse getOnlyServiceProfile() {
        return SerializationUtils.deserializeFromString(
                "{"
                        + "  \"banks\": ["
                        + "    {"
                        + "      \"name\": \"Hälsinglands Sparbank\","
                        + "      \"bankId\": \"08129\","
                        + "      \"url\": \"https://www.halsinglandssparbank.se\","
                        + "      \"corporateProfiles\": [],"
                        + "      \"servicePortalProfile\": {"
                        + "        \"activeProfileLanguage\": \"sv\","
                        + "        \"numberOfUnreadDocuments\": 2,"
                        + "        \"targetType\": \"NEUTRAL\","
                        + "        \"customerName\": \"Esbjorn Fakename\","
                        + "        \"customerNumber\": \"customerNumber\","
                        + "        \"id\": \"Id\","
                        + "        \"bankId\": \"08129\","
                        + "        \"bankName\": \"Hälsinglands Sparbank\","
                        + "        \"url\": \"https://www.halsinglandssparbank.se\","
                        + "        \"customerInternational\": false,"
                        + "        \"youthProfile\": false,"
                        + "        \"links\": {"
                        + "          \"edit\": {"
                        + "            \"method\": \"PUT\","
                        + "            \"uri\": \"/v5/profile/subscription/Id\""
                        + "          },"
                        + "          \"next\": {"
                        + "            \"method\": \"POST\","
                        + "            \"uri\": \"/v5/profile/Id\""
                        + "          }"
                        + "        }"
                        + "      }"
                        + "    }"
                        + "  ],"
                        + "  \"hasSwedbankProfile\": false,"
                        + "  \"hasSavingbankProfile\": true,"
                        + "  \"userId\": \"**HASHED:Xx**\""
                        + "}",
                ProfileResponse.class);
    }
}
