package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinkEntity {

    private String method;
    private String uri;

    public String getMethod() {
        return method;
    }

    public String getUri() {
        return uri;
    }

    public LinkEntity setMethod(String method) {
        this.method = method;
        return this;
    }

    public LinkEntity setUri(String uri) {
        this.uri = uri;
        return this;
    }

    @JsonIgnore
    public SwedbankBaseConstants.LinkMethod getMethodValue() {
        return SwedbankBaseConstants.LinkMethod.fromVerb(method);
    }

    @JsonIgnore
    public boolean isValid() {
        return method != null
                && getMethodValue() != SwedbankBaseConstants.LinkMethod.UNKNOWN
                && !Strings.isNullOrEmpty(uri);
    }
}
