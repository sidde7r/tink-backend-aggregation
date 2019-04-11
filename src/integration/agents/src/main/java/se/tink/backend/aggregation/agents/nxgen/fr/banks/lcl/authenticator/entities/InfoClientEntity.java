package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InfoClientEntity {
    @JsonProperty("raisonSociale")
    private String socialReason;

    private String lcFavori;

    @JsonProperty("user_id_AT")
    private String userIdAt;

    @JsonProperty("nomPatronymique")
    private String surname;

    @JsonProperty("nomMarital")
    private String marriedName;

    @JsonProperty("compteFavori")
    private String accountFavorite;

    private String libelleCivilite;

    @JsonProperty("idBel")
    private String butRather;

    private String idPart;

    @JsonProperty("nomUsuel")
    private String usualName;

    @JsonProperty("premierPrenom")
    private String firstName;

    private String tagTurn;

    @JsonProperty("dateConnexion")
    private String dateConnection;

    @JsonProperty("agenceFavori")
    private String agencyFavorite;

    @JsonProperty("heureConnexion")
    private String timeLogin;

    public String getSocialReason() {
        return socialReason;
    }

    public String getLcFavori() {
        return lcFavori;
    }

    public String getUserIdAt() {
        return userIdAt;
    }

    public String getSurname() {
        return surname;
    }

    public String getMarriedName() {
        return marriedName;
    }

    public String getAccountFavorite() {
        return accountFavorite;
    }

    public String getLibelleCivilite() {
        return libelleCivilite;
    }

    public String getButRather() {
        return butRather;
    }

    public String getIdPart() {
        return idPart;
    }

    public String getUsualName() {
        return usualName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getTagTurn() {
        return tagTurn;
    }

    public String getDateConnection() {
        return dateConnection;
    }

    public String getAgencyFavorite() {
        return agencyFavorite;
    }

    public String getTimeLogin() {
        return timeLogin;
    }
}
