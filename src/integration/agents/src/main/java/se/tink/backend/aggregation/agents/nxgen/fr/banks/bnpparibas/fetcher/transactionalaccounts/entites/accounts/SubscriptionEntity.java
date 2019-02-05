package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SubscriptionEntity {
    @JsonProperty("codeBanque")
    private String bankCode;
    @JsonProperty("codeBanqueDomiciliation")
    private String codeBankDomiciliation;
    @JsonProperty("codeProduitAbonnement")
    private String productCodeSubscription;
    private String codeSegmentClientele;
    @JsonProperty("codeSegmentMarche")
    private String marketCodeSegment;
    @JsonProperty("codeMotifOrigineClient")
    private String originalCustomerCodePattern;
    @JsonProperty("nombreMessageBMMNonLus")
    private int numberBmmMessagesUnread;
    @JsonProperty("nombreMessageBilatNonLus")
    private int numberBilatMessagesUnread;
    private int typeResident;
    @JsonProperty("typeUtilisateur")
    private int userType;

    public String getBankCode() {
        return bankCode;
    }

    public String getCodeBankDomiciliation() {
        return codeBankDomiciliation;
    }

    public String getProductCodeSubscription() {
        return productCodeSubscription;
    }

    public String getCodeSegmentClientele() {
        return codeSegmentClientele;
    }

    public String getMarketCodeSegment() {
        return marketCodeSegment;
    }

    public String getOriginalCustomerCodePattern() {
        return originalCustomerCodePattern;
    }

    public int getNumberBmmMessagesUnread() {
        return numberBmmMessagesUnread;
    }

    public int getNumberBilatMessagesUnread() {
        return numberBilatMessagesUnread;
    }

    public int getTypeResident() {
        return typeResident;
    }

    public int getUserType() {
        return userType;
    }
}
