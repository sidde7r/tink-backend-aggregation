package se.tink.backend.aggregation.agents.nxgen.de.banks.fints;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import java.util.ArrayList;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.Bank;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsSecretsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.base64.FinTsBase64;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.BaseRequestPart;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKIDNv2;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKTANv6;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.HKVVBv3;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class FinTsRequestProcessorTest {
    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private void initWireMockForScenarioWithTAN() {
        WireMock.stubFor(
                WireMock.post(urlEqualTo("/foo/bar"))
                        .inScenario("Processing request that requires TAN")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(
                                WireMock.aResponse().withBody(getResponseStatingTanIsRequired()))
                        .willSetStateTo("Received response saying TAN is needed"));

        WireMock.stubFor(
                WireMock.post(urlEqualTo("/foo/bar"))
                        .inScenario("Processing request that requires TAN")
                        .whenScenarioStateIs("Received response saying TAN is needed")
                        .willReturn(
                                WireMock.aResponse().withBody(getResponseWithResultsAfterTAN())));
    }

    private void initWireMockForScenarioWithoutAnyTAN() {
        WireMock.stubFor(
                WireMock.post(urlEqualTo("/foo/bar"))
                        .inScenario("Processing request that DOES NOT requires TAN")
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willReturn(
                                WireMock.aResponse().withBody(getResponseStatingTanIsNotRequired()))
                        .willSetStateTo("Received response saying TAN is NOT needed"));

        WireMock.stubFor(
                WireMock.post(urlEqualTo("/foo/bar"))
                        .inScenario("Processing request that DOES NOT requires TAN")
                        .whenScenarioStateIs("Received response saying TAN is NOT needed")
                        .willReturn(
                                WireMock.aResponse()
                                        .withBody("This response should never be returned!")));
    }

    @Test
    public void shouldGetProperResponseForOperationRequiringTAN() {
        // given
        initWireMockForScenarioWithTAN();
        String url = String.format("http://localhost:%d/foo/bar", wireMock.port());
        FinTsConfiguration configuration =
                new FinTsConfiguration("foo", Bank.POSTBANK, url, "foo", "foo");
        FinTsDialogContext context =
                new FinTsDialogContext(configuration, new FinTsSecretsConfiguration(null, null));
        FinTsRequestProcessor processor = createRequestProcessor(context, configuration);

        // when
        FinTsResponse response = processor.process(getRequestThatRequireTAN(context));

        // then
        assertThat(response)
                .isEqualTo(
                        new FinTsResponse(
                                FinTsBase64.decodeResponseFromBase64(
                                        getResponseWithResultsAfterTAN())));
    }

    @Test
    public void shouldGetProperResponseForOperationNotRequiringTAN() {
        // given
        initWireMockForScenarioWithoutAnyTAN();
        String url = String.format("http://localhost:%d/foo/bar", wireMock.port());
        FinTsConfiguration configuration =
                new FinTsConfiguration("foo", Bank.POSTBANK, url, "foo", "foo");
        FinTsDialogContext context =
                new FinTsDialogContext(configuration, new FinTsSecretsConfiguration(null, null));
        FinTsRequestProcessor processor = createRequestProcessor(context, configuration);

        // when
        FinTsResponse response = processor.process(getRequestStatingDoesNotRequireTAN(context));

        // then
        assertThat(response)
                .isEqualTo(
                        new FinTsResponse(
                                FinTsBase64.decodeResponseFromBase64(
                                        getResponseStatingTanIsNotRequired())));
    }

    private FinTsRequestProcessor createRequestProcessor(
            FinTsDialogContext context, FinTsConfiguration configuration) {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();

        FinTsRequestSender sender = new FinTsRequestSender(httpClient, configuration.getEndpoint());
        return new FinTsRequestProcessor(context, sender, getTanAnswerProvider());
    }

    private TanAnswerProvider getTanAnswerProvider() {
        TanAnswerProvider answerProvider = mock(TanAnswerProvider.class);
        when(answerProvider.getTanAnswer("dummyTanMedium")).thenReturn("answer");
        return answerProvider;
    }

    private FinTsRequest getRequestThatRequireTAN(FinTsDialogContext context) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
        additionalSegments.add(
                HKIDNv2.builder()
                        .systemId(context.getSystemId())
                        .blz(context.getConfiguration().getBlz())
                        .username(context.getConfiguration().getUsername())
                        .build());
        additionalSegments.add(
                HKVVBv3.builder().productId("123456789").productVersion("0.1").build());
        additionalSegments.add(
                HKTANv6.builder()
                        .tanProcessVariant(HKTANv6.TanProcessVariant.TAN_INITIALIZE_SINGLE)
                        .segmentType(SegmentType.HKIDN)
                        .tanMediumName("Google Phone")
                        .build());
        return FinTsRequest.createEncryptedRequest(context, additionalSegments);
    }

    private FinTsRequest getRequestStatingDoesNotRequireTAN(FinTsDialogContext context) {
        List<BaseRequestPart> additionalSegments = new ArrayList<>();
        additionalSegments.add(
                HKIDNv2.builder()
                        .systemId(context.getSystemId())
                        .blz(context.getConfiguration().getBlz())
                        .username(context.getConfiguration().getUsername())
                        .build());
        additionalSegments.add(
                HKVVBv3.builder().productId("123456789").productVersion("0.1").build());
        return FinTsRequest.createEncryptedRequest(context, additionalSegments);
    }

    private String getResponseStatingTanIsRequired() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDYyMyszMDArNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9\n"
                + "KzQrNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9OjQnSE5WU0s6OTk4OjMrUElOOjIrOTk4\n"
                + "KzErMjo6c2RTMHBWSnJ3M0FCQUFDa1l4dUpwVmtYckFRQSsxOjIwMjAwMzEwOjA5MTQwOSsyOjI6\n"
                + "MTM6QDhAMDAwMDAwMDA6NToxKzI4MDo3NTA1MDAwMDo4NDYxMTYyLTAzOlY6MDowKzAnSE5WU0Q6\n"
                + "OTk5OjErQDM3MUBITlNISzoyOjQrUElOOjIrOTIxKzQyNjc1ODErMSsxKzI6OnNkUzBwVkpydzNB\n"
                + "QkFBQ2tZeHVKcFZrWHJBUUErMSsxOjIwMjAwMzEwOjA5MTQwOSsxOjk5OToxKzY6MTA6MTYrMjgw\n"
                + "Ojc1MDUwMDAwOjg0NjExNjItMDM6UzowOjAnSElSTUc6MzoyKzAwMTA6Ok5hY2hyaWNodCBlbnRn\n"
                + "ZWdlbmdlbm9tbWVuLidISVJNUzo0OjI6NCswMDMwOjpBdWZ0cmFnIGVtcGZhbmdlbiAtIEJpdHRl\n"
                + "IGRpZSBlbXBmYW5nZW5lIFRBTiBlaW5nZWJlbi4oTUJUNjI4MjAyMDAwMDIpJ0hJVEFOOjU6Njo0\n"
                + "KzQrKzkyMzUtMDMtMTAtMDkuMTQuMDkuMjA4OTUwK0JpdHRlIGdlYmVuIFNpZSBkaWUgcHVzaFRB\n"
                + "TiBlaW4uKysrR29vZ2xlIFBob25lJ0hOU0hBOjY6Mis0MjY3NTgxJydITkhCUzo3OjErNCc=";
    }

    private String getResponseWithResultsAfterTAN() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDc0NiszMDArNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9\n"
                + "KzMrNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9OjMnSE5WU0s6OTk4OjMrUElOOjIrOTk4\n"
                + "KzErMjo6c2RTMHBWSnJ3M0FCQUFDa1l4dUpwVmtYckFRQSsxOjIwMjAwMzEwOjA5MTQwOSsyOjI6\n"
                + "MTM6QDhAMDAwMDAwMDA6NToxKzI4MDo3NTA1MDAwMDo4NDYxMTYyLTAzOlY6MDowKzAnSE5WU0Q6\n"
                + "OTk5OjErQDQ5NEBITlNISzoyOjQrUElOOjIrOTIxKzYzMTQ0ODkrMSsxKzI6OnNkUzBwVkpydzNB\n"
                + "QkFBQ2tZeHVKcFZrWHJBUUErMSsxOjIwMjAwMzEwOjA5MTQwOSsxOjk5OToxKzY6MTA6MTYrMjgw\n"
                + "Ojc1MDUwMDAwOjg0NjExNjItMDM6UzowOjAnSElSTUc6MzoyKzMwNjA6OkJpdHRlIGJlYWNodGVu\n"
                + "IFNpZSBkaWUgZW50aGFsdGVuZW4gV2FybnVuZ2VuL0hpbndlaXNlLidISVJNUzo0OjI6MyswMDIw\n"
                + "OjpEZXIgQXVmdHJhZyB3dXJkZSBhdXNnZWb8aHJ0LidISVJNUzo1OjI6NCszMDc2OjpTdGFya2Ug\n"
                + "S3VuZGVuYXV0aGVudGlmaXppZXJ1bmcgbmljaHQgbm90d2VuZGlnLidISVRBTjo2OjY6NCs0Kytu\n"
                + "b3JlZitub2NoYWxsZW5nZSsrK0dvb2dsZSBQaG9uZSdISVNBTDo3OjU6Mys4NDYxMTYyOjoyODA6\n"
                + "NzUwNTAwMDArU3R1ZGVudGVua29udG8rRVVSK0M6MTE0MCw3MzpFVVI6MjAyMDAzMTArQzowLDpF\n"
                + "VVI6MjAyMDAzMTArMCw6RVVSKzExNDAsNzM6RVVSJ0hOU0hBOjg6Mis2MzE0NDg5JydITkhCUzo5\n"
                + "OjErMyc=";
    }

    private String getResponseStatingTanIsNotRequired() {
        return "SE5IQks6MTozKzAwMDAwMDAwMDc0NiszMDArNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9\n"
                + "KzMrNjYxMTQ4MzA2ODkwPTQ4NTg0MjMzODYyMkJMQUE9OjMnSE5WU0s6OTk4OjMrUElOOjIrOTk4\n"
                + "KzErMjo6c2RTMHBWSnJ3M0FCQUFDa1l4dUpwVmtYckFRQSsxOjIwMjAwMzEwOjA5MTQwOSsyOjI6\n"
                + "MTM6QDhAMDAwMDAwMDA6NToxKzI4MDo3NTA1MDAwMDo4NDYxMTYyLTAzOlY6MDowKzAnSE5WU0Q6\n"
                + "OTk5OjErQDQ5NEBITlNISzoyOjQrUElOOjIrOTIxKzYzMTQ0ODkrMSsxKzI6OnNkUzBwVkpydzNB\n"
                + "QkFBQ2tZeHVKcFZrWHJBUUErMSsxOjIwMjAwMzEwOjA5MTQwOSsxOjk5OToxKzY6MTA6MTYrMjgw\n"
                + "Ojc1MDUwMDAwOjg0NjExNjItMDM6UzowOjAnSElSTUc6MzoyKzMwNjA6OkJpdHRlIGJlYWNodGVu\n"
                + "IFNpZSBkaWUgZW50aGFsdGVuZW4gV2FybnVuZ2VuL0hpbndlaXNlLidISVJNUzo0OjI6MyswMDIw\n"
                + "OjpEZXIgQXVmdHJhZyB3dXJkZSBhdXNnZWb8aHJ0LidISVJNUzo1OjI6NCszMDc2OjpTdGFya2Ug\n"
                + "S3VuZGVuYXV0aGVudGlmaXppZXJ1bmcgbmljaHQgbm90d2VuZGlnLidISVRBTjo2OjY6NCs0Kytu\n"
                + "b3JlZitub2NoYWxsZW5nZSsrK0dvb2dsZSBQaG9uZSdISVNBTDo3OjU6Mys4NDYxMTYyOjoyODA6\n"
                + "NzUwNTAwMDArU3R1ZGVudGVua29udG8rRVVSK0M6MTE0MCw3MzpFVVI6MjAyMDAzMTArQzowLDpF\n"
                + "VVI6MjAyMDAzMTArMCw6RVVSKzExNDAsNzM6RVVSJ0hOU0hBOjg6Mis2MzE0NDg5JydITkhCUzo5\n"
                + "OjErMyc=";
    }
}
