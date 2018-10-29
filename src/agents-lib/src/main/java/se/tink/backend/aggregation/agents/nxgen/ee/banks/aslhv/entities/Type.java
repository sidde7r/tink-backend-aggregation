package se.tink.backend.aggregation.agents.nxgen.ee.banks.aslhv.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Type {

    @JsonProperty("subfamily")
    private Object subfamily;

    @JsonProperty("domain")
    private Object domain;

    @JsonProperty("family")
    private Object family;

    public Object getSubfamily() {
        return subfamily;
    }

    public Object getDomain() {
        return domain;
    }

    public Object getFamily() {
        return family;
    }
}
