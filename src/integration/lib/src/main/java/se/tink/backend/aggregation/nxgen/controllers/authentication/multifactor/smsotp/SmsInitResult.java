package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.smsotp;

public class SmsInitResult<T> {

    private final boolean required;
    private final T token;

    public SmsInitResult(boolean required) {
        this.required = required;
        this.token = null;
    }

    public SmsInitResult(boolean required, T token) {
        this.required = required;
        this.token = token;
    }

    public boolean isRequired() {
        return required;
    }

    public T getToken() {
        return token;
    }
}
