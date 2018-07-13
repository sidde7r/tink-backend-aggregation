package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularCustomBTD6 {
    private String hayMas;
    private Date fechasaldo;
    private double saldo;
    private String signosaldo;
    private Date fechasaldoval;
    private double saldoval;
    private String signosaldoval;
    private String codmonedacta;
    private String indicadorRecibos;
    private int noccurspartemv;
    private List<BancoPopularCustomEccas211SPartEMV> customEccas211SPARTEMV;

    public String getHayMas() {
        return hayMas;
    }

    public void setHayMas(String hayMas) {
        this.hayMas = hayMas;
    }

    public Date getFechasaldo() {
        return fechasaldo;
    }

    public void setFechasaldo(Date fechasaldo) {
        this.fechasaldo = fechasaldo;
    }

    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public String getSignosaldo() {
        return signosaldo;
    }

    public void setSignosaldo(String signosaldo) {
        this.signosaldo = signosaldo;
    }

    public Date getFechasaldoval() {
        return fechasaldoval;
    }

    public void setFechasaldoval(Date fechasaldoval) {
        this.fechasaldoval = fechasaldoval;
    }

    public double getSaldoval() {
        return saldoval;
    }

    public void setSaldoval(double saldoval) {
        this.saldoval = saldoval;
    }

    public String getSignosaldoval() {
        return signosaldoval;
    }

    public void setSignosaldoval(String signosaldoval) {
        this.signosaldoval = signosaldoval;
    }

    public String getCodmonedacta() {
        return codmonedacta;
    }

    public void setCodmonedacta(String codmonedacta) {
        this.codmonedacta = codmonedacta;
    }

    public String getIndicadorRecibos() {
        return indicadorRecibos;
    }

    public void setIndicadorRecibos(String indicadorRecibos) {
        this.indicadorRecibos = indicadorRecibos;
    }

    public int getNoccurspartemv() {
        return noccurspartemv;
    }

    public void setNoccurspartemv(int noccurspartemv) {
        this.noccurspartemv = noccurspartemv;
    }

    public List<BancoPopularCustomEccas211SPartEMV> getCustomEccas211SPARTEMV() {
        return customEccas211SPARTEMV;
    }

    public void setCustomEccas211SPARTEMV(
            List<BancoPopularCustomEccas211SPartEMV> customEccas211SPARTEMV) {
        this.customEccas211SPARTEMV = customEccas211SPARTEMV;
    }
}
