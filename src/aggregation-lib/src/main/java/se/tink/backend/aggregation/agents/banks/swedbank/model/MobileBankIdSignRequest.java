package se.tink.backend.aggregation.agents.banks.swedbank.model;

public class MobileBankIdSignRequest {
    private boolean initiate;

    public boolean isInitiate() {
        return initiate;
    }

    public void setInitiate(boolean initiate) {
        this.initiate = initiate;
    }
}
