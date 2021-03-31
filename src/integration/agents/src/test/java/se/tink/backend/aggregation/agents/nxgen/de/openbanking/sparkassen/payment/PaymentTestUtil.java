package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.payment;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.PASSWORD;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_DATA_PATH;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.TEST_OTP;
import static se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.AuthenticatorTestData.USERNAME;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.entities.ScaMethodEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.AuthenticationMethodResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.authenticator.rpc.FinalizeAuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.payments.common.model.PaymentScheme;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class PaymentTestUtil {

    private SupplementalInformationHelper supplementalInformationHelper;
    private SparkassenApiClient apiClient;

    public PaymentTestUtil(
            SupplementalInformationHelper supplementalInformationHelper,
            SparkassenApiClient apiClient) {
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
    }

    public static final URL TEST_PAYMENT_AUTH_URL =
            new URL(
                    "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/70150000/v1/payments/sepa-credit-transfers"
                            + "/433018b6-0929-454b-8d35-a276bbfd7b1e/authorisations");
    public static final String TEST_PAYMENT_SCA_OAUTH_URL =
            "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/70150000/v1/payments/sepa-credit-transfers"
                    + "/433018b6-0929-454b-8d35-a276bbfd7b1e/authorisations/**HASHED:B3**";

    static final String AUTHENTICATION_METHOD_ID = "Classic - nummer1";

    public static final CreatePaymentResponse PAYMENT_CREATE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_create_response.json").toFile(),
                    CreatePaymentResponse.class);

    public static final AuthenticationMethodResponse PAYMENT_AUTHORIZATION_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_init_authorizations_response.json").toFile(),
                    AuthenticationMethodResponse.class);

    public static final AuthenticationMethodResponse PAYMENT_SCA_METHOD_SELECTION_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_method_selection_response.json")
                            .toFile(),
                    AuthenticationMethodResponse.class);

    public static final FinalizeAuthorizationResponse PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_authentication_status_response.json")
                            .toFile(),
                    FinalizeAuthorizationResponse.class);

    public static final FinalizeAuthorizationResponse
            PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE =
                    SerializationUtils.deserializeFromString(
                            Paths.get(
                                            TEST_DATA_PATH,
                                            "payment_sca_authentication_failed_response.json")
                                    .toFile(),
                            FinalizeAuthorizationResponse.class);

    public static final FetchPaymentStatusResponse PAYMENT_STATUS_SIGNED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_fetch_status_signed_response.json").toFile(),
                    FetchPaymentStatusResponse.class);

    public static final FetchPaymentStatusResponse PAYMENT_STATUS_REJECTED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_fetch_status_rejected_response.json")
                            .toFile(),
                    FetchPaymentStatusResponse.class);

    public static final FetchPaymentStatusResponse PAYMENT_STATUS_CANCELED_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_fetch_status_cancelled_response.json")
                            .toFile(),
                    FetchPaymentStatusResponse.class);

    // when helpers
    public void whenCreatePaymentReturn(PaymentRequest paymentRequest) {
        when(apiClient.createPayment(any(CreatePaymentRequest.class), eq(paymentRequest)))
                .thenReturn(PAYMENT_CREATE_RESPONSE);
    }

    public void whenFetchPaymentStatusReturn(
            PaymentRequest paymentRequest, FetchPaymentStatusResponse fetchPaymentStatusResponse) {
        when(apiClient.fetchPaymentStatus(any(PaymentRequest.class)))
                .thenReturn(fetchPaymentStatusResponse);
    }

    public void whenCreatePaymentAuthorizationReturn(
            AuthenticationMethodResponse authenticationMethodResponse) {
        when(apiClient.initializeAuthorization(TEST_PAYMENT_AUTH_URL, USERNAME, PASSWORD))
                .thenReturn(authenticationMethodResponse);
    }

    public void whenSelectPaymentAuthorizationMethodReturn(
            AuthenticationMethodResponse authenticationMethodResponse) {
        when(apiClient.selectPaymentAuthorizationMethod(
                        TEST_PAYMENT_SCA_OAUTH_URL, AUTHENTICATION_METHOD_ID))
                .thenReturn(authenticationMethodResponse);
    }

    public void whenCreatePaymentFinalizeAuthorizationReturn(
            FinalizeAuthorizationResponse finalizeAuthorizationResponse) {
        when(apiClient.finalizePaymentAuthorization(TEST_PAYMENT_SCA_OAUTH_URL, TEST_OTP))
                .thenReturn(finalizeAuthorizationResponse);
    }

    public void verifyInitializePaymentAuthorizationCalled() {
        verify(apiClient).initializeAuthorization(TEST_PAYMENT_AUTH_URL, USERNAME, PASSWORD);
    }

    public void verifySelectPaymentAuthorizationMethodCalled() {
        verify(apiClient)
                .selectPaymentAuthorizationMethod(
                        TEST_PAYMENT_SCA_OAUTH_URL, AUTHENTICATION_METHOD_ID);
    }

    public void verifyFinalizePaymentAuthorizationCalled() {
        verify(apiClient).finalizePaymentAuthorization(TEST_PAYMENT_SCA_OAUTH_URL, TEST_OTP);
    }

    public void verifyCreatePaymentCalled() {
        verify(apiClient).createPayment(any(CreatePaymentRequest.class), any(PaymentRequest.class));
    }

    public void verifyFetchPaymentStatusCalled() {
        verify(apiClient).fetchPaymentStatus(any(PaymentRequest.class));
    }

    public void verifyAskSupplementalInformationCalled(int times) {
        verify(supplementalInformationHelper, times(times)).askSupplementalInformation(any());
    }

    public void whenSupplementalInformationHelperReturn(
            AuthenticationMethodResponse authenticationMethodResponse) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(
                getFieldName(authenticationMethodResponse.getChosenScaMethod()), TEST_OTP);
        supplementalInformation.put("selectAuthMethodField", "1");

        when(supplementalInformationHelper.askSupplementalInformation(any()))
                .thenReturn(supplementalInformation);
    }

    public String getFieldName(ScaMethodEntity scaMethodEntity) {
        if (scaMethodEntity != null) {
            String authenticationType = scaMethodEntity.getAuthenticationType();
            if ("CHIP_OTP".equalsIgnoreCase(authenticationType)) {
                return "chipTan";
            } else if ("SMS_OTP".equalsIgnoreCase(authenticationType)) {
                return "smsTan";
            } else if ("PUSH_OTP".equalsIgnoreCase(authenticationType)) {
                return "pushTan";
            }
        }
        return "tanField";
    }

    public PaymentRequest createPaymentRequest() {
        return new PaymentRequest(createSepaPayment().build(), "127.0.0.1");
    }

    public PaymentResponse createPaymentResponse() {
        return new PaymentResponse(createSepaPayment().build());
    }

    private Payment.Builder createSepaPayment() {
        return createPayment().withPaymentScheme(PaymentScheme.SEPA_CREDIT_TRANSFER);
    }

    private Payment.Builder createPayment() {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue("SepaReferenceToCreditor ");
        remittanceInformation.setType(RemittanceInformationType.UNSTRUCTURED);

        AccountIdentifier creditorAccountIdentifier = new IbanIdentifier("DE11111111111111111111");
        Creditor creditor = new Creditor(creditorAccountIdentifier, "Creditor Name");

        AccountIdentifier debtorAccountIdentifier = new IbanIdentifier("DE22222222222222222222");
        Debtor debtor = new Debtor(debtorAccountIdentifier);

        ExactCurrencyAmount amount = ExactCurrencyAmount.inEUR(1);
        String currency = "EUR";

        return new Payment.Builder()
                .withCreditor(creditor)
                .withDebtor(debtor)
                .withExactCurrencyAmount(amount)
                .withCurrency(currency)
                .withRemittanceInformation(remittanceInformation);
    }
}
