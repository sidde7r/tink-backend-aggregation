package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkDetailsEntity scaOAuth;
    private LinkDetailsEntity scaStatus;
    private LinkDetailsEntity self;
    private LinkDetailsEntity status;
}
