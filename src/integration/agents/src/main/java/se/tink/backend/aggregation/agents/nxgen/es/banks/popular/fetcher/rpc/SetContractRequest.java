package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities.BancoPopularContract;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SetContractRequest {
    private long contract;
    private int banco;
    private int oficina;
    private String ip;

    @JsonIgnore
    private SetContractRequest(BancoPopularContract contract, String ip) {
        this.contract = contract.getContractNumber();
        this.banco = contract.getBank();
        this.oficina = contract.getOffice();
        this.ip = ip;
    }

    @JsonIgnore
    public static SetContractRequest build(BancoPopularContract contract, String ip) {
        return new SetContractRequest(contract, ip);
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
