package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account;

import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestProcessor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsRequestSender;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.exception.UnsuccessfulApiCallException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.configuration.FinTsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice.TanAnswerProvider;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class AccountClientTest {

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private FinTsConfiguration configuration;
    private FinTsDialogContext dialogContext;

    @Before
    public void setup() {
        configuration = TestFixtures.getFinTsConfiguration(wireMock.port());
        dialogContext = TestFixtures.getDialogContext(configuration);
    }

    @Test
    public void shouldGetAccountsDetails() {
        // given
        initWireMock(TestFixtures.getBodyOfSuccessfulAccountsDetailsResponse());
        FinTsRequestProcessor requestProcessor = createRequestProcessor();
        AccountClient client = new AccountClient(requestProcessor, dialogContext);

        // when
        FinTsResponse response = client.getSepaDetailsForAllAccounts();

        // then
        assertThat(response.isSuccess()).isTrue();
        boolean hasAccountsDetails = response.findSegment(HISPA.class).isPresent();
        assertThat(hasAccountsDetails).isTrue();
    }

    @Test
    public void getAccountDetailsShouldThrowProperExceptionIfRequestIsUnsuccessful() {
        // given
        initWireMock(TestFixtures.getBodyOfUnsuccessfulResponse());
        FinTsRequestProcessor requestProcessor = createRequestProcessor();
        AccountClient client = new AccountClient(requestProcessor, dialogContext);

        // when
        Throwable thrown = catchThrowable(client::getSepaDetailsForAllAccounts);

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage("Couldn't fetch SEPA details for user's accounts");
    }

    @Test
    public void shouldGetBalanceForAccount() {
        // given
        initWireMock(TestFixtures.getBodyOfSuccessfulAccountBalanceResponse());
        FinTsRequestProcessor requestProcessor = createRequestProcessor();
        AccountClient client = new AccountClient(requestProcessor, dialogContext);
        HIUPD hiupd = new HIUPD().setAccountNumber("123456789").setAccountType(1);

        // when
        FinTsResponse response =
                client.getBalanceForAccount(
                        BalanceReqeustBuilderProvider.getRequestBuilder(dialogContext), hiupd);

        // then
        assertThat(response.isSuccess()).isTrue();
        boolean hasBalanceDetails = response.findSegment(HISAL.class).isPresent();
        assertThat(hasBalanceDetails).isTrue();
    }

    @Test
    public void getAccountBalanceShouldThrowProperExceptionIfRequestIsUnsuccessful() {
        // given
        initWireMock(TestFixtures.getBodyOfUnsuccessfulResponse());
        FinTsRequestProcessor requestProcessor = createRequestProcessor();
        AccountClient client = new AccountClient(requestProcessor, dialogContext);
        HIUPD hiupd = new HIUPD().setAccountNumber("123456789").setAccountType(1);

        // when
        Throwable thrown =
                catchThrowable(
                        () ->
                                client.getBalanceForAccount(
                                        BalanceReqeustBuilderProvider.getRequestBuilder(
                                                dialogContext),
                                        hiupd));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(UnsuccessfulApiCallException.class)
                .hasMessage("Couldn't fetch balance for AccountNumber: 123456789 , AccountType: 1");
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
