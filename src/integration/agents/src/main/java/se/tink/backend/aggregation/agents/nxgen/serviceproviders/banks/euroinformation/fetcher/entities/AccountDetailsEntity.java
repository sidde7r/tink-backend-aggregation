package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.utils.EuroInformationUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.libraries.amount.Amount;

@JsonObject
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

    @XmlElement(name = "tit")
    private String title;

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
    private String refprd;

    @XmlElement(name = "refctr_exi_val")
    private String refctrExiVal;

    @XmlElement(name = "refctr_inn_val")
    private String refctrInnVal;

    @XmlElement(name = "agreed_overdraft")
    private String agreedOverdraft;

    @XmlElement(name = "amount_available")
    private String amountAvailable;

    private String appcpt;
    private String isholder;
    private String checkingaccount;
    private String characteristics;
    private String simulation;

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

    // Using it as we store this entity in `SessionStorage`, which  serializes to JSON
    // because of that mapper searcher for field `accountBuilder` and throws `NullPointerException`
    @JsonIgnore
    public Account.Builder<? extends Account, ?> getAccountBuilder() {
        Amount amount = EuroInformationUtils.parseAmount(amountToParse, currency);

        return Account.builder(getTinkTypeByTypeNumber().getTinkType(), decideUniqueIdentifier())
                .setAccountNumber(accountNumber)
                .setBalance(amount);
    }

    @JsonIgnore
    private String decideUniqueIdentifier() {
        if (!Strings.isNullOrEmpty(iban)) {
            return iban;
        }
        if (!Strings.isNullOrEmpty(accountNumber)) {
            return accountNumber;
        }
        return parseAccountNumberFromName();
    }

    // Using it as we store this entity in `SessionStorage`, which  serializes to JSON
    // because of that mapper searcher for field `accountBuilder` and throws `NullPointerException`
    @JsonIgnore
    public AccountTypeEnum getTinkTypeByTypeNumber() {
        return Arrays.stream(AccountTypeEnum.values())
                .filter(v -> v.getType().equalsIgnoreCase(accountType))
                .findFirst()
                .orElse(AccountTypeEnum.UNKNOWN);
    }

    @JsonIgnore
    public String parseAccountNumberFromName() {
        return accountNameAndNumber.split(accountName)[0].toLowerCase();
    }
}
