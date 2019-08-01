package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.DateFormat;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PaymentIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.ServiceLevelCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity.SupplementaryDataEntity.AcceptedAuthenticationApproachEnum;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;

@SuppressWarnings("Duplicates")
public class CreditAgricolePaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private CreditAgricoleApiClient apiClient;
    private SessionStorage sessionStorage;
    private CreditAgricoleConfiguration configuration;
    private List<PaymentResponse> paymentResponses;

    public CreditAgricolePaymentExecutor(
            CreditAgricoleApiClient apiClient,
            SessionStorage sessionStorage,
            CreditAgricoleConfiguration configuration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = configuration;
        paymentResponses = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        Payment payment = paymentRequest.getPayment();

        String id = UUID.randomUUID().toString().replace("-", "");

        PartyIdentificationEntity initiatingParty =
                PartyIdentificationEntity.builder()
                        .name(payment.getDebtor().getAccountNumber())
                        .build();

        PaymentTypeInformationEntity paymentTypeInformation =
                PaymentTypeInformationEntity.builder()
                        .serviceLevel(ServiceLevelCodeEntity.SEPA)
                        .build();

        AccountIdentificationEntity debtorAccount =
                new AccountIdentificationEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(), null);

        PartyIdentificationEntity creditor =
                PartyIdentificationEntity.builder().name(FormValues.BENEFICIARY_NAME).build();

        AccountIdentificationEntity creditorAccount =
                new AccountIdentificationEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(), null);

        BeneficiaryEntity beneficiary =
                BeneficiaryEntity.builder()
                        .creditor(creditor)
                        .creditorAccount(creditorAccount)
                        .build();

        PaymentIdentificationEntity paymentId =
                PaymentIdentificationEntity.builder()
                        .instructionId(FormValues.INSTRUCTION_ID)
                        .build();

        AmountTypeEntity instructedAmount =
                AmountTypeEntity.builder()
                        .amount(payment.getAmount().getValue().toString())
                        .currency(payment.getAmount().getCurrency())
                        .build();

        List<CreditTransferTransactionEntity> creditTransferTransaction =
                Collections.singletonList(
                        CreditTransferTransactionEntity.builder()
                                .paymentId(paymentId)
                                .requestedExecutionDate(
                                        OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                                .instructedAmount(instructedAmount)
                                .build());

        String callbackUrl =
                configuration.getRedirectUrl()
                        + Urls.SUCCESS_REPORT_PATH
                        + sessionStorage.get(StorageKeys.STATE);

        List<AcceptedAuthenticationApproachEnum> acceptedAuthenticationApproach =
                Collections.singletonList(AcceptedAuthenticationApproachEnum.REDIRECT);
        SupplementaryDataEntity supplementaryData =
                SupplementaryDataEntity.builder()
                        .acceptedAuthenticationApproach(acceptedAuthenticationApproach)
                        .successfulReportUrl(callbackUrl)
                        .unsuccessfulReportUrl(callbackUrl)
                        .build();

        PaymentRequestResourceEntity paymentRequestResourceEntity =
                PaymentRequestResourceEntity.builder()
                        .resourceId(id)
                        .paymentInformationId(id)
                        .creationDateTime(OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                        .numberOfTransactions(1)
                        .requestedExecutionDate(
                                OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                        .initiatingParty(initiatingParty)
                        .paymentTypeInformation(paymentTypeInformation)
                        .debtorAccount(debtorAccount)
                        .beneficiary(beneficiary)
                        .creditTransferTransaction(creditTransferTransaction)
                        .supplementaryData(supplementaryData)
                        .build();

        HalPaymentRequestCreation paymentRequestCreation =
                apiClient.makePayment(paymentRequestResourceEntity);

        sessionStorage.put(id, paymentRequestCreation.getLinks().getConsentApproval().getHref());

        PaymentResponse res = getPaymentResponse(paymentRequestResourceEntity);

        paymentResponses.add(res);

        return res;
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        HalPaymentRequestEntity paymentRequestEntity =
                apiClient.fetchPayment(paymentRequest.getPayment().getUniqueId());

        PaymentRequestResourceEntity payment = paymentRequestEntity.getPaymentRequest();

        return getPaymentResponse(payment);
    }

    private PaymentResponse getPaymentResponse(PaymentRequestResourceEntity payment) {
        AmountTypeEntity amountTypeEntity =
                payment.getCreditTransferTransaction().get(0).getInstructedAmount();

        return new PaymentResponse(
                new Payment.Builder()
                        .withUniqueId(payment.getResourceId())
                        .withAmount(
                                new Amount(
                                        amountTypeEntity.getCurrency(),
                                        Double.parseDouble(amountTypeEntity.getAmount())))
                        .withStatus(PaymentStatus.PENDING)
                        .withCreditor(
                                new Creditor(
                                        new IbanIdentifier(
                                                payment.getBeneficiary()
                                                        .getCreditorAccount()
                                                        .getIban()),
                                        payment.getBeneficiary().getCreditor().getName()))
                        .withDebtor(
                                new Debtor(
                                        new IbanIdentifier(payment.getDebtorAccount().getIban())))
                        .withExecutionDate(
                                parseDate(
                                                payment.getCreditTransferTransaction().stream()
                                                        .findFirst()
                                                        .orElseThrow(IllegalStateException::new)
                                                        .getRequestedExecutionDate())
                                        .toLocalDate())
                        .build());
    }

    private LocalDateTime parseDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(DateFormat.DATE_FORMAT);
            return LocalDateTime.ofInstant(format.parse(date).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            return OffsetDateTime.parse(date).toLocalDateTime();
        }
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        paymentMultiStepRequest.getPayment().setStatus(PaymentStatus.SIGNED);
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(),
                AuthenticationStepConstants.STEP_FINALIZE,
                null);
    }

    @Override
    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return null;
    }

    @Override
    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return null;
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        return new PaymentListResponse(paymentResponses);
    }
}
