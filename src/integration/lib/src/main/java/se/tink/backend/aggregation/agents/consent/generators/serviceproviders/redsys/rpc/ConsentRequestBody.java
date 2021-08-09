package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@ToString
@Builder
public class ConsentRequestBody {

    private AccessEntity access;

    private boolean recurringIndicator;

    private String validUntil;

    private int frequencyPerDay;

    private boolean combinedServiceIndicator;

    public static final class ConsentRequestBodyBuilder {

        public ConsentRequestBodyBuilder validUntil(LocalDate date) {
            validUntil = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return this;
        }
    }
}
