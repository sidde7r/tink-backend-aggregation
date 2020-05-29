package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticateResponse extends DefaultResponse {

    private String userId;
    private String perimeterId;
    private String slToken;
    private String llToken;
    private String idStructure;
    private String idPartenaire;
    private String idPartenaireCo;
    private String idPivot;
    private String serverRefDate;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPerimeterId() {
        return perimeterId;
    }

    public void setPerimeterId(String perimeterId) {
        this.perimeterId = perimeterId;
    }

    public String getSlToken() {
        return slToken;
    }

    public void setSlToken(String slToken) {
        this.slToken = slToken;
    }

    public String getLlToken() {
        return llToken;
    }

    public void setLlToken(String llToken) {
        this.llToken = llToken;
    }

    public String getIdStructure() {
        return idStructure;
    }

    public void setIdStructure(String idStructure) {
        this.idStructure = idStructure;
    }

    public String getIdPartenaire() {
        return idPartenaire;
    }

    public void setIdPartenaire(String idPartenaire) {
        this.idPartenaire = idPartenaire;
    }

    public String getIdPartenaireCo() {
        return idPartenaireCo;
    }

    public void setIdPartenaireCo(String idPartenaireCo) {
        this.idPartenaireCo = idPartenaireCo;
    }

    public String getIdPivot() {
        return idPivot;
    }

    public void setIdPivot(String idPivot) {
        this.idPivot = idPivot;
    }

    public String getServerRefDate() {
        return serverRefDate;
    }

    public void setServerRefDate(String serverRefDate) {
        this.serverRefDate = serverRefDate;
    }
}
