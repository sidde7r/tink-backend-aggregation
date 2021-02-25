package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment;

import java.security.interfaces.RSAPrivateKey;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaApiClient;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.SignatureValues;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.Amt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.CdtTrfTxInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.Cdtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.CdtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.CstmrCdtTrfInitn;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.Dbtr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.DbtrAcct;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.GrpHdr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.IbanId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.Id;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.InitgPty;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.InstdAmt;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.OrgId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.Othr;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PmtId;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PmtTpInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.PsuDataEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.RmtInf;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.SchmeNm;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.entities.SvcLvl;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.AuthorizePaymentRequest;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.CreatePaymentResponse;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.rpc.PaymentDocument;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.SignatureUtils;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.utils.XmlConverter;
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
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Debtor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class FiduciaPaymentExecutor implements PaymentExecutor, FetchablePaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private final FiduciaApiClient apiClient;
    private final String psuId;
    private final String password;
    private final String qsealcDerBase64;
    private final RSAPrivateKey privateKey;

    public FiduciaPaymentExecutor(
            FiduciaApiClient apiClient, String qsealcDerBase64, String psuId, String password) {
        this.apiClient = apiClient;
        this.psuId = psuId;
        this.password = password;
        this.qsealcDerBase64 = qsealcDerBase64;

        // TODO change made for secrets cleanup purposes (prev values were used by Vega for sbx)
        // Entire payment executor has to be refactored to use QSealC signer
        privateKey = null;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        Creditor creditor = payment.getCreditor();
        Debtor debtor = payment.getDebtor();

        ExactCurrencyAmount amount = payment.getExactCurrencyAmount();

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

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
                        new DbtrAcct(new IbanId(debtor.getAccountNumber())),
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)),
                        FormValues.CHRG_BR,
                        FormValues.PAYMENT_INFORMATION_ID,
                        String.valueOf(amount.getDoubleValue()),
                        new Dbtr(psuId),
                        FormValues.NUMBER_OF_TRANSACTIONS,
                        FormValues.PAYMENT_METHOD);

        PaymentDocument document =
                new PaymentDocument(new CstmrCdtTrfInitn(groupHeader, paymentInfo));
        String body = XmlConverter.convertToXml(document);

        String digest = SignatureUtils.createDigest(body);
        String date = SignatureUtils.getCurrentDateFormatted();
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SignatureUtils.createSignature(
                        privateKey,
                        SignatureValues.HEADERS_WITH_PSU_ID,
                        digest,
                        reqId,
                        date,
                        psuId);

        CreatePaymentResponse createPaymentResponse =
                apiClient.createPayment(
                        body, psuId, digest, qsealcDerBase64, signature, reqId, date);

        return createPaymentResponse.toTinkPayment(creditor, debtor, amount);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest) {
        Payment payment = paymentMultiStepRequest.getPayment();
        String body =
                SerializationUtils.serializeToString(
                        new AuthorizePaymentRequest(new PsuDataEntity(password)));

        String digest = SignatureUtils.createDigest(body);
        String date = SignatureUtils.getCurrentDateFormatted();
        String reqId = String.valueOf(UUID.randomUUID());

        String signature =
                SignatureUtils.createSignature(
                        privateKey,
                        SignatureValues.HEADERS_WITH_PSU_ID,
                        digest,
                        reqId,
                        date,
                        psuId);

        apiClient.authorizePayment(
                payment.getUniqueId(),
                body,
                psuId,
                digest,
                qsealcDerBase64,
                signature,
                reqId,
                date);
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(payment, SigningStepConstants.STEP_FINALIZE, null);
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
    public PaymentResponse fetch(PaymentRequest paymentRequest) {
        Payment payment = paymentRequest.getPayment();
        String paymentId = payment.getUniqueId();
        String digest = SignatureUtils.createDigest(SignatureValues.EMPTY_BODY);
        String date = SignatureUtils.getCurrentDateFormatted();
        String reqId = String.valueOf(UUID.randomUUID());
        String signature =
                SignatureUtils.createSignature(
                        privateKey, SignatureValues.HEADERS, digest, reqId, date, null);

        PaymentDocument paymentDocument =
                apiClient.getPayment(paymentId, digest, qsealcDerBase64, signature, reqId, date);

        return paymentDocument.toTinkPayment(paymentId, payment.getStatus());
    }

    @Override
    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest) {
        return new PaymentListResponse(
                paymentListRequest.getPaymentRequestList().stream()
                        .map(paymentRequest -> new PaymentResponse(paymentRequest.getPayment()))
                        .collect(Collectors.toList()));
    }
}
