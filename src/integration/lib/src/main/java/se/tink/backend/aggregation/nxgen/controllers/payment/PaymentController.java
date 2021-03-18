package se.tink.backend.aggregation.nxgen.controllers.payment;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.BankIdException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankIdError;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.payment.enums.PaymentType;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.signableoperation.enums.InternalStatus;

public class PaymentController {
    private final PaymentExecutor paymentExecutor;
    private final FetchablePaymentExecutor fetchablePaymentExecutor;

    public PaymentController(PaymentExecutor paymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = null;
    }

    public PaymentController(
            PaymentExecutor paymentExecutor, FetchablePaymentExecutor fetchablePaymentExecutor) {
        this.paymentExecutor = paymentExecutor;
        this.fetchablePaymentExecutor = fetchablePaymentExecutor;
    }

    public PaymentResponse create(PaymentRequest paymentRequest) throws PaymentException {
        return paymentExecutor.create(paymentRequest);
    }

    public PaymentMultiStepResponse sign(PaymentMultiStepRequest paymentMultiStepRequest)
            throws PaymentException {
        try {
            return paymentExecutor.sign(paymentMultiStepRequest);
        } catch (AuthenticationException e) {
            if (e instanceof BankIdException) {
                BankIdError bankIdError = ((BankIdException) e).getError();
                switch (bankIdError) {
                    case CANCELLED:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.CANCELLED,
                                InternalStatus.BANKID_CANCELLED,
                                e);
                    case NO_CLIENT:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.NO_CLIENT,
                                InternalStatus.BANKID_NO_RESPONSE,
                                e);
                    case TIMEOUT:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.TIMEOUT, InternalStatus.BANKID_TIMEOUT, e);
                    case INTERRUPTED:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.INTERRUPTED,
                                InternalStatus.BANKID_INTERRUPTED,
                                e);
                    case UNKNOWN:
                    default:
                        throw new PaymentAuthorizationException(
                                PaymentConstants.BankId.UNKNOWN,
                                InternalStatus.BANKID_UNKNOWN_EXCEPTION,
                                e);
                }
            }

            throw new PaymentAuthorizationException(
                    "Payment could not be signed", InternalStatus.BANKID_UNKNOWN_EXCEPTION, e);
        }
    }

    public CreateBeneficiaryMultiStepResponse createBeneficiary(
            CreateBeneficiaryMultiStepRequest createBeneficiaryMultiStepRequest) {
        return paymentExecutor.createBeneficiary(createBeneficiaryMultiStepRequest);
    }

    public PaymentResponse cancel(PaymentRequest paymentRequest) {
        return paymentExecutor.cancel(paymentRequest);
    }

    public PaymentResponse fetch(PaymentRequest paymentRequest) throws PaymentException {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetch(paymentRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public PaymentListResponse fetchMultiple(PaymentListRequest paymentListRequest)
            throws PaymentException {
        if (canFetch()) {
            return fetchablePaymentExecutor.fetchMultiple(paymentListRequest);
        } else {
            throw new UnsupportedOperationException(
                    "This payment controller doesn't support fetching.");
        }
    }

    public boolean canFetch() {
        return fetchablePaymentExecutor != null;
    }

    public PaymentType getPaymentProductType(Payment payment) {
        String marketCode = "";
        AccountIdentifierType accountIdentifierType =
                payment.getDebtor().getAccountIdentifierType();
        String accountNumber = payment.getDebtor().getAccountNumber();

        marketCode = payment.getMarketCode(accountNumber, marketCode, accountIdentifierType);
        return AccountTypeToPaymentTypeMapper.getType(payment, marketCode);
    }
}
