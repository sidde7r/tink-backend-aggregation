package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class UnavailabilityEntity {

    private String type;
    private String header;
    private String message;
    private boolean down;

    public UnavailabilityEntity() {}

    public UnavailabilityEntity(boolean down) {
        this.down = down;
    }

    public void setDown(boolean down) {
        this.down = down;
    }

    public String getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public String getMessage() {
        return message;
    }

    public boolean isDown() {
        return down;
    }
}
