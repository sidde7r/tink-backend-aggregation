package se.tink.backend.aggregation.nxgen.http;

import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.http.form.Form;

public abstract class AbstractForm {

    private final Form.Builder formBuilder = Form.builder();

    public String getBodyValue() {
        return formBuilder.build().serialize();
    }

    /** Add key-value parameter. */
    protected void put(@Nonnull String key, @Nonnull String value) {
        formBuilder.put(key, value);
    }

    /** Add parameter without a value. */
    protected void put(@Nonnull String key) {
        formBuilder.put(key);
    }
}
