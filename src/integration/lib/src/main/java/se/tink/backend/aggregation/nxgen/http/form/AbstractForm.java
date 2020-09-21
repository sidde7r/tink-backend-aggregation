package se.tink.backend.aggregation.nxgen.http.form;

import javax.annotation.Nonnull;

public abstract class AbstractForm {

    private final Form.Builder formBuilder = Form.builder();

    public String getBodyValue() {
        return formBuilder.build().serialize();
    }

    /** Add key-value parameter. */
    public void put(@Nonnull String key, @Nonnull String value) {
        formBuilder.put(key, value);
    }

    /** Add parameter without a value. */
    public void put(@Nonnull String key) {
        formBuilder.put(key);
    }
}
