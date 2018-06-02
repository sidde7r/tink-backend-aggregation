package se.tink.backend.system.tasks;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.system.rpc.UpdateTransactionsRequest;

public class UpdateTransactionsTask extends Task<UpdateTransactionsRequest> {

    @JsonCreator
    public UpdateTransactionsTask(@JsonProperty("topic") String topic) { super(topic); }
}
