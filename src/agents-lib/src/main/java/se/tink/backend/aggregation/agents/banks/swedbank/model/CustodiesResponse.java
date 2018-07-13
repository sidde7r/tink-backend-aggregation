package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CustodiesResponse {
    private List<CustodyEntity> custodies;

    public List<CustodyEntity> getCustodies() {
        return custodies;
    }

    public void setCustodies(List<CustodyEntity> custodies) {
        this.custodies = custodies;
    }
}
