package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.agents.exceptions.payment.ReferenceValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.authenticator.SwedbankPaymentAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.common.SwedbankOpenBankingPaymentApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentStatus;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.enums.SwedbankPaymentType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.CreatePaymentRequest.CreatePaymentRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentAuthorisationResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.rpc.PaymentStatusResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util.SwedbankDateUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.executor.payment.util.SwedbankRemittanceInformationUtil;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.util.AccountTypePair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.controllers.signing.Signer;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.signing.multifactor.bankid.BankIdSigningController;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Payment;

public class SwedbankPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final SwedbankOpenBankingPaymentApiClient apiClient;
    private final SwedbankPaymentAuthenticator paymentAuthenticator;
    private final List<PaymentResponse> createdPaymentsList = new ArrayList<>();
    private final StrongAuthenticationState strongAuthenticationState;
    private final SupplementalRequester supplementalRequester;
    private final SwedbankBankIdSigner bankIdSigner;

    public SwedbankPaymentExecutor(
            SwedbankOpenBankingPaymentApiClient apiClient,
            SwedbankPaymentAuthenticator paymentAuthenticator,
            StrongAuthenticationState strongAuthenticationState,
            SupplementalRequester supplementalRequester,
            SwedbankBankIdSigner swedbankBankIdSigner) {
        this.apiClient = apiClient;
        this.paymentAuthenticator = paymentAuthenticator;
        this.strongAuthenticationState = strongAuthenticationState;
        this.supplementalRequester = supplementalRequester;
        this.bankIdSigner = swedbankBankIdSigner;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest)
            throws ReferenceValidationException {
        final Payment payment = paymentRequest.getPayment();
        AccountEntity creditor = AccountEntity.creditorOf(paymentRequest);
        AccountEntity debtor = AccountEntity.debtorOf(paymentRequest);
        AmountEntity amount = AmountEntity.amountOf(paymentRequest);
        SwedbankRemittanceInformationUtil remittanceInformationUtil =
                SwedbankRemittanceInformationUtil.of(paymentRequest);

        CreatePaymentRequestBuilder builder =
                CreatePaymentRequest.builder()
                        .creditorAccount(creditor)
                        .debtorAccount(debtor)
                        .instructedAmount(amount)
                        .executionDate(SwedbankDateUtil.getExecutionDateOrCurrentDate(payment))
                        .remittanceInformationStructured(
                                remittanceInformationUtil.getRemittanceInformationStructured())
                        .remittanceInformationUnstructured(
                                remittanceInformationUtil.getRemittanceInformationUnStructured())
                        .debtorAccountStatementText(payment.getCreditor().getName());

        if (Type.SE.equals(paymentRequest.getPayment().getCreditor().getAccountIdentifierType())) {
            builder.creditorFriendlyName(payment.getCreditor().getName());
        }
        CreatePaymentRequest createPaymentRequest = builder.build();

        AccountTypePair accountTypePair =
                new AccountTypePair(paymentRequest.getPayment().getCreditorAndDebtorAccountType());

        SwedbankPaymentType paymentType = SwedbankPaymentType.getPaymentType(accountTypePair);

        PaymentResponse paymentResponse =
                apiClient
                        .createPayment(
                                createPaymentRequest,
                                SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                        .toTinkPaymentResponse(
                                creditor, debtor, amount, paymentType, accountTypePair);

        createdPaymentsList.add(paymentResponse);

        return paymentResponse;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .getPayment(
                        paymentRequest.getPayment().getUniqueId(),
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                .toTinkPaymentResponse(
                        paymentRequest.getPayment(),
                        apiClient
                                .getPaymentStatus(
                                        paymentRequest.getPayment().getUniqueId(),
                                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS)
                                .getTransactionStatus());
    }

    private PaymentMultiStepResponse signWithRedirectFlow(
            PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();

        String state = strongAuthenticationState.getState();

        PaymentAuthorisationResponse paymentAuthorisationResponse =
                apiClient.startPaymentAuthorisation(
                        payment.getUniqueId(),
                        SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                        state,
                        true);
        paymentAuthenticator.openThirdPartyApp(
                paymentAuthorisationResponse.getScaRedirectUrl(), state);

        payment.setStatus(getPaymentStatus(payment.getUniqueId()));

        return new PaymentMultiStepResponse(
                payment, AuthenticationStepConstants.STEP_FINALIZE, new ArrayList<>());
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {

        Payment payment = paymentMultiStepRequest.getPayment();
        String nextStep;
        nextStep = SigningStepConstants.STEP_FINALIZE;
        String state = strongAuthenticationState.getState();
        final String paymentId = payment.getUniqueId();

        switch (paymentMultiStepRequest.getStep()) {
            case SigningStepConstants.STEP_INIT:
                PaymentStatusResponse paymentStatusResponse =
                        apiClient.getPaymentStatus(
                                paymentId, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS);

                if (paymentStatusResponse.isReadyForSigning()) {
                    PaymentAuthorisationResponse paymentAuthorisationResponse =
                            apiClient.startPaymentAuthorisation(
                                    paymentId,
                                    SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS,
                                    state,
                                    false);
                    bankIdSigner.setAuthenticationResponse(
                            apiClient.startPaymentAuthorization(
                                    paymentAuthorisationResponse.getSelectAuthenticationMethod()));
                    nextStep = SigningStepConstants.STEP_SIGN;
                    break;
                } else {
                    return new PaymentMultiStepResponse(
                            payment, SigningStepConstants.STEP_INIT, new ArrayList<>());
                }
            case SigningStepConstants.STEP_SIGN:
                getSigner().sign(paymentMultiStepRequest);
                if (bankIdSigner.isMissingExtendedBankId()) {
                    signWithRedirectFlow(paymentMultiStepRequest);
                } else if (getPaymentStatus(paymentId).equals(PaymentStatus.PENDING)) {
                    nextStep = SigningStepConstants.STEP_INIT;
                } else {
                    nextStep = SigningStepConstants.STEP_FINALIZE;
                }
                break;
            case SigningStepConstants.STEP_FINALIZE:
                break;
            default:
                throw new IllegalStateException(
                        String.format("Unknown step %s", paymentMultiStepRequest.getStep()));
        }
        payment.setStatus(getPaymentStatus(paymentId));
        return new PaymentMultiStepResponse(payment, nextStep, new ArrayList<>());
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        throw new NotImplementedException(
                "createBeneficiary not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        throw new NotImplementedException(
                "cancel not yet implemented for " + this.getClass().getName());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(createdPaymentsList);
    }

    private PaymentStatus getPaymentStatus(String paymentId) {
        PaymentStatusResponse paymentStatusResponse =
                apiClient.getPaymentStatus(
                        paymentId, SwedbankPaymentType.SE_DOMESTIC_CREDIT_TRANSFERS);

        return SwedbankPaymentStatus.fromString(paymentStatusResponse.getTransactionStatus())
                .getTinkPaymentStatus();
    }

    private Signer getSigner() {
        return new BankIdSigningController(supplementalRequester, bankIdSigner);
    }
}
