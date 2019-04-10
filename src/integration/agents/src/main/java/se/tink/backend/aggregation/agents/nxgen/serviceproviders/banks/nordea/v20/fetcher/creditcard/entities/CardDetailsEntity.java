package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.libraries.strings.StringUtils;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CardDetailsEntity {
    @JsonProperty("ATMWithdrawals")
    private Map<String, Object> atmWithdrawals = new HashMap<String, Object>();

    private Map<String, Object> authorityRole = new HashMap<String, Object>();
    private Map<String, Object> balance = new HashMap<String, Object>();
    private Map<String, Object> balanceDate = new HashMap<String, Object>();
    private Map<String, Object> creditLimit = new HashMap<String, Object>();
    private Map<String, Object> creditUsed = new HashMap<String, Object>();
    private Map<String, Object> fundsAvailable = new HashMap<String, Object>();
    private Map<String, Object> mainCardHolderName = new HashMap<String, Object>();
    private Map<String, Object> mainCardId = new HashMap<String, Object>();
    private Map<String, Object> minimumInstalmentTextCode = new HashMap<String, Object>();
    private Map<String, Object> outstandingCreditAmount = new HashMap<String, Object>();
    private Map<String, Object> ownerName = new HashMap<String, Object>();
    private Map<String, Object> sideCards = new HashMap<String, Object>();
    private Map<String, Object> statusCode = new HashMap<String, Object>();

    public Map<String, Object> getAtmWithdrawals() {
        return atmWithdrawals;
    }

    public void setAtmWithdrawals(Map<String, Object> atmWithdrawals) {
        this.atmWithdrawals = atmWithdrawals;
    }

    public Map<String, Object> getAuthorityRole() {
        return authorityRole;
    }

    public void setAuthorityRole(Map<String, Object> authorityRole) {
        this.authorityRole = authorityRole;
    }

    public Map<String, Object> getBalance() {
        return balance;
    }

    public void setBalance(Map<String, Object> balance) {
        this.balance = balance;
    }

    public Map<String, Object> getBalanceDate() {
        return balanceDate;
    }

    public void setBalanceDate(Map<String, Object> balanceDate) {
        this.balanceDate = balanceDate;
    }

    public Map<String, Object> getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(Map<String, Object> creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Map<String, Object> getCreditUsed() {
        return creditUsed;
    }

    public void setCreditUsed(Map<String, Object> creditUsed) {
        this.creditUsed = creditUsed;
    }

    public Map<String, Object> getFundsAvailable() {
        return fundsAvailable;
    }

    public void setFundsAvailable(Map<String, Object> fundsAvailable) {
        this.fundsAvailable = fundsAvailable;
    }

    public Map<String, Object> getMainCardHolderName() {
        return mainCardHolderName;
    }

    public void setMainCardHolderName(Map<String, Object> mainCardHolderName) {
        this.mainCardHolderName = mainCardHolderName;
    }

    public Map<String, Object> getMainCardId() {
        return mainCardId;
    }

    public void setMainCardId(Map<String, Object> mainCardId) {
        this.mainCardId = mainCardId;
    }

    public Map<String, Object> getMinimumInstalmentTextCode() {
        return minimumInstalmentTextCode;
    }

    public void setMinimumInstalmentTextCode(Map<String, Object> minimumInstalmentTextCode) {
        this.minimumInstalmentTextCode = minimumInstalmentTextCode;
    }

    public Map<String, Object> getOutstandingCreditAmount() {
        return outstandingCreditAmount;
    }

    public void setOutstandingCreditAmount(Map<String, Object> outstandingCreditAmount) {
        this.outstandingCreditAmount = outstandingCreditAmount;
    }

    public Map<String, Object> getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(Map<String, Object> ownerName) {
        this.ownerName = ownerName;
    }

    public Map<String, Object> getSideCards() {
        return sideCards;
    }

    public void setSideCards(Map<String, Object> sideCards) {
        this.sideCards = sideCards;
    }

    public Map<String, Object> getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Map<String, Object> statusCode) {
        this.statusCode = statusCode;
    }

    // Nordea has a bug in their app when you have exceeded your credit limit.
    // We calculate balance as available funds - credit limit instead
    public double getCurrentBalance() {

        // First priority is Balance = FundsAvailable - CreditLimit
        if (getFundsAvailable().containsKey("$") && getCreditLimit().containsKey("$")) {
            return StringUtils.parseAmount(getFundsAvailable().get("$").toString())
                    - StringUtils.parseAmount(getCreditLimit().get("$").toString());

            // Fallback to using the creditUsed field if above isn't available
        } else if (getCreditUsed().containsKey("$")) {
            return -StringUtils.parseAmount(getCreditUsed().get("$").toString());
        }

        return 0;
    }

    public double constructAvailableFunds() {
        if (fundsAvailable.containsKey("$")) {
            return AgentParsingUtils.parseAmount(fundsAvailable.get("$").toString());
        }

        return 0;
    }
}
