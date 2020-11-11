package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Locale;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.BelfiusConstants.FormValues;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities.JwtHeader;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities.OriginAccount;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.entities.RemoteAccount;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.executor.payment.rpc.CreatePaymentRequest;
import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationValidator;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.eidassigner.QsealcAlg;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.QsealcSignerImpl;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.CreateBeneficiaryMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentExecutor;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentMultiStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentRequest;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentResponse;
import se.tink.backend.aggregation.nxgen.controllers.signing.SigningStepConstants;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.date.CountryDateHelper;
import se.tink.libraries.payment.enums.PaymentStatus;
import se.tink.libraries.payment.rpc.Creditor;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.serialization.utils.SerializationUtils;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class BelfiusPaymentExecutor implements PaymentExecutor {

    private static final Locale DEFAULT_LOCALE = Locale.getDefault();
    private static CountryDateHelper dateHelper = new CountryDateHelper(DEFAULT_LOCALE);

    private BelfiusApiClient apiClient;
    private SessionStorage sessionStorage;
    private AgentsServiceConfiguration configuration;
    private EidasIdentity eidasIdentity;

    public BelfiusPaymentExecutor(
            BelfiusApiClient apiClient,
            SessionStorage sessionStorage,
            AgentsServiceConfiguration configuration,
            EidasIdentity eidasIdentity) {
        this.configuration = configuration;
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
        this.eidasIdentity = eidasIdentity;
    }

    @Override
    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        final Payment payment = paymentRequest.getPayment();
        final Creditor creditor = payment.getCreditor();

        // Backwards compatibility patch: some agents would break if the dueDate was null, so we
        // defaulted it. This behaviour is no longer true for agents that properly implement the
        // execution of future dueDate. For more info about the fix, check PAY-549; for the support
        // of future dueDate, check PAY1-273.
        if (payment.getExecutionDate() == null) {
            payment.setExecutionDate(dateHelper.getNowAsLocalDate());
        }

        RemittanceInformation remittanceInformation =
                paymentRequest.getPayment().getRemittanceInformation();
        RemittanceInformationValidator.validateSupportedRemittanceInformationTypesOrThrow(
                remittanceInformation, null, RemittanceInformationType.UNSTRUCTURED);

        CreatePaymentRequest createPaymentRequest =
                new CreatePaymentRequest(
                        payment.getAmount().getValue(),
                        remittanceInformation.getValue(),
                        FormValues.FREE_TEXT,
                        payment.getAmount().getCurrency(),
                        payment.getExecutionDate()
                                .format(DateTimeFormatter.ofPattern(FormValues.DATE_FORMAT)),
                        new OriginAccount(payment.getDebtor().getAccountNumber()),
                        payment.getUniqueId(),
                        new RemoteAccount(null, creditor.getAccountNumber(), creditor.getName()),
                        null);

        QsealcSigner signer =
                QsealcSignerImpl.build(
                        configuration.getEidasProxy().toInternalConfig(),
                        QsealcAlg.EIDAS_RSA_SHA256,
                        eidasIdentity);

        JwtHeader header = new JwtHeader();

        String jwt =
                Base64.getEncoder()
                                .encodeToString(
                                        SerializationUtils.serializeToString(header).getBytes())
                        + "."
                        + Base64.getEncoder()
                                .encodeToString(
                                        SerializationUtils.serializeToString(createPaymentRequest)
                                                .getBytes());

        jwt += "." + Base64.getEncoder().encodeToString(signer.getSignature(jwt.getBytes()));
        String authorizeUrl = apiClient.createPayment(createPaymentRequest, jwt);

        sessionStorage.put(payment.getUniqueId(), authorizeUrl);
        payment.setStatus(PaymentStatus.PENDING);

        return new PaymentResponse(payment);
    }

    @Override
    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        final Payment payment = paymentMultiStepRequest.getPayment();
        payment.setStatus(PaymentStatus.PAID);

        return new PaymentMultiStepResponse(
                payment, SigningStepConstants.STEP_FINALIZE, new ArrayList<>());
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
}
