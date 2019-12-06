package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigInteger;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardCurrency;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardPaymentRequestType;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentInitiationRequest {

    private EnterCardPaymentRequestType paymentRequestType;

    private Long ocrNumber;

    private Long clearingNumber;

    private String kidNumber;

    private Long registrationNumber;

    private BigInteger payeeAccountNumber;

    private EnterCardAccountType receiverAccountType;

    private String payee;

    private String payeeBankName;

    private BigInteger customerAccountNumber;

    private double amount;

    private EnterCardCurrency currency;

    private String message;

    private Long mccCode;

    private String redirectURI;

    public PaymentInitiationRequest() {}

    @JsonIgnore
    private PaymentInitiationRequest(PaymentInitiationRequestBuilder builder) {
        this.paymentRequestType = builder.paymentRequestType;
        this.ocrNumber = builder.ocrNumber;
        this.clearingNumber = builder.clearingNumber;
        this.kidNumber = builder.kidNumber;
        this.registrationNumber = builder.registrationNumber;
        this.payeeAccountNumber = builder.payeeAccountNumber;
        this.receiverAccountType = builder.receiverAccountType;
        this.payee = builder.payee;
        this.payeeBankName = builder.payeeBankName;
        this.customerAccountNumber = builder.customerAccountNumber;
        this.amount = builder.amount;
        this.currency = builder.currency;
        this.message = builder.message;
        this.mccCode = builder.mccCode;
        this.redirectURI = builder.redirectURI;
    }

    public static final class PaymentInitiationRequestBuilder {

        private EnterCardPaymentRequestType paymentRequestType;
        private Long ocrNumber;
        private Long clearingNumber;
        private String kidNumber;
        private Long registrationNumber;
        private BigInteger payeeAccountNumber;
        private EnterCardAccountType receiverAccountType;
        private String payee;
        private String payeeBankName;
        private BigInteger customerAccountNumber;
        private double amount;
        private EnterCardCurrency currency;
        private String message;
        private Long mccCode;
        private String redirectURI;

        public PaymentInitiationRequestBuilder() {}

        public static PaymentInitiationRequestBuilder aPaymentInitiationRequest() {
            return new PaymentInitiationRequestBuilder();
        }

        public PaymentInitiationRequestBuilder withPaymentRequestType(
                EnterCardPaymentRequestType paymentRequestType) {
            this.paymentRequestType = paymentRequestType;
            return this;
        }

        public PaymentInitiationRequestBuilder withOcrNumber(Long ocrNumber) {
            this.ocrNumber = ocrNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withClearingNumber(Long clearingNumber) {
            this.clearingNumber = clearingNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withKidNumber(String kidNumber) {
            this.kidNumber = kidNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withRegistrationNumber(Long registrationNumber) {
            this.registrationNumber = registrationNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withPayeeAccountNumber(
                BigInteger payeeAccountNumber) {
            this.payeeAccountNumber = payeeAccountNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withReceiverAccountType(
                EnterCardAccountType receiverAccountType) {
            this.receiverAccountType = receiverAccountType;
            return this;
        }

        public PaymentInitiationRequestBuilder withPayee(String payee) {
            this.payee = payee;
            return this;
        }

        public PaymentInitiationRequestBuilder withPayeeBankName(String payeeBankName) {
            this.payeeBankName = payeeBankName;
            return this;
        }

        public PaymentInitiationRequestBuilder withCustomerAccountNumber(
                BigInteger customerAccountNumber) {
            this.customerAccountNumber = customerAccountNumber;
            return this;
        }

        public PaymentInitiationRequestBuilder withAmount(double amount) {
            this.amount = amount;
            return this;
        }

        public PaymentInitiationRequestBuilder withCurrency(EnterCardCurrency currency) {
            this.currency = currency;
            return this;
        }

        public PaymentInitiationRequestBuilder withMessage(String message) {
            this.message = message;
            return this;
        }

        public PaymentInitiationRequestBuilder withMccCode(Long mccCode) {
            this.mccCode = mccCode;
            return this;
        }

        public PaymentInitiationRequestBuilder withRedirectURI(String redirectURI) {
            this.redirectURI = redirectURI;
            return this;
        }

        public PaymentInitiationRequest build() {
            PaymentInitiationRequest paymentInitiationRequest = new PaymentInitiationRequest();
            paymentInitiationRequest.registrationNumber = this.registrationNumber;
            paymentInitiationRequest.clearingNumber = this.clearingNumber;
            paymentInitiationRequest.payeeBankName = this.payeeBankName;
            paymentInitiationRequest.mccCode = this.mccCode;
            paymentInitiationRequest.paymentRequestType = this.paymentRequestType;
            paymentInitiationRequest.currency = this.currency;
            paymentInitiationRequest.payee = this.payee;
            paymentInitiationRequest.customerAccountNumber = this.customerAccountNumber;
            paymentInitiationRequest.payeeAccountNumber = this.payeeAccountNumber;
            paymentInitiationRequest.kidNumber = this.kidNumber;
            paymentInitiationRequest.message = this.message;
            paymentInitiationRequest.amount = this.amount;
            paymentInitiationRequest.ocrNumber = this.ocrNumber;
            paymentInitiationRequest.receiverAccountType = this.receiverAccountType;
            paymentInitiationRequest.redirectURI = this.redirectURI;
            return paymentInitiationRequest;
        }
    }
}
