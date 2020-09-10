package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@Getter
@JsonObject
public class ValidateOCRResponse extends AbstractResponse {

    @JsonIgnore
    public void validate() {
        if (getStatusCode() != 200 || "400".equalsIgnoreCase(getResponseMessage())) {
            throw createOCRValidationError();
        }
    }

    @JsonIgnore
    private TransferExecutionException createOCRValidationError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.INVALID_OCR.getKey().get())
                .setEndUserMessage(EndUserMessage.INVALID_OCR)
                .build();
    }
}
