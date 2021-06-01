package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentCancelledException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentRejectedException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Amt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CdtTrfTxInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Cdtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CdtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.CstmrCdtTrfInitn;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Dbtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.DbtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.GrpHdr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.IbanId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Id;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.InitgPty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.InstdAmt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.OrgId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.Othr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.PmtId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.PmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.PmtTpInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.RmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.SchmeNm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.entities.SvcLvl;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.payment.rpc.CreatePaymentXmlRequest;
import se.tink.backend.aggregation.agents.utils.berlingroup.common.LinksEntity;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentAuthenticator;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.PaymentConstants.StorageValues;
import se.tink.backend.aggregation.agents.utils.berlingroup.payment.rpc.CreatePaymentResponse;
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
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.PaymentServiceType;

@RequiredArgsConstructor
@Slf4j
public class FiduciaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {
    private final PaymentAuthenticator authenticator;
    private final FiduciaApiClient apiClient;
    private final Credentials credentials;
    private final SessionStorage sessionStorage;

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();

        CreatePaymentXmlRequest createPaymentXmlRequest;
        if (PaymentServiceType.PERIODIC.equals(payment.getPaymentServiceType())) {
            createPaymentXmlRequest = getCreateRecurringPaymentRequest(payment);
        } else {
            createPaymentXmlRequest = getCreatePaymentRequest(payment);
        }

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        createPaymentXmlRequest,
                        credentials.getField(FiduciaConstants.CredentialKeys.PSU_ID),
                        paymentRequest);

        sessionStorage.put(StorageValues.SCA_LINKS, createPaymentResponse.getLinks());

        return createPaymentResponse.toTinkPayment(payment);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        LinksEntity scaLinks =
                sessionStorage
                        .get(StorageValues.SCA_LINKS, LinksEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException(ErrorMessages.MISSING_SCA_URL));

        authenticator.authenticatePayment(credentials, scaLinks);

        Payment payment = paymentMultiStepRequest.getPayment();
        PaymentResponse paymentResponse = fetch(paymentMultiStepRequest);
        PaymentStatus paymentStatus = paymentResponse.getPayment().getStatus();
        log.info("Payment id={} sign status={}", payment.getId(), paymentStatus);

        switch (paymentStatus) {
            case SIGNED:
            case PAID:
                return new PaymentMultiStepResponse(
                        paymentResponse,
                        AuthenticationStepConstants.STEP_FINALIZE,
                        Collections.emptyList());
            case REJECTED:
                throw new PaymentRejectedException("Payment rejected by Bank");
            case CANCELLED:
                throw new PaymentCancelledException("Payment Cancelled by PSU");

            default:
                log.error("Payment was not signed even after waiting for SCA");
                throw new PaymentAuthorizationException();
        }
    }

    @Override
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        return apiClient
                .fetchPaymentStatus(paymentRequest)
                .toTinkPayment(paymentRequest.getPayment());
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
        throw new NotImplementedException(
                "fetchMultiple not yet implemented for " + this.getClass().getName());
    }

    private CreatePaymentXmlRequest getCreatePaymentRequest(Payment payment) {
        Creditor creditor = payment.getCreditor();
        Debtor debtor = payment.getDebtor();
        ExactCurrencyAmount amount = payment.getExactCurrencyAmount();

        Othr other = new Othr(FormValues.OTHER_ID, new SchmeNm(FormValues.SCHEME_NAME));

        GrpHdr groupHeader =
                new GrpHdr(
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)),
                        String.valueOf(amount.getDoubleValue()),
                        new InitgPty(new Id(new OrgId(other)), FormValues.PAYMENT_INITIATOR),
                        FormValues.NUMBER_OF_TRANSACTIONS,
                        FormValues.MESSAGE_ID);

        CdtTrfTxInf trfInf =
                new CdtTrfTxInf(
                        new Cdtr(creditor.getName()),
                        new CdtrAcct(new IbanId(creditor.getAccountNumber())),
                        new PmtId(FormValues.PAYMENT_ID),
                        new Amt(
                                new InstdAmt(
                                        amount.getCurrencyCode(),
                                        String.valueOf(amount.getDoubleValue()))),
                        new RmtInf(FormValues.RMT_INF));

        PmtInf paymentInfo =
                new PmtInf(
                        trfInf,
                        new PmtTpInf(new SvcLvl(FormValues.PAYMENT_TYPE)),
                        debtor == null ? null : new DbtrAcct(new IbanId(debtor.getAccountNumber())),
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)),
                        FormValues.PAYMENT_INFORMATION_ID,
                        String.valueOf(amount.getDoubleValue()),
                        new Dbtr("NOTPROVIDED"));

        return new CreatePaymentXmlRequest(new CstmrCdtTrfInitn(groupHeader, paymentInfo));
    }

    private CreatePaymentXmlRequest getCreateRecurringPaymentRequest(Payment payment) {
        return getCreatePaymentRequest(payment);
    }
}
