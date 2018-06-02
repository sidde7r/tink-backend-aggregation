package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PhoneNumberEntity {
    private String domestic;
    private String international;
    private String sms;

    public String getDomestic() {
        return domestic;
    }

    public String getInternational() {
        return international;
    }

    public String getSms() {
        return sms;
    }
}
