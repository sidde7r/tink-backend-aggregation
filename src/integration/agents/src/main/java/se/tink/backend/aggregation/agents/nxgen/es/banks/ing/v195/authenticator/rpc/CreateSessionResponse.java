package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.rpc;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreateSessionResponse {

    private List<Integer> pinPositions;
    private List<Integer> pinPadNumbers;
    private String lastConnection;
    private String lastDeviceConnection;
    private Integer numFails;
    private Boolean registeredFingerprint;
    private Long contactId;
    private Long corpContactId;
    private List<String> pinpad;

    public List<Integer> getPinPositions() {
        return pinPositions;
    }

    public List<Integer> getPinPadNumbers() {
        return pinPadNumbers;
    }

    public String getLastConnection() {
        return lastConnection;
    }

    public String getLastDeviceConnection() {
        return lastDeviceConnection;
    }

    public Integer getNumFails() {
        return numFails;
    }

    public Boolean getRegisteredFingerprint() {
        return registeredFingerprint;
    }

    public Long getContactId() {
        return contactId;
    }

    public Long getCorpContactId() {
        return corpContactId;
    }

    public List<String> getPinpad() {
        return pinpad;
    }
}
