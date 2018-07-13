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

    public SwedbankBaseConstants.LinkMethod getMethodValue() {
        return SwedbankBaseConstants.LinkMethod.fromVerb(method);
    }

    public String getUri() {
        return uri;
    }

    @JsonIgnore
    public boolean isValid() {
        return method != null
                && getMethodValue() !=  SwedbankBaseConstants.LinkMethod.UNKNOWN
                && !Strings.isNullOrEmpty(uri);
    }
}
