package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class ConsentRequest {

    @JsonFormat(pattern = "yyyy-MM-dd")
    protected final Date validUntil;

    private final AccessEntity access = new AccessEntity();
    private final boolean recurringIndicator;
    private final int frequencyPerDay;
    private final boolean combinedServiceIndicator;

    private ConsentRequest(
            boolean recurringIndicator,
            Date validUntil,
            int frequencyPerDay,
            boolean combinedServiceIndicator) {
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
    }

    public static ConsentRequestBuilder builder() {
        return new ConsentRequestBuilder();
    }

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public static class ConsentRequestBuilder {

        private boolean recurringIndicator;
        private Date validUntil;
        private int frequencyPerDay;
        private boolean combinedServiceIndicator;

        ConsentRequestBuilder() {}

        public ConsentRequestBuilder recurringIndicator(boolean recurringIndicator) {
            this.recurringIndicator = recurringIndicator;
            return this;
        }

        public ConsentRequestBuilder validUntil(Date validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public ConsentRequestBuilder frequencyPerDay(int frequencyPerDay) {
            this.frequencyPerDay = frequencyPerDay;
            return this;
        }

        public ConsentRequestBuilder combinedServiceIndicator(boolean combinedServiceIndicator) {
            this.combinedServiceIndicator = combinedServiceIndicator;
            return this;
        }

        public ConsentRequest build() {
            return new ConsentRequest(
                    recurringIndicator, validUntil, frequencyPerDay, combinedServiceIndicator);
        }

        public String toString() {
            return "ConsentRequest.ConsentRequestBuilder(recurringIndicator="
                    + this.recurringIndicator
                    + ", validUntil="
                    + this.validUntil
                    + ", frequencyPerDay="
                    + this.frequencyPerDay
                    + ", combinedServiceIndicator="
                    + this.combinedServiceIndicator
                    + ")";
        }
    }
}
