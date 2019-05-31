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

    public String toData() {
        return SerializationUtils.serializeToString(this);
    }

    public AccessEntity getAccess() {
        return access;
    }

    public boolean isRecurringIndicator() {
        return recurringIndicator;
    }

    public void setRecurringIndicator(final boolean recurringIndicator) {
        this.recurringIndicator = recurringIndicator;
    }

    public Date getValidUntil() {
        return validUntil;
    }

    public void setValidUntil(final Date validUntil) {
        this.validUntil = validUntil;
    }

    public int getFrequencyPerDay() {
        return frequencyPerDay;
    }

    public void setFrequencyPerDay(final int frequencyPerDay) {
        this.frequencyPerDay = frequencyPerDay;
    }

    public boolean isCombinedServiceIndicator() {
        return combinedServiceIndicator;
    }

    public void setCombinedServiceIndicator(final boolean combinedServiceIndicator) {
        this.combinedServiceIndicator = combinedServiceIndicator;
    }
}
