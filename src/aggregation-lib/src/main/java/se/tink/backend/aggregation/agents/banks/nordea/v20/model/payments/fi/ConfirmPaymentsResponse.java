package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments.fi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfirmPaymentsResponse {
    private ConfirmPaymentsOut confirmPaymentsOut;

    private BankingServiceResponse bankingServiceResponse;

    public ConfirmPaymentsOut getConfirmPaymentsOut() {
        return confirmPaymentsOut;
    }

    public void setConfirmPaymentsOut(ConfirmPaymentsOut confirmPaymentsOut) {
        this.confirmPaymentsOut = confirmPaymentsOut;
    }

    public boolean isPaymentSigned() {

        return confirmPaymentsOut != null && confirmPaymentsOut.getConfirmationStatus() != null
                && confirmPaymentsOut.getConfirmationStatus().getStatusCode() != null
                && confirmPaymentsOut.getConfirmationStatus().getStatusCode().equalsIgnoreCase("Paid");
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(
            BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public String getErrorCode() {
        if (bankingServiceResponse == null || bankingServiceResponse.getErrorMessage() == null) {
            return null;
        }

        return (String) ((Map) bankingServiceResponse.getErrorMessage().get("errorCode")).get("$");
    }

    public boolean isError() {
        return getErrorCode() != null;
    }

}
