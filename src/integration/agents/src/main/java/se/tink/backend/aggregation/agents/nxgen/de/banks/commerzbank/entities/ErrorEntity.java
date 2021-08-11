package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    private boolean cancelling;
    private List<ErrorMessageEntity> errors;

    /**
     * Returns the first error message in the list. Don't think there will ever be more than one,
     * but they seem to like to put everything in lists.
     */
    @JsonIgnore
    public Optional<ErrorMessageEntity> getErrorMessage() {
        if (errors.isEmpty()) {
            return Optional.empty();
        }

        return Optional.ofNullable(errors.get(0));
    }
}
