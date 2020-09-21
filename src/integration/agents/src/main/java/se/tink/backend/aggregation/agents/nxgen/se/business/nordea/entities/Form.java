package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.entities;

import java.util.Map;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class Form extends AbstractForm {

    public Form() {}

    public Form(final Map<String, String> initMap) {
        initMap.forEach(this::put);
    }
}
