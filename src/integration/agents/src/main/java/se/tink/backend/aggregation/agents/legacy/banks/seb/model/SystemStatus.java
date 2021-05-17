package se.tink.backend.aggregation.agents.legacy.banks.seb.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class SystemStatus {
    private int systemcode;
    private int errorcode;
    private String systemtitle;
    private String systemmessage;
}
