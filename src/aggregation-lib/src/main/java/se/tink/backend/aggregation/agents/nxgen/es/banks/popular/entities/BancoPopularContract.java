package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.entities;

/*
 * Class for storing login contracts in storage
 */
public class BancoPopularContract {
    private long nItnCont;
    private int oficina;
    private int banco;

    public long getnItnCont() {
        return nItnCont;
    }

    public BancoPopularContract setnItnCont(long nItnCont) {
        this.nItnCont = nItnCont;
        return this;
    }

    public int getOficina() {
        return oficina;
    }

    public BancoPopularContract setOficina(int oficina) {
        this.oficina = oficina;
        return this;
    }

    public int getBanco() {
        return banco;
    }

    public BancoPopularContract setBanco(int banco) {
        this.banco = banco;
        return this;
    }
}
