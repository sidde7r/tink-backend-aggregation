package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UserMessage {
    private String header;
    private List<Message> messages;

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("header", header)
                .append("messages", messages)
                .toString();
    }

    public String getHeader() {
        return header;
    }

    public UserMessage setHeader(String header) {
        this.header = header;
        return this;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public UserMessage setMessages(
            List<Message> messages) {
        this.messages = messages;
        return this;
    }



    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Message {
        private String key;
        private String value;

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("key", key)
                    .append("value", value)
                    .toString();
        }

        public String getKey() {
            return key;
        }

        public Message setKey(String key) {
            this.key = key;
            return this;
        }

        public String getValue() {
            return value;
        }

        public Message setValue(String value) {
            this.value = value;
            return this;
        }
    }
}
