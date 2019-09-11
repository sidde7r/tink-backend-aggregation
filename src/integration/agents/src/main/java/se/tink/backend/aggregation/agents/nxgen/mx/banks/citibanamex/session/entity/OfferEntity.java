package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.session.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OfferEntity {
    private String campaignID;
    private String configID;
    private String description;
    private String serial1;
    private String serial2;
    private String status;
    private String title;
    private String waveID;
}
