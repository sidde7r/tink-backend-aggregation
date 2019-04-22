package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Calendar;
import java.util.Date;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class ConsentBaseRequest {

    @JsonProperty protected boolean recurringIndicator;

    @JsonProperty
    @JsonFormat(pattern = "yyyy-MM-dd")
    protected Date validUntil;

    @JsonProperty protected int frequencyPerDay;
    @JsonProperty protected boolean combinedServiceIndicator;

    public ConsentBaseRequest() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH, 11);
        this.recurringIndicator = true;
        this.validUntil = now.getTime();
        this.frequencyPerDay = 4;
        this.combinedServiceIndicator = false;
    }

    public abstract String toData();

    public void setValidUntil(Date validUntil) {
        this.validUntil = validUntil;
    }
}
