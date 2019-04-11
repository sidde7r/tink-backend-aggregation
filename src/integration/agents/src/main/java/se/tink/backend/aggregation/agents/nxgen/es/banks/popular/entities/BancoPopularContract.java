package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities.BancoPopularLoginContract;
import se.tink.backend.aggregation.annotations.JsonObject;

/*
 * Class for storing login contracts in storage
 */
@JsonObject
public class BancoPopularContract {
    @JsonProperty("nItnCont")
    private long contractNumber;

    @JsonProperty("oficina")
    private int office;

    @JsonProperty("banco")
    private int bank;

    public BancoPopularContract() {}

    private BancoPopularContract(BancoPopularLoginContract loginContract) {
        this.contractNumber = loginContract.getContractNumber();
        this.office = loginContract.getOffice();
        this.bank = loginContract.getBank();
    }

    public static BancoPopularContract build(BancoPopularLoginContract loginContract) {
        return new BancoPopularContract(loginContract);
    }

    public long getContractNumber() {
        return contractNumber;
    }

    public int getOffice() {
        return office;
    }

    public int getBank() {
        return bank;
    }
}
