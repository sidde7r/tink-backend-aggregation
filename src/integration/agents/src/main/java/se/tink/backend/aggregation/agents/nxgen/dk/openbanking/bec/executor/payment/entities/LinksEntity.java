package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkDetailsEntity self;
    private LinkDetailsEntity status;
}
