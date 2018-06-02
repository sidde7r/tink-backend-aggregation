package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {
    public enum LinkMethod {
        GET, POST, PUT, OPTIONS, DELETE
    }

    private LinkMethod method;
    private String uri;

    public LinkMethod getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public boolean isValid() {
        return method != null && !Strings.isNullOrEmpty(uri);
    }
}
