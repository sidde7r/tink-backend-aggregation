package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
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
    private static final Logger logger = LoggerFactory.getLogger(CmcicPaymentExecutor.class);

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
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        apiClient.fetchPisOauthToken();

        PaymentRequestResourceEntity paymentRequestResourceEntity =
                buildPaymentRequest(paymentRequest);
        HalPaymentRequestCreation paymentRequestCreation =
                apiClient.makePayment(paymentRequestResourceEntity);

        String authorizeUrl =
                Optional.ofNullable(
                                paymentRequestCreation.getLinks().getConsentApproval().getHref())
                        .orElseThrow(
                                () -> {
                                    logger.error(
                                            "Payment authorization failed. There is no authentication url!");
                                    return new PaymentAuthorizationException(
                                            "Payment authorization failed.",
                                            new PaymentRejectedException());
                                });

        sessionStorage.put(StorageKeys.AUTH_URL, authorizeUrl);

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
                                parseDate(payment.getRequestedExecutionDate()).toLocalDate())
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

    private PaymentRequestResourceEntity buildPaymentRequest(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

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
                new PaymentIdentificationEntity(
                        payment.getUniqueId(), UUIDUtils.generateUUID(), null);

        AmountTypeEntity instructedAmount =
                new AmountTypeEntity(
                        payment.getAmount().getCurrency(),
                        payment.getAmount().getValue().toString());

        List<CreditTransferTransactionEntity> creditTransferTransaction =
                Collections.singletonList(
                        CreditTransferTransactionEntity.builder()
                                .paymentId(paymentId)
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

        return PaymentRequestResourceEntity.builder()
                .paymentInformationId(UUIDUtils.generateUUID())
                .creationDateTime(OffsetDateTime.now(Clock.systemDefaultZone()).toString())
                .numberOfTransactions(FormValues.NUMBER_OF_TRANSACTIONS)
                .requestedExecutionDate(getExecutionDate(payment.getExecutionDate()))
                .initiatingParty(initiatingParty)
                .paymentTypeInformation(paymentTypeInformation)
                .debtorAccount(debtorAccount)
                .beneficiary(beneficiary)
                .creditTransferTransaction(creditTransferTransaction)
                .supplementaryData(supplementaryData)
                .build();
    }

    private String getExecutionDate(LocalDate localDate) {
        return Optional.ofNullable(localDate)
                .map(
                        date ->
                                localDate
                                        .atStartOfDay()
                                        .atZone(ZoneId.of("CET"))
                                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME))
                .orElse(
                        LocalDateTime.now()
                                .atZone(ZoneId.of("CET"))
                                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
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
