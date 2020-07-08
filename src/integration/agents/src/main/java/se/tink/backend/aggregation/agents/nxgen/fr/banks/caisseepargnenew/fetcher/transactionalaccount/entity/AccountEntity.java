package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.mapper.CaisseEpargneAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "CompteInterneSynt")
public class AccountEntity {
    // private static final Logger logger = LoggerFactory.getLogger(AccountEntity.class);

    @JacksonXmlProperty(localName = "NumeroRib")
    private String fullAccountNumber;

    @JacksonXmlProperty(localName = "NumeroCompteReduit")
    private String reducedAccountNumber;

    @JacksonXmlProperty(localName = "LibelleTypeProduit")
    private String productTypeLabel;

    @JacksonXmlProperty(localName = "MontantSoldeCompte")
    private BigDecimal amountBalanceAccount;

    @JacksonXmlProperty(localName = "CodeDevise")
    private String currencyCode;

    @JacksonXmlProperty(localName = "IntituleProduit")
    private String productTitle;

    @JacksonXmlProperty(localName = "LibelleAbregeTypeProduit")
    private String libelleAbregeProductType;

    @JacksonXmlProperty(localName = "IsClicable")
    private String isClickable;

    @JacksonXmlProperty(localName = "CodeSens")
    private String codeMeaning;

    @JacksonXmlProperty(localName = "MontantDecouvert")
    private String amountDecouvert;

    @JacksonXmlProperty(localName = "CodeDeviseDecouvert")
    private String codeCurrencyDecouvert;

    @JacksonXmlProperty(localName = "CodeSensDecouvert")
    private String codeSensDecouvert;

    @JacksonXmlProperty(localName = "CodeProduit")
    private String productCode;

    @JacksonXmlProperty(localName = "CodeCategorieProduit")
    private String codeProductCategory;

    @JacksonXmlProperty(localName = "NumeroRibCompteLie")
    private String numberRibAccountLie;

    @JacksonXmlProperty(localName = "IndicateurChequierRice")
    private String chequierRiceIndicator;

    @JacksonXmlProperty(localName = "CodeMeteo")
    private String codeMeteo;

    @JacksonXmlProperty(localName = "Personnalise")
    private String customize;

    @JacksonXmlProperty(localName = "SeuilMin")
    private String minThreshold;

    @JacksonXmlProperty(localName = "SeuilMax")
    private String maxThreshold;

    @JacksonXmlProperty(localName = "NvAutoCpt")
    private String nvAutoCpt;

    @JsonIgnore private String iban;

    @JsonIgnore
    private ExactCurrencyAmount getBalance() {
        BigDecimal amount =
                amountBalanceAccount.divide(BigDecimal.valueOf(100), 2, RoundingMode.UNNECESSARY);
        return ExactCurrencyAmount.of(amount, "EUR");
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount() {
        if (!getAccountType().isPresent() || Strings.isNullOrEmpty(iban)) {
            return Optional.empty();
        }
        return TransactionalAccount.nxBuilder()
                .withType(getAccountType().get())
                .withoutFlags()
                // TODO make sure balance is right sign. Currently there is no way of knowing
                // positive/negative balance difference from the response.
                .withBalance(BalanceModule.of(getBalance()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(iban)
                                .withAccountNumber(reducedAccountNumber)
                                .withAccountName(productTypeLabel)
                                .addIdentifier(new IbanIdentifier(iban))
                                .build())
                .addHolderName(productTitle)
                .setBankIdentifier(fullAccountNumber)
                .build();
    }

    @JsonIgnore
    private Optional<TransactionalAccountType> getAccountType() {
        return CaisseEpargneAccountTypeMapper.getAccountType(productCode);
    }

    public String getFullAccountNumber() {
        return fullAccountNumber;
    }

    @JsonIgnore
    public void setIban(String iban) {
        this.iban = iban;
    }
}
