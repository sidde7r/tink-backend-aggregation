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
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.SparkassenApiClient;
import se.tink.backend.aggregation.agents.utils.authentication.AuthenticationType;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.AuthorizationStatusResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ScaMethodEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.FetchPaymentStatusResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
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

@Ignore
public class PaymentTestHelper {

    private SupplementalInformationController supplementalInformationController;
    private SparkassenApiClient apiClient;

    public PaymentTestHelper(
            SupplementalInformationController supplementalInformationController,
            SparkassenApiClient apiClient) {
        this.supplementalInformationController = supplementalInformationController;
        this.apiClient = apiClient;
    }

    public static final String TEST_PAYMENT_AUTH_URL =
            "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/70150000/v1/payments/sepa-credit-transfers"
                    + "/433018b6-0929-454b-8d35-a276bbfd7b1e/authorisations";
    public static final String TEST_PAYMENT_SCA_OAUTH_URL =
            "https://xs2a.f-i-apim.de:8443/fixs2aop-env/xs2a-api/70150000/v1/payments/sepa-credit-transfers"
                    + "/433018b6-0929-454b-8d35-a276bbfd7b1e/authorisations/**HASHED:B3**";

    static final String AUTHENTICATION_METHOD_ID = "Classic - nummer1";
    static final String AUTHENTICATION_METHOD_ID_2 = "Classic - nummer2";

    public static final CreatePaymentResponse PAYMENT_CREATE_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_create_response.json").toFile(),
                    CreatePaymentResponse.class);

    public static final LinksEntity SCA_LINKS =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "sca_links.json").toFile(), LinksEntity.class);

    public static final AuthorizationResponse
            PAYMENT_AUTHORIZATION_RESPONSE_WITH_MULTIPLE_SCA_METHOD =
                    SerializationUtils.deserializeFromString(
                            Paths.get(
                                            TEST_DATA_PATH,
                                            "payment_init_authorizations_with_multiple_sca_method_response.json")
                                    .toFile(),
                            AuthorizationResponse.class);

    public static final AuthorizationResponse
            PAYMENT_AUTHORIZATION_RESPONSE_WITH_SINGLE_SCA_METHOD =
                    SerializationUtils.deserializeFromString(
                            Paths.get(
                                            TEST_DATA_PATH,
                                            "payment_init_authorizations_with_single_sca_method_response.json")
                                    .toFile(),
                            AuthorizationResponse.class);

    public static final AuthorizationResponse PAYMENT_SCA_METHOD_SELECTION_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_method_selection_response.json")
                            .toFile(),
                    AuthorizationResponse.class);

    public static final AuthorizationResponse PAYMENT_SCA_EXEMPTION_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_exemption_response.json").toFile(),
                    AuthorizationResponse.class);

    public static final AuthorizationStatusResponse PAYMENT_SCA_AUTHENTICATION_STATUS_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_authentication_status_response.json")
                            .toFile(),
                    AuthorizationStatusResponse.class);

    public static final AuthorizationStatusResponse
            PAYMENT_SCA_AUTHENTICATION_FAILED_STATUS_RESPONSE =
                    SerializationUtils.deserializeFromString(
                            Paths.get(
                                            TEST_DATA_PATH,
                                            "payment_sca_authentication_failed_response.json")
                                    .toFile(),
                            AuthorizationStatusResponse.class);

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

    public static final AuthorizationResponse PAYMENT_SCA_METHOD_CHIP_TAN_SELECTION_RESPONSE =
            SerializationUtils.deserializeFromString(
                    Paths.get(TEST_DATA_PATH, "payment_sca_method_selection_chip_tan_response.json")
                            .toFile(),
                    AuthorizationResponse.class);

    // when helpers
    public void whenCreatePaymentReturn(PaymentRequest paymentRequest) {
        when(apiClient.createPayment(eq(paymentRequest))).thenReturn(PAYMENT_CREATE_RESPONSE);
    }

    public void whenFetchPaymentStatusReturn(
            FetchPaymentStatusResponse fetchPaymentStatusResponse) {
        when(apiClient.fetchPaymentStatus(any(PaymentRequest.class)))
                .thenReturn(fetchPaymentStatusResponse);
    }

    public void whenCreatePaymentAuthorizationReturn(AuthorizationResponse authorizationResponse) {
        when(apiClient.initializeAuthorization(TEST_PAYMENT_AUTH_URL, USERNAME, PASSWORD))
                .thenReturn(authorizationResponse);
    }

    public void whenSelectPaymentAuthorizationMethodReturn(
            AuthorizationResponse authorizationResponse) {
        when(apiClient.selectAuthorizationMethod(
                        TEST_PAYMENT_SCA_OAUTH_URL, AUTHENTICATION_METHOD_ID))
                .thenReturn(authorizationResponse);
    }

    public void whenSelect2ndOptionPaymentAuthorizationMethodReturn(
            AuthorizationResponse authorizationResponse) {
        when(apiClient.selectAuthorizationMethod(
                        TEST_PAYMENT_SCA_OAUTH_URL, AUTHENTICATION_METHOD_ID_2))
                .thenReturn(authorizationResponse);
    }

    public void whenCreatePaymentFinalizeAuthorizationReturn(
            AuthorizationStatusResponse finalizeAuthorizationResponse) {
        when(apiClient.finalizeAuthorization(TEST_PAYMENT_SCA_OAUTH_URL, TEST_OTP))
                .thenReturn(finalizeAuthorizationResponse);
    }

    public Storage prepareStorageWithScaLinks(SessionStorage sessionStorage) {
        sessionStorage.put(StorageValues.SCA_LINKS, SCA_LINKS);
        return sessionStorage;
    }

    public void verifyInitializePaymentAuthorizationCalled() {
        verify(apiClient).initializeAuthorization(TEST_PAYMENT_AUTH_URL, USERNAME, PASSWORD);
    }

    public void verifySelectPaymentAuthorizationMethodCalled() {
        verify(apiClient)
                .selectAuthorizationMethod(TEST_PAYMENT_SCA_OAUTH_URL, AUTHENTICATION_METHOD_ID);
    }

    public void verifyFinalizePaymentAuthorizationCalled() {
        verify(apiClient).finalizeAuthorization(TEST_PAYMENT_SCA_OAUTH_URL, TEST_OTP);
    }

    public void verifyCreatePaymentCalled() {
        verify(apiClient).createPayment(any(PaymentRequest.class));
    }

    public void verifyFetchPaymentStatusCalled() {
        verify(apiClient).fetchPaymentStatus(any(PaymentRequest.class));
    }

    public void verifyAskSupplementalInformationCalled(int times) {
        verify(supplementalInformationController, times(times))
                .askSupplementalInformationSync(any());
    }

    public void whenSupplementalInformationControllerReturn(
            AuthorizationResponse authorizationResponse, int selectionNumber) {
        Map<String, String> supplementalInformation = new HashMap<>();
        supplementalInformation.put(
                getFieldName(authorizationResponse.getChosenScaMethod()), TEST_OTP);
        supplementalInformation.put("selectAuthMethodField", String.valueOf(selectionNumber));

        when(supplementalInformationController.askSupplementalInformationSync(any()))
                .thenReturn(supplementalInformation);
    }

    public String getFieldName(ScaMethodEntity scaMethodEntity) {
        if (scaMethodEntity != null) {
            AuthenticationType authenticationType =
                    AuthenticationType.fromString(scaMethodEntity.getAuthenticationType()).get();
            switch (authenticationType) {
                case CHIP_OTP:
                    return "chipTan";
                case SMS_OTP:
                    return "smsTan";
                case PUSH_OTP:
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

    public PaymentResponse createPaymentResponseWithScaLinks() {
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
