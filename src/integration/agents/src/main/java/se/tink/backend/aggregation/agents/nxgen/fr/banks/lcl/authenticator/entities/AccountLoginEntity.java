package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountLoginEntity {
    @JsonProperty("agence")
    private String agency;
    @JsonProperty("agenceGestion")
    private String managementAgency;
    @JsonProperty("cleRIB")
    private String cleRib;
    @JsonProperty("codeBanque")
    private String bankCode;
    @JsonProperty("codeFamilleCompte")
    private String familyCodeAccount;
    @JsonProperty("compte")
    private String accountNumber;
    @JsonProperty("encoursCB")
    private String encoursCb;
    @JsonProperty("lettreCle")
    private String cleLetter;
    @JsonProperty("natureCode")
    private String typeCode;
    @JsonProperty("natureLibelle")
    private String typeLabel;
    @JsonProperty("natureLibelleOriginal")
    private String originalTypeLabel;
    @JsonProperty("signeencours")
    private String outstandingSign;
    @JsonProperty("soldeComptable")
    private String accountingBalance;
    @JsonProperty("soldeComptableEnCentime")
    private String accountingBalanceInCentime;
    @JsonProperty("soldeComptableSigne")
    private String accountingBalanceSign;
    @JsonProperty("soldeEnValeur")
    private String valueBalance;
    @JsonProperty("soldeEnValeurEnCentimes")
    private String balanceInValueInCentimes;
    @JsonProperty("soldeEnValeurSigne")
    private String balanceInValueSign;
    private String topDevise;
    @JsonProperty("topEncoursCB")
    private String topOutstandingCb;
    @JsonProperty("supportEffectifCB")
    private boolean effectiveSupportCb;
    private int nbMvtG3P;
    private int soldeG3P;
    @JsonProperty("soldeDispoCentimeDevise")
    private String balanceDisposibleCentimeCurrency;
    @JsonProperty("soldeDispoCentimeDeviseSigne")
    private String balanceAvailableCentimeCurrencySign;
    @JsonProperty("soldeDisponible")
    private String availableBalance;
    @JsonProperty("soldeDisponibleSigne")
    private String balanceAvailableSign;
    @JsonProperty("soldeOperNonCompriseSigne")
    private String operBalanceNotIncludedSign;
    @JsonProperty("soldeOperNonComprise")
    private String balanceOperationsNotIncluded;

    public String getAgency() {
        return agency;
    }

    public String getManagementAgency() {
        return managementAgency;
    }

    public String getCleRib() {
        return cleRib;
    }

    public String getBankCode() {
        return bankCode;
    }

    public String getFamilyCodeAccount() {
        return familyCodeAccount;
    }

    public String getAccountNumber() {
        return accountNumber == null ? "" : accountNumber.trim();
    }

    public String getEncoursCb() {
        return encoursCb;
    }

    public String getCleLetter() {
        return cleLetter;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getTypeLabel() {
        return typeLabel == null ? "" : typeLabel.trim();
    }

    public String getOriginalTypeLabel() {
        return originalTypeLabel;
    }

    public String getOutstandingSign() {
        return outstandingSign;
    }

    public String getAccountingBalance() {
        return accountingBalance;
    }

    public String getAccountingBalanceInCentime() {
        return accountingBalanceInCentime;
    }

    public String getAccountingBalanceSign() {
        return accountingBalanceSign;
    }

    public String getValueBalance() {
        return valueBalance;
    }

    public String getBalanceInValueInCentimes() {
        return balanceInValueInCentimes;
    }

    public String getBalanceInValueSign() {
        return balanceInValueSign;
    }

    public String getTopDevise() {
        return topDevise;
    }

    public String getTopOutstandingCb() {
        return topOutstandingCb;
    }

    public boolean isEffectiveSupportCb() {
        return effectiveSupportCb;
    }

    public int getNbMvtG3P() {
        return nbMvtG3P;
    }

    public int getSoldeG3P() {
        return soldeG3P;
    }

    public String getBalanceDisposibleCentimeCurrency() {
        return balanceDisposibleCentimeCurrency;
    }

    public String getBalanceAvailableCentimeCurrencySign() {
        return balanceAvailableCentimeCurrencySign;
    }

    public String getBalanceAvailable() {
        return availableBalance;
    }

    public String getBalanceAvailableSign() {
        return balanceAvailableSign;
    }

    public String getOperBalanceNotIncludedSign() {
        return operBalanceNotIncludedSign;
    }

    public String getBalanceOperationsNotIncluded() {
        return balanceOperationsNotIncluded;
    }
}
