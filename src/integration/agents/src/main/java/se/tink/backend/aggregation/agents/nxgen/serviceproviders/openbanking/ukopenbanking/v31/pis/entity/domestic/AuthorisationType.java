package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.pis.entity.domestic;

public enum AuthorisationType {
    ANY("Any"),
    SINGLE("Single");

    private String value;

    AuthorisationType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
