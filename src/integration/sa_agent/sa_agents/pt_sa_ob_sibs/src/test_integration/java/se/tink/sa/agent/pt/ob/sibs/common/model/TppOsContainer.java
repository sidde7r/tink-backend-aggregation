package se.tink.sa.agent.pt.ob.sibs.common.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TppOsContainer {

    private String packageName;
    private String requiredMinimumVersion;
    private String intent;

    private String appStoreUrl;
    private String scheme;
    private String deepLinkUrl;
}
