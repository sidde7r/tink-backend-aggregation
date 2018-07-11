package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.authenticator.rpc;

import java.util.List;

public class LoginPinPositions {

    private List<Integer> pinPositions;

    public LoginPinPositions() {
        super();
    }

    public LoginPinPositions(List<Integer> pinPositions) {
        this.pinPositions = pinPositions;
    }

    public List<Integer> getPinPositions() {
        return pinPositions;
    }

    public void setPinPositions(List<Integer> pinPositions) {
        this.pinPositions = pinPositions;
    }
}
