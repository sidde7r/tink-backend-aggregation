package se.tink.backend.sms.gateways.cmtelecom.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE)
public class BulkSmsRequest {
    public Messages messages;

    public static Builder builder() {
        return new Builder();
    }

    public final static class Builder {
        private String apiKey;
        private String from;
        private String to;
        private String message;

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder from(String from) {
            this.from = from;
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

        public BulkSmsRequest build() {
            Preconditions.checkState(!Strings.isNullOrEmpty(apiKey), "Api key must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(to), "To must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(from), "From must not be null or empty.");
            Preconditions.checkState(!Strings.isNullOrEmpty(message), "Message must not be null or empty.");

            Msg msg = new Msg();
            msg.setFrom(from);
            msg.setTo(Lists.newArrayList(new To(to)));
            msg.setBody(new Body(message));

            Messages messages = new Messages();

            messages.setAuthentication(new Authentication(apiKey));
            messages.setMsg(Lists.newArrayList(msg));

            BulkSmsRequest request = new BulkSmsRequest();
            request.messages = messages;

            return request;
        }
    }
}
