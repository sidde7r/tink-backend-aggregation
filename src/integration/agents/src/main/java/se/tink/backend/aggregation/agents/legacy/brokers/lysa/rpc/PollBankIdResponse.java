package se.tink.backend.aggregation.agents.brokers.lysa.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.brokers.lysa.model.PayloadEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PollBankIdResponse {
    public static class Status {
        public static final String USER_SIGN = "USER_SIGN";
        public static final String OUTSTANDING_TRANSACTION = "OUTSTANDING_TRANSACTION";
        public static final String NO_CLIENT = "NO_CLIENT";
        public static final String COMPLETE = "COMPLETE";
        public static final String ERROR = "ERROR";
        public static final String EXPIRED_TRANSACTION = "EXPIRED_TRANSACTION";
        public static final String ABORTED = "ABORTED";
        public static final String STARTED = "STARTED";
    }

    private String status;

    public PayloadEntity getPayload() {
        return payload;
    }

    public void setPayload(PayloadEntity payload) {
        this.payload = payload;
    }

    private PayloadEntity payload;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
