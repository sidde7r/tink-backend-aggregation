package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.entites;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginDataEntity {
    @JsonProperty("statut")
    private String status;
    private String message;
    @JsonProperty("identifiant")
    private String loginId;
    private String canal;
    private String segment;
    private String communaute;
    @JsonProperty("nom")
    private String name;
    @JsonProperty("dateDerniereConnexion")
    private String dateLastLogin;
    @JsonProperty("IDSeconde")
    private String secondId;
    @JsonProperty("identifiantCommunaute")
    private String communityId;
    @JsonProperty("codeEnseigne")
    private String codeSign;
    @JsonProperty("libelleEnseigne")
    private String wordingSign;
    @JsonProperty("datePriseEffetCommunauteBD")
    private String dateTakeEffectCommunitybd;
    @JsonProperty("iKpiPersonnePhysique")
    private String iKpiPhysicalPerson;
    @JsonProperty("iKpiPersonne")
    private String iKpiPerson;
    private boolean eligibleAuthentForte;
    @JsonProperty("eligibleAuthentForteSC")
    private boolean eligibleAuthentFortesc;
    private boolean indicPresentationKyc;
    @JsonProperty("codeMotifOrigineClient")
    private String codeOriginOriginCustomer;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getCanal() {
        return canal;
    }

    public String getSegment() {
        return segment;
    }

    public String getCommunaute() {
        return communaute;
    }

    public String getName() {
        return name;
    }

    public String getDateLastLogin() {
        return dateLastLogin;
    }

    public String getSecondId() {
        return secondId;
    }

    public String getCommunityId() {
        return communityId;
    }

    public String getCodeSign() {
        return codeSign;
    }

    public String getWordingSign() {
        return wordingSign;
    }

    public String getDateTakeEffectCommunitybd() {
        return dateTakeEffectCommunitybd;
    }

    public String getiKpiPhysicalPerson() {
        return iKpiPhysicalPerson;
    }

    public String getiKpiPerson() {
        return iKpiPerson;
    }

    public boolean isEligibleAuthentForte() {
        return eligibleAuthentForte;
    }

    public boolean isEligibleAuthentFortesc() {
        return eligibleAuthentFortesc;
    }

    public boolean isIndicPresentationKyc() {
        return indicPresentationKyc;
    }

    public String getCodeOriginOriginCustomer() {
        return codeOriginOriginCustomer;
    }
}
