package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
@JacksonXmlRootElement(localName = "SyntInternalAccountEntity")
public class AccountEntity {

    private static final Logger logger = LoggerFactory.getLogger(AccountEntity.class);

    @JacksonXmlProperty(localName = "NumeroRib")
    private String fullAccountNumber;

    @JacksonXmlProperty(localName = "NumeroCompteReduit")
    private String reducedAccountNumber;

    @JacksonXmlProperty(localName = "LibelleTypeProduit")
    private String productTypeLabel;

    @JacksonXmlProperty(localName = "MontantSoldeCompte")
    private double amountBalanceAccount;

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

    private Amount getBalance() {
        return Amount.valueOf(currencyCode, Math.round(amountBalanceAccount), 2);
    }

    public TransactionalAccount toTinkAccount() {

        TransactionalAccount.Builder builder =
                TransactionalAccount.builder(
                                this.toTinkAccountType(), fullAccountNumber, this.getBalance())
                        .setAccountNumber(reducedAccountNumber)
                        .setName(productTypeLabel)
                        .setBankIdentifier(fullAccountNumber);

        return builder.build();
    }

    public boolean isTransactionalAccount() {
        switch (this.toTinkAccountType()) {
            case CHECKING:
            case SAVINGS:
                return true;
            default:
                return false;
        }
    }

    private AccountTypes toTinkAccountType() {

        Optional<AccountTypes> translated =
                CaisseEpargneConstants.AccountType.translate(productCode);

        if (!translated.isPresent()) {
            logger.info(
                    CaisseEpargneConstants.LogMessage.UNKNOWN_ACCOUNT_TYPE,
                    CaisseEpargneConstants.LogTag.UNKNOWN_ACCOUNT_TYPE,
                    productCode);
        }

        return translated.orElse(AccountTypes.OTHER);
    }
}
