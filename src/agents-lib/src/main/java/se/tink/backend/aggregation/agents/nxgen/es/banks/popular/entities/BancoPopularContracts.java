package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.ArrayList;
import java.util.List;

/*
 * Class for storing login contracts in storage
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularContracts {
    private List<BancoPopularContract> contracts;

    @JsonIgnore
    public void addLoginContract(BancoPopularContract loginContract) {
        if (contracts == null) {
            contracts = new ArrayList<>();
        }

        contracts.add(loginContract);
    }

    @JsonIgnore
    public BancoPopularContract getFirstContract() {
        return contracts.get(0);
    }

    public List<BancoPopularContract> getContracts() {
        return contracts;
    }

    public BancoPopularContracts setContracts(
            List<BancoPopularContract> contracts) {
        this.contracts = contracts;
        return this;
    }
}
