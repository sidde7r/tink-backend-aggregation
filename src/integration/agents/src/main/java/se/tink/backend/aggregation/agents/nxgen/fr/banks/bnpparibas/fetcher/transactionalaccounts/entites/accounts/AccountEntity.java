package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;

@JsonObject
public class AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

    @JsonProperty("codeEcoCompte")
    private int ecoAccountCode;
    private int codeNatureRelationCommercial;
    @JsonProperty("identifiantProduitCompte")
    private String accountProductId;
    @JsonProperty("identifiantSGIRattache")
    private String SgieIdentifier;
    @JsonProperty("indicAssuranceVie")
    private boolean indicativeLifeInsurance;
    @JsonProperty("indicCompteJoint")
    private boolean indicJointAccount;
    @JsonProperty("indicCompteTiers")
    private boolean indicativeThirdPartyAccount;
    @JsonProperty("libelleProduit")
    private String productLabel;
    @JsonProperty("nomAgence")
    private String agencyName;
    private String relationCommercial;
    @JsonProperty("typeCompte")
    private int accountType;
    @JsonProperty("codeMarque")
    private String brandCode;
    @JsonProperty("dateDernierMouvement")
    private String dateLastMovement;
    @JsonProperty("montantDecouvertAutorise")
    private int authorisedAmount;
    @JsonProperty("solde")
    private BalanceEntity balance;
    @JsonProperty("ibanChiffre")
    private String ibanKey;
    @JsonProperty("titulaire")
    private CustomerEntity holder;
    @JsonProperty("codeRegroupement")
    private int groupingCode;
    @JsonProperty("compteFavoris")
    private boolean favoritesAccount;
    @JsonProperty("seuilMeteoCritique")
    private double criticaWeatherThreshold;
    @JsonProperty("seuilMeteoVigilance")
    private double vigilanceWeatherThreshold;
    @JsonProperty("ribAnonymise")
    private String anonymisedRib;

    @JsonIgnore
    public TransactionalAccount toTinkAccount(RibListEntity accountDetails) {
        String iban = getIbanFromRib(accountDetails);

        return TransactionalAccount.builder(getTinkAccountType(), iban.toLowerCase(), balance.getTinkAmount())
                .setAccountNumber(iban)
                .setHolderName(new HolderName(getCustomerName()))
                .setName(productLabel)
                .putInTemporaryStorage(BnpParibasConstants.Storage.IBAN_KEY, ibanKey)
                .build();
    }

    /**
     * In France RIB is a statement of banking identity which is used for setting up payments and more. Our way of
     * getting the iban number if through the RIB details. For some reason it's returned as a list, but since the
     * details are fetched with the encrypted iban as a query parameter it's highly unlikely that other accounts
     * are in this list. That's why we just pick the first element in the list. This function throws an exception
     * if there are less or more than one element as we have to investigate that case.
     */
    @JsonIgnore
    private String getIbanFromRib(RibListEntity accountDetails) {
        List<RibEntity> ribList = accountDetails.getRibList();

        if (ribList.size() != 1) {
            throw new IllegalStateException(String.format("Expected details for one account, found %d.",
                    accountDetails.getRibList().size()));
        }

        return ribList.get(0).getAccountInfo().getIban();
    }

    @JsonIgnore
    private AccountTypes getTinkAccountType() {
        if (productLabel.equalsIgnoreCase(BnpParibasConstants.AccountType.CHECKING)) {
            return AccountTypes.CHECKING;
        }

        log.warn("{}: Unknown type: {}", BnpParibasConstants.Tags.UNKNOWN_ACCOUNT_TYPE, productLabel);
        return AccountTypes.OTHER;
    }

    @JsonIgnore
    public boolean isKnownAccountType() {
        return productLabel.equalsIgnoreCase(BnpParibasConstants.AccountType.CHECKING);
    }

    @JsonIgnore
    private String getCustomerName() {
        if (holder.getFirstName() == null || holder.getFullName() == null) {
            return null;
        }

        return holder.getFirstName() + " " + holder.getFullName();
    }

    public int getEcoAccountCode() {
        return ecoAccountCode;
    }

    public int getCodeNatureRelationCommercial() {
        return codeNatureRelationCommercial;
    }

    public String getAccountProductId() {
        return accountProductId;
    }

    public String getSgieIdentifier() {
        return SgieIdentifier;
    }

    public boolean isIndicativeLifeInsurance() {
        return indicativeLifeInsurance;
    }

    public boolean isIndicJointAccount() {
        return indicJointAccount;
    }

    public boolean isIndicativeThirdPartyAccount() {
        return indicativeThirdPartyAccount;
    }

    public String getProductLabel() {
        return productLabel;
    }

    public String getAgencyName() {
        return agencyName;
    }

    public String getRelationCommercial() {
        return relationCommercial;
    }

    public int getAccountType() {
        return accountType;
    }

    public String getBrandCode() {
        return brandCode;
    }

    public String getDateLastMovement() {
        return dateLastMovement;
    }

    public int getAuthorisedAmount() {
        return authorisedAmount;
    }

    public BalanceEntity getBalance() {
        return balance;
    }

    public String getIbanKey() {
        return ibanKey;
    }

    public CustomerEntity getHolder() {
        return holder;
    }

    public int getGroupingCode() {
        return groupingCode;
    }

    public boolean isFavoritesAccount() {
        return favoritesAccount;
    }

    public double getCriticaWeatherThreshold() {
        return criticaWeatherThreshold;
    }

    public double getVigilanceWeatherThreshold() {
        return vigilanceWeatherThreshold;
    }

    public String getAnonymisedRib() {
        return anonymisedRib;
    }

    @JsonObject
    public static class AccountInfoEntity {
        private String bic;
        private String iban;
        private String rib;

        public String getBic() {
            return bic;
        }

        public String getIban() {
            return iban;
        }

        public String getRib() {
            return rib;
        }
    }
}
