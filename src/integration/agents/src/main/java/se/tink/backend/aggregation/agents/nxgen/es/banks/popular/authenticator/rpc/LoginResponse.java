package se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.popular.authenticator.entities.BancoPopularLoginContract;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginResponse {
    private List<BancoPopularLoginContract> loginContractOut;
    @JsonProperty("numIntPersona")
    private String personNumber;
    @JsonProperty("nombre")
    private String firstName;
    @JsonProperty("tratamiento")
    private int treatment;
    private String digDocum;
    @JsonProperty("datosBasicos")
    private String basicData;
    @JsonProperty("sitFirma")
    private int sitCompany;
    @JsonProperty("rentasAltas")
    private String highRents;
    @JsonProperty("inHayMas")
    private String inHasMore;
    @JsonProperty("codConducta")
    private String codeConduct;
    private String otpTelef;
    private String dsExc;
    private String dsOtp;
    private String glContprov;
    @JsonProperty("delegado")
    private String delegate;
    private String sitBenFre;
    @JsonProperty("tipoFirma")
    private String signatureType;
    private int nDiasMigrs;
    private int tPerfilclie;

    public List<BancoPopularLoginContract> getLoginContractOut() {
        return loginContractOut;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public int getTreatment() {
        return treatment;
    }

    public String getDigDocum() {
        return digDocum;
    }

    public String getBasicData() {
        return basicData;
    }

    public int getSitCompany() {
        return sitCompany;
    }

    public String getHighRents() {
        return highRents;
    }

    public String getInHasMore() {
        return inHasMore;
    }

    public String getCodeConduct() {
        return codeConduct;
    }

    public String getOtpTelef() {
        return otpTelef;
    }

    public String getDsExc() {
        return dsExc;
    }

    public String getDsOtp() {
        return dsOtp;
    }

    public String getGlContprov() {
        return glContprov;
    }

    public String getDelegate() {
        return delegate;
    }

    public String getSitBenFre() {
        return sitBenFre;
    }

    public String getSignatureType() {
        return signatureType;
    }

    public int getnDiasMigrs() {
        return nDiasMigrs;
    }

    public int gettPerfilclie() {
        return tPerfilclie;
    }
}
