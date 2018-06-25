package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    private String href;
    private String method;
    private List<String> mediaTypes;

    public String getHref() {
        return href;
    }

    public String getMethod() {
        return method;
    }

    public List<String> getMediaTypes() {
        return mediaTypes;
    }
}
