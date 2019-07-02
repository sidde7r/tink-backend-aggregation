package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.entities.AccessEntity;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ConsentRequest {

    private final AccessEntity access = new AccessEntity();
    @JsonProperty protected boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date validUntil;

    @JsonProperty protected int frequencyPerDay;
    @JsonProperty protected boolean combinedServiceIndicator;

    public ConsentRequest() {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }

    private ConsentRequest(
            final boolean recurringIndicator,
            final Date validUntil,
            final int frequencyPerDay,
            final boolean combinedServiceIndicator) {
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

    public AccessEntity getAccess() {
        return access;
    }

    public boolean isRecurringIndicator() {
        return recurringIndicator;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public boolean isCombinedServiceIndicator() {
        return combinedServiceIndicator;
    }

    public static class ConsentRequestBuilder {

        private boolean recurringIndicator;
        private Date validUntil;
        private int frequencyPerDay;
        private boolean combinedServiceIndicator;

        public ConsentRequestBuilder() {}

        public ConsentRequestBuilder recurringIndicator(final boolean recurringIndicator) {
            this.recurringIndicator = recurringIndicator;
            return this;
        }

        public ConsentRequestBuilder validUntil(final Date validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public ConsentRequestBuilder frequencyPerDay(final int frequencyPerDay) {
            this.frequencyPerDay = frequencyPerDay;
            return this;
        }

        public ConsentRequestBuilder combinedServiceIndicator(
                final boolean combinedServiceIndicator) {
            this.combinedServiceIndicator = combinedServiceIndicator;
            return this;
        }

        public ConsentRequest build() {
            return new ConsentRequest(
                    recurringIndicator, validUntil, frequencyPerDay, combinedServiceIndicator);
        }
    }
}
