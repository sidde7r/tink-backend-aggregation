package se.tink.backend.sms.gateways.rpc;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class SmsRequest {
    private String sender;
    private String to;
    private String message;

    public static Builder builder() {
        return new Builder();
    }

    public String getSender() {
        return sender;
    }

    public String getTo() {
        return to;
    }

    public String getMessage() {
        return message;
    }

    public final static class Builder {
        private String sender;
        private String to;
        private String message;

        public Builder sender(String sender) {
            this.sender = sender;
            return this;
        }

        public Builder to(String to) {
            this.to = to;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public SmsRequest build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(sender), "Sender must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(to), "To must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(message), "Message must not be null or empty.");

            SmsRequest request = new SmsRequest();
            request.sender = sender;
            request.to = to;
            request.message = message;

            return request;
        }
    }
}

