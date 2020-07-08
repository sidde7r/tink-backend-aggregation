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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.apiclient.CmcicApiClient;
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
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepConstants;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.uuid.UUIDUtils;

public class CmcicPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private CmcicApiClient apiClient;
    private SessionStorage sessionStorage;
    private CmcicConfiguration configuration;
    private String redirectUrl;
    private List<PaymentResponse> paymentResponses;

    public CmcicPaymentExecutor(
            CmcicApiClient apiClient,
            SessionStorage sessionStorage,
            AgentConfiguration<CmcicConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.configuration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
        paymentResponses = new ArrayList<>();
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        apiClient.fetchPisOauthToken();
        Payment payment = paymentRequest.getPayment();

        String id = UUIDUtils.generateUUID();

        PartyIdentificationEntity initiatingParty =
                new PartyIdentificationEntity(
                        payment.getDebtor().getAccountNumber(), null, null, null);

        PaymentTypeInformationEntity paymentTypeInformation =
                new PaymentTypeInformationEntity(null, ServiceLevelCodeEntity.SEPA, null, null);

        AccountIdentificationEntity debtorAccount =
                new AccountIdentificationEntity(
                        paymentRequest.getPayment().getDebtor().getAccountNumber(), null);

        PartyIdentificationEntity creditor =
                new PartyIdentificationEntity(FormValues.BENEFICIARY_NAME, null, null, null);

        AccountIdentificationEntity creditorAccount =
                new AccountIdentificationEntity(
                        paymentRequest.getPayment().getCreditor().getAccountNumber(), null);

        BeneficiaryEntity beneficiary =
                BeneficiaryEntity.builder()
                        .creditor(creditor)
                        .creditorAccount(creditorAccount)
                        .build();

        PaymentIdentificationEntity paymentId =
                new PaymentIdentificationEntity(null, FormValues.INSTRUCTION_ID, null);

        AmountTypeEntity instructedAmount =
                new AmountTypeEntity(
                        payment.getAmount().getCurrency(),
                        payment.getAmount().getValue().toString());

        List<CreditTransferTransactionEntity> creditTransferTransaction =
                Collections.singletonList(
                        CreditTransferTransactionEntity.builder()
                                .paymentId(paymentId)
                                .requestedExecutionDate(
                                        OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                                .instructedAmount(instructedAmount)
                                .build());

        String callbackUrl =
                redirectUrl + Urls.SUCCESS_REPORT_PATH + sessionStorage.get(StorageKeys.STATE);

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
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
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
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        paymentMultiStepRequest.getPayment().setStatus(PaymentStatus.SIGNED);
        return new PaymentMultiStepResponse(
                paymentMultiStepRequest.getPayment(),
                AuthenticationStepConstants.STEP_FINALIZE,
                null);
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
        return new PaymentListResponse(paymentResponses);
    }
}
