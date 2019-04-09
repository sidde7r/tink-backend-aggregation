package se.tink.backend.aggregation.agents.banks.nordea.v20.model.payments;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaErrorUtils;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.BankingServiceResponse;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferResponse {
    private CreatePaymentOut createPaymentOut;
    private BankingServiceResponse bankingServiceResponse;

    public CreatePaymentOut getCreatePaymentOut() {
        return createPaymentOut;
    }

    public void setCreatePaymentOut(CreatePaymentOut createPaymentOut) {
        this.createPaymentOut = createPaymentOut;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    public Optional<String> getError() {
        if (getBankingServiceResponse() != null) {
            if (getBankingServiceResponse().getErrorCode().isPresent()) {

                return Optional.ofNullable(
                        NordeaErrorUtils.getErrorMessage(
                                getBankingServiceResponse().getErrorCode().get()));
            }
        }

        if (!createPaymentOut.getStatusCode().equalsIgnoreCase("Unconfirmed")) {
            return Optional.ofNullable("Something went wrong making transfer.");
        }

        return Optional.empty();
    }
}
