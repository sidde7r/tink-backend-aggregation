package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian.entity;

import java.util.List;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.backend.aggregation.nxgen.http.form.AbstractForm;

public class Form extends AbstractForm {

    public Form(final MultivaluedMap<String, String> initMap) {
        initMap.forEach(this::putFirstValue);
    }

    private void putFirstValue(String key, List<String> value) {
        if (!value.isEmpty()) {
            put(key, value.get(0));
        }
    }
}
