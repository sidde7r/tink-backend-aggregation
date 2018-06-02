package se.tink.backend.rpc;

import java.util.Date;

public class RefreshCredentialSchedulationRequest {
    private Date nextExecutionTimestamp;
    private Date executionTimestamp;

    public Date getNextExecution() {
        return nextExecutionTimestamp;
    }

    public Date getNow() {
        return executionTimestamp;
    }

    public void setNextExecution(Date nextExecution) {
        this.nextExecutionTimestamp = nextExecution;
    }

    public void setNow(Date now) {
        this.executionTimestamp = now;
    }
}
