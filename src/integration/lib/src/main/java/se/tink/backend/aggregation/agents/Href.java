package se.tink.backend.aggregation.agents;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Href {
    private String href;

    public String getHref() {
        return href;
    }

    @JsonIgnore
    public String getNullableHref() {
        return StringUtils.defaultString(href);
    }

    @JsonIgnore
    public String getHrefCheckNotNull() {
        return Preconditions.checkNotNull(href);
    }
}
