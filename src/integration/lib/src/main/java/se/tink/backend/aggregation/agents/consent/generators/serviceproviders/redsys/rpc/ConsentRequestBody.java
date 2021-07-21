package se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.agents.consent.generators.serviceproviders.redsys.entities.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@Getter
public class ConsentRequestBody {

    @JsonProperty private AccessEntity access;

    @JsonProperty private boolean recurringIndicator;

    @JsonProperty private String validUntil;

    @JsonProperty private int frequencyPerDay;

    @JsonProperty private boolean combinedServiceIndicator;

    public static final class ConsentRequestBodyBuilder {

        private AccessEntity access;
        private boolean recurringIndicator;
        private String validUntil;
        private int frequencyPerDay;
        private boolean combinedServiceIndicator;

        private ConsentRequestBodyBuilder() {}

        public ConsentRequestBodyBuilder access(AccessEntity access) {
            this.access = access;
            return this;
        }

        public ConsentRequestBodyBuilder recurringIndicator(boolean recurringIndicator) {
            this.recurringIndicator = recurringIndicator;
            return this;
        }

        public ConsentRequestBodyBuilder validUntil(LocalDate date) {
            this.validUntil = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
            return this;
        }

        public ConsentRequestBodyBuilder frequencyPerDay(int frequencyPerDay) {
            this.frequencyPerDay = frequencyPerDay;
            return this;
        }

        public ConsentRequestBodyBuilder combinedServiceIndicator(
                boolean combinedServiceIndicator) {
            this.combinedServiceIndicator = combinedServiceIndicator;
            return this;
        }

        public ConsentRequestBody build() {
            return new ConsentRequestBody(
                    access,
                    recurringIndicator,
                    validUntil,
                    frequencyPerDay,
                    combinedServiceIndicator);
        }
    }

    public static ConsentRequestBodyBuilder builder() {
        return new ConsentRequestBodyBuilder();
    }

    @Override
    public String toString() {
        return "GetConsentRequest{"
                + "access="
                + access
                + ", recurringIndicator="
                + recurringIndicator
                + ", validUntil='"
                + validUntil
                + '\''
                + ", frequencyPerDay="
                + frequencyPerDay
                + ", combinedServiceIndicator="
                + combinedServiceIndicator
                + '}';
    }
}
