package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.authenticator.entity;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaRedirect {

    private String href;

    public String getHref() {
        return Preconditions.checkNotNull(Strings.emptyToNull(href));
    }
}
