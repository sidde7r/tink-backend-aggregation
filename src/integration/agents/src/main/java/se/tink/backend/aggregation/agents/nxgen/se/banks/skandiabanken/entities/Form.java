package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.entities;

import java.util.Map;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class Form extends AbstractForm {
    public Form() {}

    public Form(final Map<String, String> initMap) {
        initMap.forEach(this::put);
    }

    public void put(@Nonnull String key, @Nonnull String value) {
        super.put(key, value);
    }
}
