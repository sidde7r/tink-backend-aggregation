package se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.LoanTypes;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.montepio.MontepioConstants.PropertyKeys;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonIgnore private static final Logger log = LoggerFactory.getLogger(AccountEntity.class);

    @JsonIgnore
    private static final Map<String, LoanDetails.Type> productNameToLoanType = new HashMap<>();

    static {
        productNameToLoanType.put(LoanTypes.MORTGAGE, LoanDetails.Type.MORTGAGE);
        productNameToLoanType.put(LoanTypes.INDIVUDUAL_CREDIT, LoanDetails.Type.CREDIT);
        productNameToLoanType.put(LoanTypes.MORTGAGE_ADDON, LoanDetails.Type.MORTGAGE);
    }

    @JsonProperty("Balance")
    private double balance;

    @JsonProperty("Currency")
    private String currency;

    @JsonProperty("Id")
    private String handle; // used for details and transaction fetching

    @JsonProperty("Name")
    private String name;

    @JsonProperty("Number")
    private String number;

    @JsonIgnore
    public Optional<TransactionalAccount> toCheckingAccount(String iban) {
        BalanceModule balanceModule = BalanceModule.builder().withBalance(balance()).build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(iban)
                        .withAccountNumber(iban)
                        .withAccountName(name)
                        .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                        .build();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(balanceModule)
                .withId(idModule)
                .putInTemporaryStorage(PropertyKeys.HANDLE, handle)
                .build();
    }

    @JsonIgnore
    public InvestmentAccount toInvestmentAccount() {
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.COUNTRY_SPECIFIC, number))
                        .build();
        return InvestmentAccount.nxBuilder()
                .withoutPortfolios()
                .withCashBalance(balance())
                .withId(idModule)
                .putInTemporaryStorage(PropertyKeys.HANDLE, handle)
                .build();
    }

    @JsonIgnore
    public Optional<LoanAccount> toLoanAccount(Map<String, String> details) {

        if (!loanType(name).isPresent()) {
            return Optional.empty();
        }

        double interest = getInterestRate(details);
        Double initialBalance = getInitialBalance(details);
        String holder = getHolderName(details);
        LoanModule loanModule =
                LoanModule.builder()
                        .withType(loanType(name).get())
                        .withBalance(balance())
                        .withInterestRate(interest)
                        .setInitialBalance(ExactCurrencyAmount.of(initialBalance, currency))
                        .setApplicants(Collections.singletonList(holder))
                        .build();
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(number)
                        .withAccountNumber(number)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifierType.COUNTRY_SPECIFIC, number))
                        .build();
        return Optional.of(
                LoanAccount.nxBuilder()
                        .withLoanDetails(loanModule)
                        .withId(idModule)
                        .addHolderName(holder)
                        .build());
    }

    @JsonIgnore
    private double getInterestRate(Map<String, String> details) {
        String propertyValue = details.get(PropertyKeys.LOAN_INTEREST_DETAILS_KEY);
        Preconditions.checkNotNull(Strings.emptyToNull(propertyValue));
        propertyValue = propertyValue.replace("%", "");
        propertyValue = propertyValue.replace(",", ".");
        return Double.parseDouble(propertyValue);
    }

    @JsonIgnore
    private double getInitialBalance(Map<String, String> details) {
        String propertyValue = details.get(PropertyKeys.LOAN_INITIAL_BALANCE_DETAILS_KEY);
        Preconditions.checkNotNull(Strings.emptyToNull(propertyValue));
        return Double.parseDouble(propertyValue);
    }

    @JsonIgnore
    private String getHolderName(Map<String, String> details) {
        String propertyValue = details.get(PropertyKeys.LOAN_HOLDER_NAME_DETAILS_KEY);
        Preconditions.checkNotNull(Strings.emptyToNull(propertyValue));
        return propertyValue.trim();
    }

    @JsonIgnore
    private ExactCurrencyAmount balance() {
        return ExactCurrencyAmount.of(balance, currency);
    }

    @JsonIgnore
    private Optional<LoanDetails.Type> loanType(String productName) {
        LoanDetails.Type resultType = productNameToLoanType.get(productName);
        if (resultType == null) {
            log.warn(
                    "Loan type mapping for product >>>{}<<< not found, skipping mapping",
                    productName);
            return Optional.empty();
        }
        return Optional.of(resultType);
    }

    @JsonIgnore
    public String getHandle() {
        return handle;
    }

    @JsonIgnore
    public String getNumber() {
        return number;
    }
}
