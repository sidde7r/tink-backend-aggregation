package se.tink.backend.aggregation.agents.nxgen.mx.banks.citibanamex.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CellPhoneInfoEntity {
    private String carrier;
    private String cellphone;
    private String certChannel;
    private String certLevel;
}
