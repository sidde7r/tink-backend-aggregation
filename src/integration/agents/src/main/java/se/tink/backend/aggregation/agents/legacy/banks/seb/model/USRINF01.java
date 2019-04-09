package se.tink.backend.aggregation.agents.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class USRINF01 {
    @JsonProperty("USER_NAME")
    public String USER_NAME;

    @JsonProperty("SEB_KUND_NR")
    public String SEB_KUND_NR;

    @JsonProperty("BUNT_ANTAL_OSIGNERAD")
    public String BUNT_ANTAL_OSIGNERAD;

    @JsonProperty("EFACTURA_ANTAL_OSIGN")
    public String EFACTURA_ANTAL_OSIGN;

    @JsonProperty("IMS_SHORT_USERID")
    public String IMS_SHORT_USERID;

    @JsonProperty("LOGON_COMPLETED")
    public String LOGON_COMPLETED;

    @JsonProperty("STATUS")
    public String STATUS;
}
