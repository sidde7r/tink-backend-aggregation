package se.tink.backend.main.providers.twilio;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class SmsRequest extends MultivaluedMapImpl {
    private static final String SENDER = "+46765194000";

    public SmsRequest() {
        add("From", SENDER);
    }

    public void setRecipient(String recipient) {
        add("To", recipient);
    }

    public String getRecipient() {
        return getFirst("To");
    }

    public void setMessage(String message) {
        add("Body", message);
    }

    public String getMessage() {
        return getFirst("Body");
    }
}
