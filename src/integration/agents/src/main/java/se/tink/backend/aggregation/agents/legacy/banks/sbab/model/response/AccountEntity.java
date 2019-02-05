package se.tink.backend.aggregation.agents.banks.sbab.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.agents.rpc.Account;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity implements GeneralAccountEntity {

    private static final AggregationLogger log = new AggregationLogger(AccountEntity.class);

    @JsonProperty("formattedSaldo")
    private String formattedBalance;

    @JsonProperty("kontonamn")
    private String name;

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("kontostatus")
    private String status;

    @JsonProperty("saldo")
    private String balance;

    @JsonProperty("pagaendeUppdrag")
    private int numberOfPendingJobs;

    @JsonProperty("maxNoOfAccounts")
    private boolean maxNumberOfAccounts;

    @JsonProperty("ejGenomfordaUppdrag")
    private int numberOfUnfinishedJobs;

    public String getFormattedBalance() {
        return formattedBalance;
    }

    public void setFormattedBalance(String formattedBalance) {
        this.formattedBalance = formattedBalance;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public int getNumberOfPendingJobs() {
        return numberOfPendingJobs;
    }

    public void setNumberOfPendingJobs(int numberOfPendingJobs) {
        this.numberOfPendingJobs = numberOfPendingJobs;
    }

    public boolean isMaxNumberOfAccounts() {
        return maxNumberOfAccounts;
    }

    public void setMaxNumberOfAccounts(boolean maxNumberOfAccounts) {
        this.maxNumberOfAccounts = maxNumberOfAccounts;
    }

    public int getNumberOfUnfinishedJobs() {
        return numberOfUnfinishedJobs;
    }

    public void setNumberOfUnfinishedJobs(int numberOfUnfinishedJobs) {
        this.numberOfUnfinishedJobs = numberOfUnfinishedJobs;
    }

    public Optional<Account> toTinkAccount() {
        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        account.setAccountNumber(getAccountNumber());
        account.setBankId(getAccountNumber());
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        if (!Strings.isNullOrEmpty(getBalance()) && !getBalance().trim().isEmpty()) {
            String cleanBalance = getBalance().replaceAll("[^\\d.,]", "");
            account.setBalance(StringUtils.parseAmount(cleanBalance));
        } else {
            log.error("An account cannot have a null balance");
            return Optional.empty();
        }

        String name = !Strings.isNullOrEmpty(getName()) ? getName() : getAccountNumber();
        account.setName(name == null ? "" : name.replace("\n", "").replace("\r", ""));

        return Optional.of(account);
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(getAccountNumber());
    }

    @Override
    public String generalGetBank() {
        if (generalGetAccountIdentifier().isValid()) {
            return generalGetAccountIdentifier().to(SwedishIdentifier.class).getBankName();
        }
        return null;
    }

    @Override
    public String generalGetName() {
        return getName();
    }
}
