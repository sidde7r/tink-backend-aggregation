package se.tink.backend.aggregation.agents.nxgen.se.banks.danskebank.executors.payment.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import lombok.Getter;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException;
import se.tink.backend.aggregation.agents.exceptions.transfer.TransferExecutionException.EndUserMessage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.rpc.AbstractResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.signableoperation.enums.SignableOperationStatuses;

@Getter
@JsonObject
public class ValidateGiroResponse extends AbstractResponse {
    private String giroName;

    @JsonIgnore
    public ValidateGiroResponse validate() {
        if (Strings.isNullOrEmpty(giroName)) {
            throw createGiroValidationError();
        }
        return this;
    }

    @JsonIgnore
    private TransferExecutionException createGiroValidationError() {
        return TransferExecutionException.builder(SignableOperationStatuses.CANCELLED)
                .setMessage(EndUserMessage.INVALID_DESTINATION.getKey().get())
                .setEndUserMessage(EndUserMessage.INVALID_DESTINATION)
                .build();
    }
}
