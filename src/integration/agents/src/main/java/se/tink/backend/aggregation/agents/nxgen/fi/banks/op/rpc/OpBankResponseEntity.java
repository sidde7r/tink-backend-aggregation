package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.entities.UserMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OpBankResponseEntity {
    protected int status;
    private UserMessage userMessageList;

    public boolean serviceNotAvailable() {
        return status == 5;
    }

    public boolean isSuccess() {
        return status == 0;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public int getStatus() {
        return status;
    }

    public OpBankResponseEntity setStatus(int status) {
        this.status = status;
        return this;
    }

    public UserMessage getUserMessageList() {
        return userMessageList;
    }

    public OpBankResponseEntity setUserMessageList(
            UserMessage userMessageList) {
        this.userMessageList = userMessageList;
        return this;
    }
}
