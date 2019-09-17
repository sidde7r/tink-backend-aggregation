package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkDetailsEntity self;
    private LinkDetailsEntity status;
    private LinkDetailsEntity scaStatus;
    private LinkDetailsEntity scaRedirect;

    public LinkDetailsEntity getScaRedirect() {
        return Preconditions.checkNotNull(scaRedirect);
    }
}
