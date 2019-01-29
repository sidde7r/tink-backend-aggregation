package se.tink.backend.aggregation.agents.banks.nordea.v20.model.transfers;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Optional;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.banks.nordea.NordeaErrorUtils;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.BankingServiceResponse;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InternalBankTransferResponse
{
    private CreateTransferOut createTransferOut;
    private BankingServiceResponse bankingServiceResponse;

    public CreateTransferOut getCreateTransferOut ()
    {
        return createTransferOut;
    }

    public void setCreateTransferOut(CreateTransferOut createTransferOut) {
        this.createTransferOut = createTransferOut;
    }

    public BankingServiceResponse getBankingServiceResponse() {
        return bankingServiceResponse;
    }

    public void setBankingServiceResponse(BankingServiceResponse bankingServiceResponse) {
        this.bankingServiceResponse = bankingServiceResponse;
    }

    /**
     * Accepted state for Version 2.3 of the API is StatusCode = "Paid"
     * Accepted state for Version 2.0 of the API is StatusCode = "Empty"
     */
    public boolean isTransferAccepted() {

        return createTransferOut != null && (Strings.nullToEmpty(createTransferOut.getStatusCode())
                .equalsIgnoreCase("Paid") || Strings.isNullOrEmpty(createTransferOut.getStatusCode()));

    }

    public Optional<String> getErrorMessage() {
        if (createTransferOut != null) {
            String warningText = createTransferOut.getWarningText();
            if (!Strings.isNullOrEmpty(warningText)) {
                return Optional.of(warningText);
            }
        }

        if (bankingServiceResponse != null && bankingServiceResponse.getErrorCode().isPresent()) {
            return Optional.of(NordeaErrorUtils.getErrorMessage(bankingServiceResponse.getErrorCode().get()));
        }

        return Optional.empty();
    }

    public SignableOperationStatuses getErrorStatus() {
        if (bankingServiceResponse != null) {
            Optional<String> errorCode = bankingServiceResponse.getErrorCode();
            return NordeaErrorUtils.getTransferErrorStatus(errorCode.get());
        } else {
            return SignableOperationStatuses.FAILED;
        }
    }
}
