package se.tink.backend.product.execution.unit.agents.sbab.mortgage.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class ApplicationErrorEntity {
    @JsonProperty("valideringsFel")
    private Map<String, String> errors;

    public Collection<String> getErrors() {
        return !errors.isEmpty() ? errors.values() : Collections.<String>emptyList();
    }

    public Optional<String> getError() {
        Collection<String> errors = getErrors();

        if (errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(Strings.emptyToNull(Iterables.get(errors, 0)));
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
