package se.tink.backend.aggregation.aggregationcontroller.v1.rpc;

public class UpdateIdentityDataRequest {
    private String name;
    private String ssn;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSsn() {
        return ssn;
    }

    public void setSsn(String ssn) {
        this.ssn = ssn;
    }
}
