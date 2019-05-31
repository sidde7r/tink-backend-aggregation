package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.entity.AccessEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class ConsentBaseRequest {

    private final AccessEntity access = new AccessEntity();
    @JsonProperty protected boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date validUntil;

    @JsonProperty protected int frequencyPerDay;
    @JsonProperty protected boolean combinedServiceIndicator;

    public ConsentBaseRequest() {
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }

    private ConsentBaseRequest(
            final boolean recurringIndicator,
            final Date validUntil,
            final int frequencyPerDay,
            final boolean combinedServiceIndicator) {
        this.recurringIndicator = recurringIndicator;
        this.validUntil = validUntil;
        this.frequencyPerDay = frequencyPerDay;
        this.combinedServiceIndicator = combinedServiceIndicator;
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

    public static class ConsentBaseRequestBuilder {

        private boolean recurringIndicator;
        private Date validUntil;
        private int frequencyPerDay;
        private boolean combinedServiceIndicator;

        ConsentBaseRequestBuilder() {}

        public ConsentBaseRequestBuilder recurringIndicator(final boolean recurringIndicator) {
            this.recurringIndicator = recurringIndicator;
            return this;
        }

        public ConsentBaseRequestBuilder validUntil(final Date validUntil) {
            this.validUntil = validUntil;
            return this;
        }

        public ConsentBaseRequestBuilder frequencyPerDay(final int frequencyPerDay) {
            this.frequencyPerDay = frequencyPerDay;
            return this;
        }

        public ConsentBaseRequestBuilder combinedServiceIndicator(
                final boolean combinedServiceIndicator) {
            this.combinedServiceIndicator = combinedServiceIndicator;
            return this;
        }

        public ConsentBaseRequest build() {
            return new ConsentBaseRequest(
                    recurringIndicator, validUntil, frequencyPerDay, combinedServiceIndicator);
        }
    }
}
