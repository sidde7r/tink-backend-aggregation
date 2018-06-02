package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.rpc;

public class SetContractRequest {
    private long contract;
    private int banco;
    private int oficina;
    private String ip;

    public long getContract() {
        return contract;
    }

    public SetContractRequest setContract(long contract) {
        this.contract = contract;
        return this;
    }

    public int getBanco() {
        return banco;
    }

    public SetContractRequest setBanco(int banco) {
        this.banco = banco;
        return this;
    }

    public int getOficina() {
        return oficina;
    }

    public SetContractRequest setOficina(int oficina) {
        this.oficina = oficina;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public SetContractRequest setIp(String ip) {
        this.ip = ip;
        return this;
    }
}
