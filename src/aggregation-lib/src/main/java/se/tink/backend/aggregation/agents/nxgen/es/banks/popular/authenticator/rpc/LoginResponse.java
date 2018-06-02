package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities.BancoPopularLoginContract;

/*
 * Sample data

    "loginContractOut": [{}],  // see entities.BancoPopularLoginContract
    "numIntPersona": "033352780",
    "nombre": "KARL ALFRED ",
    "tratamiento": 1,
    "digDocum": "S",
    "datosBasicos": "S",
    "sitFirma": 5,
    "rentasAltas": "N",
    "inHayMas": "N",
    "codConducta": "0",
    "otpTelef": "2",
    "dsExc": "0",
    "dsOtp": "0",
    "glContprov": "0",
    "delegado": "",
    "sitBenFre": "I",
    "tipoFirma": "N"
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginResponse {
    private List<BancoPopularLoginContract> loginContractOut;
    private String numIntPersona;
    private String nombre;
    private int tratamiento;
    private String digDocum;
    private String datosBasicos;
    private int sitFirma;
    private String rentasAltas;
    private String inHayMas;
    private String codConducta;
    private String otpTelef;
    private String dsExc;
    private String dsOtp;
    private String glContprov;
    private String delegado;
    private String sitBenFre;
    private String tipoFirma;

    public List<BancoPopularLoginContract> getLoginContractOut() {
        return loginContractOut;
    }

    public void setLoginContractOut(
            List<BancoPopularLoginContract> loginContractOut) {
        this.loginContractOut = loginContractOut;
    }

    public String getNumIntPersona() {
        return numIntPersona;
    }

    public void setNumIntPersona(String numIntPersona) {
        this.numIntPersona = numIntPersona;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getTratamiento() {
        return tratamiento;
    }

    public void setTratamiento(int tratamiento) {
        this.tratamiento = tratamiento;
    }

    public String getDigDocum() {
        return digDocum;
    }

    public void setDigDocum(String digDocum) {
        this.digDocum = digDocum;
    }

    public String getDatosBasicos() {
        return datosBasicos;
    }

    public void setDatosBasicos(String datosBasicos) {
        this.datosBasicos = datosBasicos;
    }

    public int getSitFirma() {
        return sitFirma;
    }

    public void setSitFirma(int sitFirma) {
        this.sitFirma = sitFirma;
    }

    public String getRentasAltas() {
        return rentasAltas;
    }

    public void setRentasAltas(String rentasAltas) {
        this.rentasAltas = rentasAltas;
    }

    public String getInHayMas() {
        return inHayMas;
    }

    public void setInHayMas(String inHayMas) {
        this.inHayMas = inHayMas;
    }

    public String getCodConducta() {
        return codConducta;
    }

    public void setCodConducta(String codConducta) {
        this.codConducta = codConducta;
    }

    public String getOtpTelef() {
        return otpTelef;
    }

    public void setOtpTelef(String otpTelef) {
        this.otpTelef = otpTelef;
    }

    public String getDsExc() {
        return dsExc;
    }

    public void setDsExc(String dsExc) {
        this.dsExc = dsExc;
    }

    public String getDsOtp() {
        return dsOtp;
    }

    public void setDsOtp(String dsOtp) {
        this.dsOtp = dsOtp;
    }

    public String getGlContprov() {
        return glContprov;
    }

    public void setGlContprov(String glContprov) {
        this.glContprov = glContprov;
    }

    public String getDelegado() {
        return delegado;
    }

    public void setDelegado(String delegado) {
        this.delegado = delegado;
    }

    public String getSitBenFre() {
        return sitBenFre;
    }

    public void setSitBenFre(String sitBenFre) {
        this.sitBenFre = sitBenFre;
    }

    public String getTipoFirma() {
        return tipoFirma;
    }

    public void setTipoFirma(String tipoFirma) {
        this.tipoFirma = tipoFirma;
    }
}
