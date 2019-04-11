package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountDetailsEntity {
    private int id;

    @JsonProperty("agence")
    private String agency;

    @JsonProperty("agenceGestion")
    private String managementAgency;

    @JsonProperty("compte")
    private String accountNumber;

    @JsonProperty("compteSurOnzeCaractere")
    private String accountOnElevenCharacters;

    @JsonProperty("lettreClef")
    private String clefLetter;

    private String natureCode;

    @JsonProperty("natureLibelle")
    private String typeLabel;

    @JsonProperty("titulaireIdentifiant")
    private String holderId;

    @JsonProperty("titulaireTypePersonne")
    private String holderPersonType;

    @JsonProperty("titulaireCivilite")
    private String holderName;

    @JsonProperty("titulaireNomPatronimique")
    private String holderPatronymicName;

    @JsonProperty("titulairePrenom")
    private String holderFirstName;

    @JsonProperty("titulaireNomMarital")
    private String holderLastName;

    @JsonProperty("titulaireNomUsuel")
    private String holderUsualName;

    @JsonProperty("titulaireRaisonSociale")
    private String holderCorporateName;

    @JsonProperty("titulaireIntitule")
    private String holderTitle;

    @JsonProperty("agenceGestionLibelle")
    private String managementAgencyLabel;

    @JsonProperty("compteBIC")
    private String accountBic;

    @JsonProperty("nbRibImpression")
    private int ribPrinting;

    @JsonProperty("ribGuichet")
    private String ribCounter;

    private String ribClef;

    @JsonProperty("ribBanque")
    private String ribBank;

    @JsonProperty("cleIBAN")
    private String iban;

    private String codePaysIBAN;
    private String topICI;
    private String urlPdf;

    public int getId() {
        return id;
    }

    public String getAgency() {
        return agency;
    }

    public String getManagementAgency() {
        return managementAgency;
    }

    public String getAccountNumber() {
        return accountNumber == null ? "" : accountNumber.trim();
    }

    public String getAccountOnElevenCharacters() {
        return accountOnElevenCharacters;
    }

    public String getClefLetter() {
        return clefLetter == null ? "" : clefLetter.trim();
    }

    public String getNatureCode() {
        return natureCode;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getHolderId() {
        return holderId;
    }

    public String getHolderPersonType() {
        return holderPersonType;
    }

    public String getHolderName() {
        return holderName == null ? "" : holderName.trim();
    }

    public String getHolderPatronymicName() {
        return holderPatronymicName;
    }

    public String getHolderFirstName() {
        return holderFirstName;
    }

    public String getHolderLastName() {
        return holderLastName;
    }

    public String getHolderUsualName() {
        return holderUsualName;
    }

    public String getHolderCorporateName() {
        return holderCorporateName;
    }

    public String getHolderTitle() {
        return holderTitle;
    }

    public String getManagementAgencyLabel() {
        return managementAgencyLabel;
    }

    public String getAccountBic() {
        return accountBic;
    }

    public int getRibPrinting() {
        return ribPrinting;
    }

    public String getRibCounter() {
        return ribCounter;
    }

    public String getRibClef() {
        return ribClef;
    }

    public String getRibBank() {
        return ribBank;
    }

    public String getIban() {
        return iban;
    }

    public String getCodePaysIBAN() {
        return codePaysIBAN;
    }

    public String getTopICI() {
        return topICI;
    }

    public String getUrlPdf() {
        return urlPdf;
    }
}
