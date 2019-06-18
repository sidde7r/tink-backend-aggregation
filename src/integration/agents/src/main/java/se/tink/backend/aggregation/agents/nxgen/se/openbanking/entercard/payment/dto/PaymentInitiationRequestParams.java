package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.dto;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardAccountType;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.payment.enums.EnterCardPaymentRequestType;

public class PaymentInitiationRequestParams {

    private Long clearingNumber;
    private Long ocrNumber;
    private EnterCardPaymentRequestType requestType;
    private EnterCardAccountType accountType;

    public PaymentInitiationRequestParams(
            Long clearingNumber,
            Long ocrNumber,
            EnterCardPaymentRequestType requestType,
            EnterCardAccountType accountType) {
        this.clearingNumber = clearingNumber;
        this.ocrNumber = ocrNumber;
        this.requestType = requestType;
        this.accountType = accountType;
    }

    public Long getClearingNumber() {
        return clearingNumber;
    }

    public Long getOcrNumber() {
        return ocrNumber;
    }

    public EnterCardPaymentRequestType getRequestType() {
        return requestType;
    }

    public EnterCardAccountType getAccountType() {
        return accountType;
    }
}
