package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.fetcher.entities;

import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.targobank.utils.TargoBankUtils;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.core.Amount;

@XmlRootElement(name = "compte")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountDetailsEntity {
    @XmlElement(name = "account_type")
    private String accountType;
    private String iban;
    @XmlElement(name = "account_number")
    private String accountNumber;
    @XmlElement(name = "devise")
    private String currency;
    @XmlElement(name = "intc")
    private String accountNameAndNumber;
    @XmlElement(name = "int")
    private String accountName;
    private String codprod;
    @XmlElement(name = "category_code")
    private String categoryCode;
    @XmlElement(name = "category_name")
    private String categoryName;
    @XmlElement(name = "solde")
    private String amountToParse;
    @XmlElement(name = "transactions_to_come")
    private String transactionsToCome;
    @XmlElement(name = "webid")
    private String webId;
    private String contract;

    public String getContract() {
        return contract;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getIban() {
        return iban;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public String getAccountNameAndNumber() {
        return accountNameAndNumber;
    }

    public String getAccountName() {
        return accountName;
    }

    public String getCodprod() {
        return codprod;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getAmountToParse() {
        return amountToParse;
    }

    public String getTransactionsToCome() {
        return transactionsToCome;
    }

    public String getWebId() {
        return webId;
    }

    public TransactionalAccount toTransactionalAccount() {
        Amount amount = TargoBankUtils.parseAmount(amountToParse, currency);
        return TransactionalAccount.builder(
                getTinkTypeByTypeNumber().getTinkType(), accountNumber, amount)
                .build();
    }

    public Account.Builder<? extends Account, ?> getAccountBuilder() {
        Amount amount = TargoBankUtils.parseAmount(amountToParse, currency);
        return Account.builder(getTinkTypeByTypeNumber().getTinkType()).setAccountNumber(accountNumber)
                .setBalance(amount);
    }

    public AccountTypeEnum getTinkTypeByTypeNumber() {
        return Arrays.stream(AccountTypeEnum.values())
                .filter(v -> v.getType().equalsIgnoreCase(accountType))
                .findFirst()
                .orElse(AccountTypeEnum.UNKNOWN);
    }
}
