package se.tink.backend.aggregation.agents.abnamro.client.exceptions;

public class RejectedAccountException extends Exception {
    private Integer rejectedReasonCode;

    public RejectedAccountException(Integer rejectedReasonCode) {
        this.rejectedReasonCode = rejectedReasonCode;
    }

    public Integer getRejectedReasonCode() {
        return rejectedReasonCode;
    }
}
