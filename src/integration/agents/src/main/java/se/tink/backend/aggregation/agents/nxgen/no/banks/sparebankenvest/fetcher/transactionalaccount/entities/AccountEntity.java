package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.SparebankenVestConstants;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.NorwegianIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity {
    @JsonProperty("sperretBeloep")
    private Double blockedAmount;

    private Boolean isDisponentOrHasInnsyn;

    private Boolean isFamilie;

    private Double bevilgning;

    private Boolean hasDisponenter;

    private Double disponibelt;

    private Boolean hasKort;

    @JsonProperty("saldo")
    private BigDecimal balance;

    private List<LenkerItem> lenker;

    private Object laanDetaljer;

    @JsonProperty("kontoeierNavn")
    private String accountHolderName;

    private ForfallResponse forfallResponse;

    private Object bsuDetaljer;

    @JsonProperty("kontotype")
    private String accountType;

    private Boolean displayAsDisponerer;

    @JsonProperty("kontonummer")
    private String accountNumber;

    private String namespace;

    private Object spareavtaleDetaljer;

    private String id;

    private Boolean isNudge;

    @JsonProperty("kontonavn")
    private String accountName;

    @JsonProperty("valutakode")
    private String currency;

    public void setblockedAmount(Double blockedAmount) {
        this.blockedAmount = blockedAmount;
    }

    public Double getblockedAmount() {
        return blockedAmount;
    }

    public void setIsDisponentOrHasInnsyn(Boolean isDisponentOrHasInnsyn) {
        this.isDisponentOrHasInnsyn = isDisponentOrHasInnsyn;
    }

    public Boolean isIsDisponentOrHasInnsyn() {
        return isDisponentOrHasInnsyn;
    }

    public void setIsFamilie(Boolean isFamilie) {
        this.isFamilie = isFamilie;
    }

    public Boolean isIsFamilie() {
        return isFamilie;
    }

    public void setBevilgning(Double bevilgning) {
        this.bevilgning = bevilgning;
    }

    public Double getBevilgning() {
        return bevilgning;
    }

    public void setHasDisponenter(Boolean hasDisponenter) {
        this.hasDisponenter = hasDisponenter;
    }

    public Boolean isHasDisponenter() {
        return hasDisponenter;
    }

    public void setDisponibelt(Double disponibelt) {
        this.disponibelt = disponibelt;
    }

    public Double getDisponibelt() {
        return disponibelt;
    }

    public void setHasKort(Boolean hasKort) {
        this.hasKort = hasKort;
    }

    public Boolean isHasKort() {
        return hasKort;
    }

    public void setBlance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setLenker(List<LenkerItem> lenker) {
        this.lenker = lenker;
    }

    public List<LenkerItem> getLenker() {
        return lenker;
    }

    public void setLaanDetaljer(Object laanDetaljer) {
        this.laanDetaljer = laanDetaljer;
    }

    public Object getLaanDetaljer() {
        return laanDetaljer;
    }

    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public void setForfallResponse(ForfallResponse forfallResponse) {
        this.forfallResponse = forfallResponse;
    }

    public ForfallResponse getForfallResponse() {
        return forfallResponse;
    }

    public void setBsuDetaljer(Object bsuDetaljer) {
        this.bsuDetaljer = bsuDetaljer;
    }

    public Object getBsuDetaljer() {
        return bsuDetaljer;
    }

    public void setKontotype(String accountType) {
        this.accountType = accountType;
    }

    public String getKontotype() {
        return accountType;
    }

    public void setDisplayAsDisponerer(Boolean displayAsDisponerer) {
        this.displayAsDisponerer = displayAsDisponerer;
    }

    public Boolean isDisplayAsDisponerer() {
        return displayAsDisponerer;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setSpareavtaleDetaljer(Object spareavtaleDetaljer) {
        this.spareavtaleDetaljer = spareavtaleDetaljer;
    }

    public Object getSpareavtaleDetaljer() {
        return spareavtaleDetaljer;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setIsNudge(Boolean isNudge) {
        this.isNudge = isNudge;
    }

    public Boolean isIsNudge() {
        return isNudge;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public Optional<TransactionalAccount> toTinkAccount() {
        Optional<AccountTypes> type =
                SparebankenVestConstants.ACCOUNT_TYPE_MAPPER.translate(accountType);

        // todo: should I throw this here?
        if (!type.isPresent()) {
            throw new IllegalStateException("Account type " + type + " not mapped.");
        }

        switch (type.get()) {
            case CHECKING:
                return toCheckingAccount();
            default:
                // If the account is something else then a checking or savings account
                // we should ignore it while parsing Transactional Accounts.
                return Optional.empty();
        }
    }

    @JsonIgnore
    private ExactCurrencyAmount getBalanceAsCurrencyAmount() {
        return ExactCurrencyAmount.of(balance, getCurrency());
    }

    @JsonIgnore
    private BalanceModule getBalanceModule() {
        return BalanceModule.builder().withBalance(getBalanceAsCurrencyAmount()).build();
    }

    private Optional<TransactionalAccount> toCheckingAccount() {
        IdModule ID_MODULE =
                IdModule.builder()
                        .withUniqueIdentifier(accountNumber)
                        .withAccountNumber(accountNumber)
                        .withAccountName(accountName)
                        .addIdentifier(new NorwegianIdentifier(accountNumber))
                        .build();

        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withPaymentAccountFlag()
                .withBalance(getBalanceModule())
                .withId(ID_MODULE)
                .setApiIdentifier(accountNumber)
                .addHolderName(accountHolderName)
                .build();
    }
}
