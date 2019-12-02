package se.tink.sa.agent.pt.ob.sibs.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TppRequestModel {

    private TppOsContainer android;
    private TppOsContainer ios;
    private String downloadTitle;
    private String downloadMessage;
    private String upgradeTitle;
    private String upgradeMessage;
}
