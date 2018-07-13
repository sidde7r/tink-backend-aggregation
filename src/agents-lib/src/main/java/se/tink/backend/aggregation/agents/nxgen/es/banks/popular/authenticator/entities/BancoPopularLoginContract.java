package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/*
 * Sample data

    "nItnCont": 394133316,
    "nOrdIntc": 1,
    "cuenta": 3144,
    "detCuad": null,   // not parsed
    "oficina": 128,
    "foIntabr": 1,
    "formaIntervJurContrato": "TITULAR",
    "nomTitContrato": "KARL ALFRED ",
    "cCuadNorm": 0,
    "cOnline": 1,
    "codResOper": 4,
    "codigo": 767,
    "banco": 75,
    "desAlias": "",
    "estadoMigracion": 1
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class BancoPopularLoginContract {
    private long nItnCont;  // contract number
    private int nOrdIntc;
    private int cuenta;
    private int oficina;   // office
    private int foIntabr;
    private String formaIntervJurContrato;
    private String nomTitContrato;
    private int cCuadNorm;
    private int cOnline;
    private int codResOper;
    private int codigo;
    private int banco;   //bank
    private String desAlias;
    private int estadoMigracion;

    public long getnItnCont() {
        return nItnCont;
    }

    public void setnItnCont(int nItnCont) {
        this.nItnCont = nItnCont;
    }

    public int getnOrdIntc() {
        return nOrdIntc;
    }

    public void setnOrdIntc(int nOrdIntc) {
        this.nOrdIntc = nOrdIntc;
    }

    public int getCuenta() {
        return cuenta;
    }

    public void setCuenta(int cuenta) {
        this.cuenta = cuenta;
    }

    public int getOficina() {
        return oficina;
    }

    public void setOficina(int oficina) {
        this.oficina = oficina;
    }

    public int getFoIntabr() {
        return foIntabr;
    }

    public void setFoIntabr(int foIntabr) {
        this.foIntabr = foIntabr;
    }

    public String getFormaIntervJurContrato() {
        return formaIntervJurContrato;
    }

    public void setFormaIntervJurContrato(String formaIntervJurContrato) {
        this.formaIntervJurContrato = formaIntervJurContrato;
    }

    public String getNomTitContrato() {
        return nomTitContrato;
    }

    public void setNomTitContrato(String nomTitContrato) {
        this.nomTitContrato = nomTitContrato;
    }

    public int getcCuadNorm() {
        return cCuadNorm;
    }

    public void setcCuadNorm(int cCuadNorm) {
        this.cCuadNorm = cCuadNorm;
    }

    public int getcOnline() {
        return cOnline;
    }

    public void setcOnline(int cOnline) {
        this.cOnline = cOnline;
    }

    public int getCodResOper() {
        return codResOper;
    }

    public void setCodResOper(int codResOper) {
        this.codResOper = codResOper;
    }

    public int getCodigo() {
        return codigo;
    }

    public void setCodigo(int codigo) {
        this.codigo = codigo;
    }

    public int getBanco() {
        return banco;
    }

    public void setBanco(int banco) {
        this.banco = banco;
    }

    public String getDesAlias() {
        return desAlias;
    }

    public void setDesAlias(String desAlias) {
        this.desAlias = desAlias;
    }

    public int getEstadoMigracion() {
        return estadoMigracion;
    }

    public void setEstadoMigracion(int estadoMigracion) {
        this.estadoMigracion = estadoMigracion;
    }
}
