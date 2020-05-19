package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.entites.accounts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.BnpParibasConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {
    @JsonProperty("plafond")
    private double ceiling;

    @JsonProperty("isAutreCompte")
    private int anotherAccountIs;

    private String value;

    @JsonProperty("actif")
    private int active;

    @JsonProperty("key")
    private String ibanKey;

    @JsonProperty("libellePersoProduit")
    private String wordingCharacterProduct;

    @JsonProperty("isCompteDeFacturation")
    private int fromBillingAccountIs;

    @JsonProperty("libelleProduit")
    private String productLabel;

    @JsonProperty("libelleCourtProduit")
    private String shortNameProduct;

    @JsonProperty("dateSoldeDispo")
    private String dateBalanceAvailable;

    @JsonProperty("dateDernierMvt")
    private String lastTimeMvt;

    @JsonProperty("soldeDispo")
    private double availableBalance;

    @JsonProperty("soldeAVenir")
    private double hipComix;

    @JsonProperty("soldeAVenirContreValeur")
    private double soldeaComingAgainstValue;

    @JsonProperty("indicateurSolde")
    private int balanceIndicator;

    @JsonProperty("indicateurSoldeEnCours")
    private int balanceIndicatorUnderway;

    @JsonProperty("avertissementDepassement")
    private int overflowWarning;

    @JsonProperty("typeIdentifiantProduit")
    private int typeIdentifierMaterial;

    @JsonProperty("codeEcoCompte")
    private int ecoAccountCode;

    @JsonProperty("indicAssuranceVie")
    private int indicLifeInsurance;

    @JsonProperty("indicCompteJoint")
    private int indicJointAccount;

    @JsonProperty("indicCompteTiers")
    private int thirdIndicAccount;

    private String devise;

    @JsonProperty("nomAgence")
    private String agencyName;

    @JsonProperty("typeCompte")
    private AccountTypesLabelEntity accountTypes;

    @JsonProperty("titulaire")
    private HolderEntity holder;

    private List<ServicesEntity> services;
    private int eligibleSolde;

    @JsonProperty("comptePrincipal")
    private boolean mainAccount;

    @JsonProperty("compteFavori")
    private boolean accountFavorite;

    @JsonProperty("indicLivret")
    private boolean indicBooklet;

    @JsonProperty("montantDecouvertAutorise")
    private int authorizesAmountDiscovered;

    private String ribAnonymise;

    @JsonProperty("faciliteCaisse")
    private double facilitatesFund;

    @JsonIgnore
    private Optional<TransactionalAccount> toTinkAccount(
            String iban, TransactionalAccountType accountType) {
        if (Strings.isNullOrEmpty(iban)) {
            return Optional.empty();
        }
        return TransactionalAccount.nxBuilder()
                .withType(accountType)
                .withoutFlags()
                .withBalance(BalanceModule.of(getTinkAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(iban)
                                .withAccountName(getAccountName())
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(getCustomerName())
                .putInTemporaryStorage(BnpParibasConstants.Storage.IBAN_KEY, ibanKey)
                .build();
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkCheckingAccount(String iban) {
        return this.toTinkAccount(iban, TransactionalAccountType.CHECKING);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkSavingsAccount(String iban) {
        return this.toTinkAccount(iban, TransactionalAccountType.SAVINGS);
    }

    @JsonIgnore
    private String getCustomerName() {
        if (holder.getFirstName() == null || holder.getLastName() == null) {
            return null;
        }

        return holder.getFirstName() + " " + holder.getLastName();
    }

    private ExactCurrencyAmount getTinkAmount() {
        return ExactCurrencyAmount.inEUR(availableBalance);
    }

    public String getIbanKey() {
        return ibanKey;
    }

    private String getAccountName() {
        return this.productLabel;
    }
}
