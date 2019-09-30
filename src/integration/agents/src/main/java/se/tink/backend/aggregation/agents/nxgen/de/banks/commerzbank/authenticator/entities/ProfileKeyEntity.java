package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.CommerzbankConstants.CompleteAppRegistration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class ProfileKeyEntity {
    private String description;
    private String id = "";

    private ProfileKeyEntity() {
        this.description = CompleteAppRegistration.DESCRIPTION + getFormattedDateSuffix();
    }

    public static ProfileKeyEntity create() {
        return new ProfileKeyEntity();
    }

    @JsonIgnore
    private String getFormattedDateSuffix() {
        Date date = new Date();
        String dateAsString = ThreadSafeDateFormat.FORMATTER_DOTTED_DAILY.format(date);

        return " - " + dateAsString;
    }
}
