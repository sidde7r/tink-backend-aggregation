package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration.CmcicConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AccountIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.AmountTypeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.CreditTransferTransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestCreation;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.HalPaymentRequestEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PartyIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentIdentificationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentRequestResourceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.PaymentTypeInformationEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.ServiceLevelCodeEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity.SupplementaryDataEntity.AcceptedAuthenticationApproachEnum;
import se.tink.backend.aggregation.nxgen.controllers.authentication.AuthenticationStepConstants;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
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

public class CmcicPaymentExecutor implements PaymentExecutor {

    private CmcicApiClient apiClient;
    private SessionStorage sessionStorage;
    private CmcicConfiguration configuration;
    private List<PaymentResponse> paymentResponses;

    public CmcicPaymentExecutor(
            CmcicApiClient apiClient,
            SessionStorage sessionStorage,
            CmcicConfiguration configuration) {
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
                AccountIdentificationEntity.builder()
                        .iban(paymentRequest.getPayment().getDebtor().getAccountNumber())
                        .build();

        PartyIdentificationEntity creditor =
                PartyIdentificationEntity.builder().name(FormValues.BENEFICIARY_NAME).build();

        AccountIdentificationEntity creditorAccount =
                AccountIdentificationEntity.builder()
                        .iban(paymentRequest.getPayment().getCreditor().getAccountNumber())
                        .build();

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
                                                payment.getCreditTransferTransaction()
                                                        .get(0)
                                                        .getRequestedExecutionDate())
                                        .toLocalDate())
                        .build());
    }

    private LocalDateTime parseDate(String date) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(CmcicConstants.DateFormat.DATE_FORMAT);
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
