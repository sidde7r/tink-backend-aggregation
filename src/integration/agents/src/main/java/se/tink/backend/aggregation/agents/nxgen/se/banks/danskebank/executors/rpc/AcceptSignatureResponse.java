package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class AcceptSignatureResponse extends AbstractResponse {
    private String error;
    private String payload;
    private int status;
    private String traceId;

    @JsonIgnore
    public void validate() {
        if (getStatusCode() != 200) {
            throw signatureFailureError();
        }
    }

    @JsonIgnore
    private TransferExecutionException signatureFailureError() {
        return TransferExecutionException.builder(SignableOperationStatuses.FAILED)
                .setMessage(EndUserMessage.SIGN_TRANSFER_FAILED.getKey().get())
                .setEndUserMessage(EndUserMessage.SIGN_TRANSFER_FAILED)
                .build();
    }
}
