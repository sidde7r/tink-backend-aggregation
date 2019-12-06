package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {
    private LinkEntity scaRedirect;
    private LinkEntity self;
}
