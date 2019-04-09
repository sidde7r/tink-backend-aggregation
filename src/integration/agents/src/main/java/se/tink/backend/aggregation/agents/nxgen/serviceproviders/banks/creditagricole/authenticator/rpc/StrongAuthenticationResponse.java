package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.rpc.DefaultResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StrongAuthenticationResponse extends DefaultResponse {
    private String partnerId;
    private String slToken;
    private String llToken;
    private String idStructure;
    private String idPartenaire;
    private String userid;

    public String getPartnerId() {
        return partnerId;
    }

    public String getSlToken() {
        return slToken;
    }

    public String getLlToken() {
        return llToken;
    }

    public String getIdStructure() {
        return idStructure;
    }

    public String getIdPartenaire() {
        return idPartenaire;
    }

    public String getUserid() {
        return userid;
    }
}
