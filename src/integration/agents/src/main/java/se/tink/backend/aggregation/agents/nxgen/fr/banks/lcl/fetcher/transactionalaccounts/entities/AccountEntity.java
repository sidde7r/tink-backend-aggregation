package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

public class AccountEntity {
    private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

    @JsonProperty("famille")
    private String group;

    @JsonProperty("natureLibelle")
    private String typeLabel;

    @JsonProperty("agence")
    private String agency;

    @JsonProperty("compte")
    private String accountNumber;

    @JsonProperty("lettreCle")
    private String cleLetter;

    @JsonProperty("encoursCB")
    private String outstandingCb;

    @JsonProperty("natureLibelleOriginal")
    private String originalTypeLabel;

    private String topDevise;

    @JsonProperty("natureCode")
    private String typeCode;

    @JsonProperty("soldeComptable")
    private String accountedBalance;

    @JsonProperty("soldeFormatG3P")
    private String g3pFormattedBalance;

    private int nbMvtG3P;

    @JsonProperty("soldeG3P")
    private int g3pBalance;

    private String topMADPro;

    @JsonProperty("soldeEnValeur")
    private String balanceInValue;

    @JsonProperty("soldeDisponible")
    private String availableBalance;

    @JsonProperty("soldeOperNonComprise")
    private String operNotIncludedBalance;

    @JsonProperty("soldeOperNonCompriseSigne")
    private String operNotIncludedBalanceSign;

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(AccountDetailsEntity accountDetailsEntity) {
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType())
                .withoutFlags()
                .withBalance(BalanceModule.of(getAmount()))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountDetailsEntity.getIban().toLowerCase())
                                .withAccountNumber(accountDetailsEntity.getIban())
                                .withAccountName(typeLabel != null ? typeLabel : "")
                                .addIdentifier(new IbanIdentifier(accountDetailsEntity.getIban()))
                                .build())
                .addHolderName(accountDetailsEntity.getHolderName())
                .putInTemporaryStorage(
                        LclConstants.Storage.ACCOUNT_DETAILS_ENTITY, accountDetailsEntity)
                .build();
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.inEUR(StringUtils.parseAmount(availableBalance));
    }

    @JsonIgnore
    private TransactionalAccountType getTinkAccountType() {
        if (isCheckingAccount()) {
            return TransactionalAccountType.CHECKING;
        }

        log.warn(
                "{} Unknown account type. Type label: {}, original label: {}",
                LclConstants.Logs.UNKNOWN_ACCOUNT_TYPE,
                typeLabel,
                originalTypeLabel);

        return TransactionalAccountType.OTHER;
    }

    @JsonIgnore
    private boolean isCheckingAccount() {
        return LclConstants.AccountTypes.CHECKING_ACCOUNT.equalsIgnoreCase(getTypeLabel());
    }

    public String getGroup() {
        return group;
    }

    public String getTypeLabel() {
        return typeLabel;
    }

    public String getAgency() {
        return agency;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCleLetter() {
        return cleLetter;
    }

    public String getOutstandingCb() {
        return outstandingCb;
    }

    public String getOriginalTypeLabel() {
        return originalTypeLabel;
    }

    public String getTopDevise() {
        return topDevise;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public String getAccountedBalance() {
        return accountedBalance;
    }

    public String getG3pFormattedBalance() {
        return g3pFormattedBalance;
    }

    public int getNbMvtG3P() {
        return nbMvtG3P;
    }

    public int getG3pBalance() {
        return g3pBalance;
    }

    public String getTopMADPro() {
        return topMADPro;
    }

    public String getBalanceInValue() {
        return balanceInValue;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public String getOperNotIncludedBalance() {
        return operNotIncludedBalance;
    }

    public String getOperNotIncludedBalanceSign() {
        return operNotIncludedBalanceSign;
    }
}
