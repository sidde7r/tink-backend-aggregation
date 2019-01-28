package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.fetcher.transactionalaccount.entities;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.api.client.util.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngHelper;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities.LoginResponseEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.account.identifiers.SepaEurIdentifier;

@XmlRootElement
public class AccountEntity {
    public static final AggregationLogger LOGGER = new AggregationLogger(AccountEntity.class);

    private String type;
    private String category;
    private String ibanNumber;
    private String bbanNumber;
    private String account313;
    private String mnemonic;
    private String currency;
    private String name;
    private String address;
    private String city;
    private String country;
    private String powerCode;
    private String signCode;
    private String transactionAllowed;
    private String beneficiaryAllowed;
    private String rulesCode;
    private BeneficiaryRuleListEntity beneficiaryRules;
    private String balance;
    private String centralisationCode;
    private String movementAvailability;
    private String dateAccountBalance;
    private String availableAmount;

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return this.category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getIbanNumber() {
        return this.ibanNumber;
    }

    public void setIbanNumber(String ibanNumber) {
        this.ibanNumber = ibanNumber;
    }

    public String getBbanNumber() {
        return this.bbanNumber;
    }

    public void setBbanNumber(String bbanNumber) {
        this.bbanNumber = bbanNumber;
    }

    public String getAccount313() {
        return this.account313;
    }

    public void setAccount313(String account313) {
        this.account313 = account313;
    }

    public String getMnemonic() {
        return this.mnemonic;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPowerCode() {
        return this.powerCode;
    }

    public void setPowerCode(String powerCode) {
        this.powerCode = powerCode;
    }

    public String getSignCode() {
        return this.signCode;
    }

    public void setSignCode(String signCode) {
        this.signCode = signCode;
    }

    public String getTransactionAllowed() {
        return this.transactionAllowed;
    }

    public void setTransactionAllowed(String transactionAllowed) {
        this.transactionAllowed = transactionAllowed;
    }

    public String getBeneficiaryAllowed() {
        return this.beneficiaryAllowed;
    }

    public void setBeneficiaryAllowed(String beneficiaryAllowed) {
        this.beneficiaryAllowed = beneficiaryAllowed;
    }

    public String getRulesCode() {
        return this.rulesCode;
    }

    public void setRulesCode(String rulesCode) {
        this.rulesCode = rulesCode;
    }

    public BeneficiaryRuleListEntity getBeneficiaryRules() {
        return this.beneficiaryRules;
    }

    public void setBeneficiaryRules(
            BeneficiaryRuleListEntity beneficiaryRules) {
        this.beneficiaryRules = beneficiaryRules;
    }

    public String getBalance() {
        return this.balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getCentralisationCode() {
        return this.centralisationCode;
    }

    public void setCentralisationCode(String centralisationCode) {
        this.centralisationCode = centralisationCode;
    }

    public String getMovementAvailability() {
        return this.movementAvailability;
    }

    public void setMovementAvailability(String movementAvailability) {
        this.movementAvailability = movementAvailability;
    }

    public String getDateAccountBalance() {
        return this.dateAccountBalance;
    }

    public void setDateAccountBalance(String dateAccountBalance) {
        this.dateAccountBalance = dateAccountBalance;
    }

    public String getAvailableAmount() {
        return this.availableAmount;
    }

    public void setAvailableAmount(String availableAmount) {
        this.availableAmount = availableAmount;
    }

    public boolean isDesiredType() {
        return IngConstants.AccountTypes.CURRENT_ACCOUNT.equalsIgnoreCase(category)
                || IngConstants.AccountTypes.SAVINGS_ACCOUNT.equalsIgnoreCase(category);
    }

    public TransactionalAccount toTinkAccount(LoginResponseEntity loginResponse) {
        return TransactionalAccount.builder(getTinkAccountType(), ibanNumber,
                Amount.inEUR(IngHelper.parseAmountStringToDouble(balance)))
                .setAccountNumber(ibanNumber)
                .setName(type)
                .setBankIdentifier(bbanNumber)
                .addIdentifier(new SepaEurIdentifier(ibanNumber))
                .setHolderName(getHolderName(loginResponse))
                .build();
    }

    private HolderName getHolderName(final LoginResponseEntity loginResponse) {
        // If the name of the account holder is empty, fall back on customer holder name.
        if (Strings.isNullOrEmpty(name)) {
            return loginResponse.getCustomerHolderName().orElse(null);
        }
        return new HolderName(name);
    }

    private AccountTypes getTinkAccountType() {
        if (category == null) {
            return AccountTypes.OTHER;
        }

        switch(this.category.toLowerCase()) {
        case IngConstants.AccountTypes.CURRENT_ACCOUNT:
            return AccountTypes.CHECKING;
        case IngConstants.AccountTypes.SAVINGS_ACCOUNT:
            return AccountTypes.SAVINGS;
        default:
            LOGGER.warn(String.format(
                    "Could not map account type [%s] to a Tink account type", type));
            return AccountTypes.OTHER;
        }
    }
}
