package se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.sun.jersey.core.util.MultivaluedMapImpl;
import javax.ws.rs.core.MultivaluedMap;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.CommerzBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.commerzbank.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersConstants.StorageKeys;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.PaymentServiceType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class CommerzBankPaymentExecutorTest {

    private static final String TEST_PAYMENT_ID = "test_payment_id";

    private CommerzBankApiClient mockApiClient;
    private CommerzBankPaymentAuthenticator mockPaymentAuthenticator;

    private SessionStorage sessionStorage;
    private Credentials credentials;

    private CommerzBankPaymentExecutor paymentExecutor;

    @Before
    public void setup() {
        mockApiClient = mock(CommerzBankApiClient.class);
        mockPaymentAuthenticator = mock(CommerzBankPaymentAuthenticator.class);

        sessionStorage = new SessionStorage();
        credentials = new Credentials();
        paymentExecutor =
                new CommerzBankPaymentExecutor(
                        mockApiClient, mockPaymentAuthenticator, sessionStorage, credentials);
    }

    @Test
    public void shouldCreatePaymentSuccesfullyAndSaveProperDataInDecoupledPath() {
        // given
        PaymentRequest paymentRequest = createDummyRequest();

        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        headersMap.putSingle("ASPSP-SCA-Approach", "DECOUPLED");
        when(mockHttpResponse.getHeaders()).thenReturn(headersMap);
        when(mockHttpResponse.getBody(CreatePaymentResponse.class))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.PAYMENT_CREATED_OK_DECOUPLED,
                                CreatePaymentResponse.class));

        when(mockApiClient.createPayment(any(), any())).thenReturn(mockHttpResponse);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        assertThat(paymentRequest.getPayment().getUniqueId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(sessionStorage.get(StorageKeys.SCA_APPROACH)).isEqualTo("DECOUPLED");
        assertThat(sessionStorage.get(StorageKeys.SCA_STATUS_LINK))
                .isEqualTo(
                        "https://xs2a-api.comdirect.de/berlingroup/v1/payments/sepa-credit-transfers/test_payment_id/authorisations/auth_id");
        assertThat(sessionStorage.get(StorageKeys.SCA_OAUTH_LINK)).isNull();
    }

    @Test
    public void shouldCreatePaymentSuccesfullyAndSaveProperDataInRedirectPath() {
        // given
        PaymentRequest paymentRequest = createDummyRequest();

        HttpResponse mockHttpResponse = mock(HttpResponse.class);
        MultivaluedMap<String, String> headersMap = new MultivaluedMapImpl();
        headersMap.putSingle("ASPSP-SCA-Approach", "REDIRECT");
        when(mockHttpResponse.getHeaders()).thenReturn(headersMap);
        when(mockHttpResponse.getBody(CreatePaymentResponse.class))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.PAYMENT_CREATED_OK_REDIRECT,
                                CreatePaymentResponse.class));

        when(mockApiClient.createPayment(any(), any())).thenReturn(mockHttpResponse);

        // when
        paymentExecutor.create(paymentRequest);

        // then
        assertThat(paymentRequest.getPayment().getUniqueId()).isEqualTo(TEST_PAYMENT_ID);
        assertThat(sessionStorage.get(StorageKeys.SCA_APPROACH)).isEqualTo("REDIRECT");
        assertThat(sessionStorage.get(StorageKeys.SCA_STATUS_LINK))
                .isEqualTo(
                        "https://xs2a-api.comdirect.de/berlingroup/v1/payments/sepa-credit-transfers/test_payment_id/authorisations/auth_id");
        assertThat(sessionStorage.get(StorageKeys.SCA_OAUTH_LINK))
                .isEqualTo(
                        "https://xs2a-api.comdirect.de/berlingroup/.well-known/openid-configuration?authorizationId=auth_id");
    }

    private PaymentRequest createDummyRequest() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("remittanceInfo");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        return new PaymentRequest(
                new Payment.Builder()
                        .withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER)
                        .withPaymentServiceType(PaymentServiceType.SINGLE)
                        .withExactCurrencyAmount(ExactCurrencyAmount.inEUR(1))
                        .withCreditor(new Creditor(new IbanIdentifier("DE13500105175163227317")))
                        .withDebtor(new Debtor(new IbanIdentifier("DE13500105175163227317")))
                        .withRemittanceInformation(remittanceInformation)
                        .build());
    }
}
