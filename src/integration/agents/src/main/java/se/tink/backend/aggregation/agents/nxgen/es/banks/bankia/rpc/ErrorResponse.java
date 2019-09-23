package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.BankiaConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    @JsonProperty private String operationResult;

    @JsonProperty private String operationMessage;

    public String getOperationResult() {
        return operationResult;
    }

    public String getOperationMessage() {
        return operationMessage;
    }

    public boolean isNoDataForEnquiry() {
        return Errors.NO_DATA_FOR_ENQUIRY_CODE.equalsIgnoreCase(operationResult)
                && Errors.NO_DATA_FOR_ENQUIRY_MESSAGE.equalsIgnoreCase(operationMessage);
    }
}
