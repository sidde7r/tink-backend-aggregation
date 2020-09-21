package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.entities;

import static se.tink.backend.aggregation.nxgen.http.form.Form.builder;

import java.util.Map;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.http.form.Form.Builder;

public class Form {
    private final Builder formBuilder = builder();

    public Form() {}

    public Form(final Map<String, String> initMap) {
        initMap.forEach(this::put);
    }

    public void put(@Nonnull String key, @Nonnull String value) {
        formBuilder.put(key, value);
    }

    public String getBodyValue() {
        return formBuilder.build().serialize();
    }
}
