package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.transaction;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestSender;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.request.FinTsRequest;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HICAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIKAZ;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class TransactionClientTest {

    private static final TransactionRequestBuilder dummyRequestBuilder =
            (lamdaDialogContext, account, startingPoint) ->
                    FinTsRequest.createEncryptedRequest(
                            lamdaDialogContext, Collections.emptyList());

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private FinTsConfiguration configuration;
    private FinTsDialogContext dialogContext;

    @Before
    public void setup() {
        configuration = TestFixtures.getFinTsConfiguration(wireMock.port());
        dialogContext = TestFixtures.getDialogContext(configuration);
    }

    @Test
    public void shouldGetTransactionDetailsWithMT940Format() {
        // given
        initWireMock(TestFixtures.getBodyOfFetchTransactionsResponseInMT940Format());
        TransactionClient client = new TransactionClient(createRequestProcessor(), dialogContext);
        FinTsAccountInformation accountInformation = TestFixtures.getAccountInformation();

        // when
        List<FinTsResponse> responses =
                client.getTransactionResponses(dummyRequestBuilder, accountInformation);

        assertThat(responses).hasSize(1);
        boolean hasTransactionDetails = responses.get(0).findSegment(HIKAZ.class).isPresent();
        assertThat(hasTransactionDetails).isTrue();
    }

    @Test
    public void shouldGetTransactionDetailsWithXMLFormat() {
        // given
        initWireMock(TestFixtures.getBodyOfFetchTransactionsResponseInXMLFormat());
        TransactionClient client = new TransactionClient(createRequestProcessor(), dialogContext);
        FinTsAccountInformation accountInformation = TestFixtures.getAccountInformation();

        // when
        List<FinTsResponse> responses =
                client.getTransactionResponses(dummyRequestBuilder, accountInformation);

        // then
        assertThat(responses).hasSize(1);
        boolean hasTransactionDetails = responses.get(0).findSegment(HICAZ.class).isPresent();
        assertThat(hasTransactionDetails).isTrue();
    }

    @Test
    public void shouldThrowProperExceptionIfRequestIsUnsuccessful() {
        // given
        initWireMock(TestFixtures.getBodyOfUnsuccessfulResponse());
        TransactionClient client = new TransactionClient(createRequestProcessor(), dialogContext);
        FinTsAccountInformation accountInformation = TestFixtures.getAccountInformation();

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                client.getTransactionResponses(
                                        dummyRequestBuilder, accountInformation));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage("Fetching transaction failed");
    }

    private void initWireMock(String responseBody) {
        WireMock.stubFor(
                WireMock.post(urlEqualTo("/foo/bar"))
                        .willReturn(WireMock.aResponse().withBody(responseBody)));
    }

    private FinTsRequestProcessor createRequestProcessor() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        FinTsRequestSender sender = new FinTsRequestSender(httpClient, configuration.getEndpoint());
        return new FinTsRequestProcessor(dialogContext, sender, getTanAnswerProvider());
    }

    private TanAnswerProvider getTanAnswerProvider() {
        TanAnswerProvider answerProvider = mock(TanAnswerProvider.class);
        when(answerProvider.getTanAnswer("dummyTanMedium")).thenReturn("answer");
        return answerProvider;
    }
}
