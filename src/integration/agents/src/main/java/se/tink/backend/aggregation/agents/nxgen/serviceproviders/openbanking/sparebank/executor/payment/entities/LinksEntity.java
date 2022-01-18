package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class LinksEntity {
    private LinkEntity self;
    private LinkEntity status;
    private LinkEntity startAuthorisation;
    private LinkEntity scaRedirect;
}
