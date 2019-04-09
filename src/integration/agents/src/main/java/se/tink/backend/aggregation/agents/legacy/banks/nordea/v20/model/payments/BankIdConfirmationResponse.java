package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.base.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BankIdConfirmationResponse {

    private BankIdConfirmationOut bankIdConfirmationOut;
    private BankingServiceResponse bankingServiceResponse;

    public BankIdConfirmationOut getBankIdConfirmationOut() {
        return bankIdConfirmationOut;
    }

    public void setBankIdConfirmationOut(BankIdConfirmationOut bankIdConfirmationOut) {
        this.bankIdConfirmationOut = bankIdConfirmationOut;
    }

    public boolean isPaymentSigned() {
        if (bankIdConfirmationOut != null
                && bankIdConfirmationOut.getConfirmPayments() != null
                && bankIdConfirmationOut.getConfirmPayments().getConfirmationStatus() != null
                && bankIdConfirmationOut
                                .getConfirmPayments()
                                .getConfirmationStatus()
                                .getStatusCode()
                        != null) {

            String statusCode =
                    bankIdConfirmationOut
                            .getConfirmPayments()
                            .getConfirmationStatus()
                            .getStatusCode();

            // For bank transfers the complete status is "Paid".
            // For payments the complete status is "Confirmed".

            if (Objects.equal(statusCode, "Paid") || Objects.equal(statusCode, "Confirmed")) {
                return true;
            }
        }
        return false;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public Optional<String> getErrorCode() {
        if (bankingServiceResponse != null) {
            return bankingServiceResponse.getErrorCode();
        }

        return Optional.empty();
    }

    public boolean isError() {
        return bankingServiceResponse != null && bankingServiceResponse.isError();
    }
}
