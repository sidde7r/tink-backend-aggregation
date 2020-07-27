package se.tink.backend.aggregation.agents.banks.sbab.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountEntity implements GeneralAccountEntity {
    @JsonIgnore
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @JsonProperty("produktnamn")
    private String productName;

    @JsonProperty("produkttyp")
    private String productType;

    @JsonProperty("kundvaltNamn")
    private String accountaName;

    @JsonProperty("kontonummer")
    private String accountNumber;

    @JsonProperty("saldo")
    private String balance;

    @JsonProperty("disponibeltBelopp")
    private String availableBalance;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getAccountaName() {
        return accountaName;
    }

    public void setAccountaName(String accountaName) {
        this.accountaName = accountaName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    public Optional<Account> toTinkAccount() {

        Account account = new Account();
        account.setType(AccountTypes.SAVINGS);
        account.putFlag(AccountFlag.PSD2_PAYMENT_ACCOUNT);
        account.setAccountNumber(accountNumber);
        account.setBankId(accountNumber);
        account.setBalance(Double.parseDouble(balance));
        account.setExactBalance(ExactCurrencyAmount.of(availableBalance, "SEK"));
        account.setName(accountaName);
        account.putIdentifier(new SwedishIdentifier(accountNumber));

        if (!Strings.isNullOrEmpty(balance) && !balance.trim().isEmpty()) {
            String cleanBalance = balance.replaceAll("[^\\d.,]", "");
            account.setBalance(StringUtils.parseAmount(cleanBalance));
        } else {
            logger.error("An account cannot have a null balance");
            return Optional.empty();
        }

        String name = !Strings.isNullOrEmpty(accountaName) ? accountaName : accountNumber;
        account.setName(name == null ? "" : name.replace("\n", "").replace("\r", ""));

        return Optional.of(account);
    }

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return new SwedishIdentifier(accountNumber);
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
        return accountaName;
    }
}
