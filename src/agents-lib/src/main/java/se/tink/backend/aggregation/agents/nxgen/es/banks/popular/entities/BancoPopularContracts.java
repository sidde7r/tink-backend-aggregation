package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

/*
 * Class for storing login contracts in storage
 */
@JsonObject
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
