package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
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
