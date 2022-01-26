package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.CreditorAddressEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities.RemittanceInformationStructured;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.enums.SparebankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.rpc.AccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.FetchablePaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentListResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class SparebankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private final SparebankApiClient apiClient;
    private final SparebankStorage storage;
    private final SparebankPaymentSigner signer;

    private static final CountryDateHelper dateHelper = new CountryDateHelper(Locale.getDefault());

    public SparebankPaymentExecutor(
            SparebankApiClient apiClient,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState,
            SparebankStorage storage) {
        this.apiClient = apiClient;
        this.storage = storage;
        this.signer =
                new SparebankPaymentSigner(
                        this,
                        apiClient,
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        storage);
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        signer.initiate();
        return getPaymentResponse(paymentRequest);
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {

        String paymentId = paymentRequest.getPayment().getUniqueId();
        SparebankPaymentType paymentType =
                SparebankPaymentType.getSpareBankPaymentType(paymentRequest);

        final String paymentStatus =
                apiClient.fetchPaymentStatus(getPaymentStatusUrl(paymentId)).getTransactionStatus();

        return apiClient
                .fetchPayment(getPaymentResponseUrl(paymentId))
                .toTinkPaymentResponse(paymentRequest.getPayment(), paymentType, paymentStatus);
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(req -> new PaymentResponse(req.getPayment()))
                        .collect(Collectors.toList()));
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException, AuthenticationException {
        signer.sign(paymentMultiStepRequest);

        final Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(PaymentStatus.PAID);
        return new PaymentMultiStepResponse(payment, SigningStepConstants.STEP_FINALIZE);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) throws PaymentException {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    protected PaymentResponse getPaymentResponse(PaymentRequest paymentRequest) {
        final Payment payment = paymentRequest.getPayment();

        if (Objects.isNull(payment.getExecutionDate())) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        final AccountEntity creditor = AccountEntity.creditorOf(payment);
        final AccountEntity debtor = getDebtorAccount();
        final String creditorName = payment.getCreditor().getName();
        final SparebankPaymentType paymentType =
                SparebankPaymentType.getSpareBankPaymentType(payment);
        final RemittanceInformation remittanceInformation = payment.getRemittanceInformation();
        final String requestedExecutionDate =
                payment.getExecutionDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        CreatePaymentRequest createPaymentRequest =
                CreatePaymentRequest.builder()
                        .sparebankPaymentType(paymentType)
                        .debtorAccount(debtor.toBban())
                        .creditorAccount(creditor.toBban())
                        .creditorName(creditorName)
                        .amount(AmountEntity.amountOf(paymentRequest))
                        .requestedExecutionDate(requestedExecutionDate)
                        .creditorAddress(new CreditorAddressEntity())
                        .remittanceInformationUnstructured(remittanceInformation.getValue())
                        .remittanceInformationStructured(
                                isRemittanceInformationStructured(remittanceInformation)
                                        ? new RemittanceInformationStructured(
                                                remittanceInformation.getValue())
                                        : null)
                        .build();

        CreatePaymentResponse paymentResponse =
                apiClient.createPayment(createPaymentRequest, paymentType);

        storage.storePaymentUrls(paymentResponse.getPaymentId(), paymentResponse.getLinks());

        return paymentResponse.toTinkPaymentResponse(
                createPaymentRequest,
                paymentType,
                paymentRequest.getPayment().getPaymentServiceType(),
                paymentRequest.getPayment().getPaymentScheme());
    }

    private boolean isRemittanceInformationStructured(RemittanceInformation remittanceInformation) {
        return remittanceInformation.getType() == RemittanceInformationType.REFERENCE;
    }

    private AccountEntity getDebtorAccount() {
        AccountResponse accountResponse =
                storage.getStoredAccounts()
                        .orElseThrow(() -> new IllegalStateException("Empty accounts to debit"));
        return AccountEntity.debtorOf(accountResponse);
    }

    private String getPaymentStatusUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Empty payment status url."))
                .getStatus()
                .getHref();
    }

    private String getPaymentResponseUrl(String paymentId) {
        return storage.getPaymentUrls(paymentId)
                .orElseThrow(() -> new IllegalStateException("Empty get payment response url."))
                .getSelf()
                .getHref();
    }
}
