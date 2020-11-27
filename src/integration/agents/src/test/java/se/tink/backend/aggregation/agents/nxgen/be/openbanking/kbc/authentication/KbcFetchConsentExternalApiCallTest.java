package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.time.LocalDate;
import java.util.HashMap;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.AuthenticationPersistedDataCookieStoreAccessorFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.http.ExternalApiCallResult;
import se.tink.backend.aggregation.wiremock.WireMockIntegrationTest;

public class KbcFetchConsentExternalApiCallTest extends WireMockIntegrationTest {

    @Test
    public void shouldFetchNewConsent() {
        // given
        LocalDate expectedValidUntilDate = LocalDate.now().plusYears(1).minusMonths(1);
        final String expectedRequestBody =
                "{\"access\":{\"accounts\":[],\"transactions\":[{\"iban\":\"BE68539007547034\"}],\"balances\":[{\"iban\":\"BE68539007547034\"}]},\"recurringIndicator\":true,\"validUntil\":\""
                        + expectedValidUntilDate
                        + "\",\"frequencyPerDay\":4,\"combinedServiceIndicator\":false}";
        WireMock.stubFor(
                WireMock.post(WireMock.urlPathEqualTo("/psd2/v2/consents"))
                        .withHeader("PSU-IP-Address", WireMock.equalTo("0.0.0.0"))
                        .withHeader(
                                "X-Request-ID",
                                WireMock.matching(
                                        "[0-9a-fA-F]{8}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{4}\\-[0-9a-fA-F]{12}"))
                        .withRequestBody(WireMock.equalTo(expectedRequestBody))
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody(
                                                "{\"consentStatus\":\"received\",\"consentId\":\"4017482272\",\"_links\":{}}")
                                        .withHeader("Content-Type", "application/json")));
        KbcFetchConsentExternalApiCallParameters inputParameters =
                new KbcFetchConsentExternalApiCallParameters(
                        "BE68539007547034", "https://redirectUrl", "0.0.0.0");
        KbcFetchConsentExternalApiCall objectUnderTest =
                new KbcFetchConsentExternalApiCall(
                        new AgentPlatformHttpClient(httpClient), getOrigin());

        // when
        ExternalApiCallResult<String> result =
                objectUnderTest.execute(
                        inputParameters,
                        null,
                        AuthenticationPersistedDataCookieStoreAccessorFactory.create(
                                new AgentAuthenticationPersistedData(new HashMap<>())));

        // then
        Assert.assertTrue(result.getResponse().isPresent());
        Assertions.assertThat(result.getResponse().get()).isEqualTo("4017482272");
    }
}
