package se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.societegenerale.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {

    @JsonProperty("CODE_DEVISE_EUR")
    private String codeDeviseEur;
    @JsonProperty("idPrestaTech")
    private String technicalId;
    private String idPrestation;
    @JsonProperty("virementOK")
    private boolean virementok;
    @JsonProperty("codeProduit")
    private String productCode;
    @JsonProperty("codeSousProduit")
    private String codeUnderProduct;
    @JsonProperty("codeGroupeBancaire")
    private String bankGroupCode;
    @JsonProperty("libelle")
    private String label;
    @JsonProperty("numero")
    private String number;
    @JsonProperty("solde")
    private AmountEntity balance;
    @JsonProperty("dateSolde")
    private String dateBalance;
    private String codeType;
    @JsonProperty("detailVue")
    private String detailView;
    @JsonProperty("detailURL")
    private String detailurl;
    @JsonProperty("opeCarteMsg")
    private boolean opeMsg;
    @JsonProperty("cumulable")
    private boolean combinable;
    private String eligibilitePg;
    private boolean isEligibleRib;
    @JsonProperty("typePrestation")
    private String serviceType;
    @JsonProperty("numeroCarteTechnique")
    private String technicalCardId;
    @JsonProperty("isEligibleAutorisationDecouvert")
    private boolean isEligibleAuthorizationDecouvert;
    @JsonProperty("codeFamille")
    private String familyCode;
    @JsonProperty("libelleDescriptif")
    private String libelleDescription;

    public String getNumber() {
        return number;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public String getLabel() {
        return label;
    }

    public String getTechnicalId() {
        return technicalId;
    }

    public String getProductCode() {
        return productCode;
    }


    public String getTechnicalCardId() {
        return technicalCardId;
    }


}
