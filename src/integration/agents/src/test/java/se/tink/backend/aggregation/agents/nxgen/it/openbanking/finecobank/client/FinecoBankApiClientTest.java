package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.enums.FinecoBankPaymentProduct;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentAuthsResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.payment.rpc.GetPaymentStatusResponse;
import se.tink.backend.aggregation.logmasker.LogMaskerImpl;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.NextGenTinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.libraries.amount.ExactCurrencyAmount;

@RunWith(JUnitParamsRunner.class)
public class FinecoBankApiClientTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/it/openbanking/finecobank/resources";
    private static final String FILE_PAYMENT_INIT_OK = "paymentInitOkResponse.json";
    private static final String FILE_PAYMENT_INIT_REQ = "paymentInitRequest.json";
    private static final String FILE_GET_PAYMENT_RESP = "getPaymentResponse.json";
    private static final String FILE_GET_PAYMENT_STATUS_RESP = "getPaymentStatusResponse.json";
    private static final String FILE_GET_PAYMENT_AUTHS_RESP = "getAuthsResponse.json";
    private static final String FILE_GET_PAYMENT_AUTH_STATUS_RESP = "getAuthStatusResponse.json";

    private static final String TEST_REDIRECT_URL = "test_redirect_url";
    private static final String TEST_USER_IP = "192.168.56.78";

    private static final String TEST_REQ_ID = "00000000-0000-4000-0000-000000000000";
    private static final String TEST_STATE = "awesome_test_state";
    private static final String TEST_PAYMENT_ID = "test_payment_id";
    private static final String TEST_AUTH_ID = "test_auth_id";

    @Rule public WireMockRule wireMock = new WireMockRule(wireMockConfig().dynamicPort());

    private FinecoHeaderValues mockHeaderValues;
    private FinecoBankApiClient apiClient;

    @Before
    public void setup() {
        TinkHttpClient httpClient =
                NextGenTinkHttpClient.builder(
                                LogMaskerImpl.builder().build(),
                                LogMaskerImpl.LoggingMode.UNSURE_IF_MASKER_COVERS_SECRETS)
                        .build();
        mockHeaderValues = mock(FinecoHeaderValues.class);
        when(mockHeaderValues.getRedirectUrl()).thenReturn(TEST_REDIRECT_URL);
        apiClient =
                new FinecoBankApiClient(
                        new FinecoUrlProvider(wireMock.baseUrl()),
                        httpClient,
                        mockHeaderValues,
                        new MockRandomValueGenerator());
    }

    @Test
    @Parameters(method = "parametersForPaymentApiCall")
    public void shouldCreateProperPaymentInitRequest(
            boolean isManual, FinecoBankPaymentProduct finecoBankPaymentProduct) {
        // given
        when(mockHeaderValues.getUserIp()).thenReturn(isManual ? TEST_USER_IP : null);
        String url = "/payments/" + finecoBankPaymentProduct.getValue();
        WireMock.stubFor(
                WireMock.post(urlEqualTo(url)).willReturn(fileAsResponse(FILE_PAYMENT_INIT_OK)));
        // when
        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        testCreatePaymentRequest(), finecoBankPaymentProduct, TEST_STATE);

        // then
        RequestPatternBuilder requestPatternBuilder =
                commonRequestPattern(RequestMethod.POST, isManual, url);
        requestPatternBuilder.withHeader(
                "TPP-Redirect-URI", equalTo(TEST_REDIRECT_URL + "?state=" + TEST_STATE));
        verify(1, requestPatternBuilder);

        assertThat(createPaymentResponse.getPaymentId())
                .isEqualTo("1827d19a-e0d5-4205-ac63-3919a6200505");
        assertThat(createPaymentResponse.getScaRedirectLink())
                .isEqualTo("https://scaRedirectlink.test.com");
        assertThat(createPaymentResponse.getTransactionStatus()).isEqualTo("RCVD");
    }

    @Test
    @Parameters(method = "parametersForPaymentApiCall")
    public void shouldGetPaymentSuccessfully(
            boolean isManual, FinecoBankPaymentProduct finecoBankPaymentProduct) {
        // given
        when(mockHeaderValues.getUserIp()).thenReturn(isManual ? TEST_USER_IP : null);
        String url = "/payments/" + finecoBankPaymentProduct.getValue() + "/" + TEST_PAYMENT_ID;
        WireMock.stubFor(
                WireMock.get(urlEqualTo(url)).willReturn(fileAsResponse(FILE_GET_PAYMENT_RESP)));
        // when
        GetPaymentResponse getPaymentResponse =
                apiClient.getPayment(finecoBankPaymentProduct, TEST_PAYMENT_ID);

        // then
        verify(1, commonRequestPattern(RequestMethod.GET, isManual, url));

        assertThat(getPaymentResponse.getTransactionStatus()).isEqualTo("ACSC");
        assertThat(getPaymentResponse.getInstructedAmount().toTinkAmount())
                .isEqualTo(ExactCurrencyAmount.of(123.50, "EUR"));
        assertThat(getPaymentResponse.getDebtorAccount().getIban())
                .isEqualTo("IT31X0301503200000003517230");
        assertThat(getPaymentResponse.getCreditorAccount().getIban())
                .isEqualTo("DE02100100109307118603");
        assertThat(getPaymentResponse.getCreditorName()).isEqualTo("Walter Bianchi");
        assertThat(getPaymentResponse.getRemittanceInformationUnstructured())
                .isEqualTo("causale pagamento");
    }

    @Test
    @Parameters(method = "parametersForPaymentApiCall")
    public void shouldGetPaymentStatusSuccessfully(
            boolean isManual, FinecoBankPaymentProduct finecoBankPaymentProduct) {
        // given
        when(mockHeaderValues.getUserIp()).thenReturn(isManual ? TEST_USER_IP : null);
        String url =
                "/payments/"
                        + finecoBankPaymentProduct.getValue()
                        + "/"
                        + TEST_PAYMENT_ID
                        + "/status";
        WireMock.stubFor(
                WireMock.get(urlEqualTo(url))
                        .willReturn(fileAsResponse(FILE_GET_PAYMENT_STATUS_RESP)));
        // when
        GetPaymentStatusResponse paymentStatus =
                apiClient.getPaymentStatus(finecoBankPaymentProduct, TEST_PAYMENT_ID);

        // then
        verify(1, commonRequestPattern(RequestMethod.GET, isManual, url));

        assertThat(paymentStatus.getTransactionStatus()).isEqualTo("RCVD");
    }

    @Test
    @Parameters(method = "parametersForPaymentApiCall")
    public void shouldGetPaymentAuthsSuccessfully(
            boolean isManual, FinecoBankPaymentProduct finecoBankPaymentProduct) {
        // given
        when(mockHeaderValues.getUserIp()).thenReturn(isManual ? TEST_USER_IP : null);
        String url =
                "/payments/"
                        + finecoBankPaymentProduct.getValue()
                        + "/"
                        + TEST_PAYMENT_ID
                        + "/authorisations";
        WireMock.stubFor(
                WireMock.get(urlEqualTo(url))
                        .willReturn(fileAsResponse(FILE_GET_PAYMENT_AUTHS_RESP)));
        // when
        GetPaymentAuthsResponse paymentAuths =
                apiClient.getPaymentAuths(finecoBankPaymentProduct, TEST_PAYMENT_ID);

        // then
        verify(1, commonRequestPattern(RequestMethod.GET, isManual, url));

        assertThat(paymentAuths.getAuthorisationIds()).hasSize(1);
        assertThat(paymentAuths.getAuthorisationIds().get(0))
                .isEqualTo("au-f1464ff1-1f15-49ad-bd9d-80858ed2aba0");
    }

    @Test
    @Parameters(method = "parametersForPaymentApiCall")
    public void shouldGetPaymentAuthStatusSuccessfully(
            boolean isManual, FinecoBankPaymentProduct finecoBankPaymentProduct) {
        // given
        when(mockHeaderValues.getUserIp()).thenReturn(isManual ? TEST_USER_IP : null);
        String url =
                "/payments/"
                        + finecoBankPaymentProduct.getValue()
                        + "/"
                        + TEST_PAYMENT_ID
                        + "/authorisations/"
                        + TEST_AUTH_ID;
        WireMock.stubFor(
                WireMock.get(urlEqualTo(url))
                        .willReturn(fileAsResponse(FILE_GET_PAYMENT_AUTH_STATUS_RESP)));
        // when
        GetPaymentAuthStatusResponse paymentAuthStatus =
                apiClient.getPaymentAuthStatus(
                        finecoBankPaymentProduct, TEST_PAYMENT_ID, TEST_AUTH_ID);

        // then
        verify(1, commonRequestPattern(RequestMethod.GET, isManual, url));
        assertThat(paymentAuthStatus.getScaStatus()).isEqualTo("received");
    }

    private RequestPatternBuilder commonRequestPattern(
            RequestMethod requestMethod, boolean isManual, String url) {
        RequestPatternBuilder requestPatternBuilder =
                new RequestPatternBuilder(requestMethod, urlEqualTo(url))
                        .withHeader("X-Request-ID", equalTo(TEST_REQ_ID))
                        .withHeader("content-type", equalTo("application/json"));
        if (isManual) {
            requestPatternBuilder.withHeader("PSU-IP-Address", equalTo(TEST_USER_IP));
        } else {
            requestPatternBuilder.withoutHeader("PSU-IP-Address");
        }
        return requestPatternBuilder;
    }

    // Builds all combinations of manual/background refresh with any of known products
    private Object[] parametersForPaymentApiCall() {
        List<Object[]> list = new ArrayList<>();
        for (boolean v : new boolean[] {true, false}) {
            for (FinecoBankPaymentProduct product : FinecoBankPaymentProduct.values()) {
                list.add(new Object[] {v, product});
            }
        }
        return list.toArray();
    }

    private ResponseDefinitionBuilder fileAsResponse(String filename) {
        return WireMock.aResponse()
                .withStatus(201)
                .withBody(readJson(filename))
                .withHeader("Content-Type", "application/json");
    }

    @SneakyThrows
    private String readJson(String filename) {
        return String.join("", Files.readAllLines(Paths.get(TEST_DATA_PATH, filename)));
    }

    private CreatePaymentRequest testCreatePaymentRequest() {
        return CreatePaymentRequest.builder()
                .remittanceInformationUnstructured("fineco")
                .instructedAmount(new AmountEntity(ExactCurrencyAmount.inEUR(1.0)))
                .creditorAccount(new AccountEntity("IT95N0300203280155761664887"))
                .creditorName("Creditor Name")
                .build();
    }
}
