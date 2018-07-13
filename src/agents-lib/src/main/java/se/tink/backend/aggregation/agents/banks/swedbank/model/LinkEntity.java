package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.MoreObjects;
import se.tink.backend.aggregation.utils.TrimmingStringDeserializer;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LinkEntity {
    private String method;
    private String uri;

    public String getUri() {
        return uri;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMethod() {
        return method;
    }

    @JsonDeserialize(using = TrimmingStringDeserializer.class)
    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("method", method)
                .add("uri", uri)
                .toString();
    }
}
