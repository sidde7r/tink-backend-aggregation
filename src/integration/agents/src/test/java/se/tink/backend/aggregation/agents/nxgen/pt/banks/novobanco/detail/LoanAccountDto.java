package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LoanAccountDto {
    private String accountNumber;
    private String uniqueIdentifier;
    private String description;
    private ExactCurrencyAmount exactBalance;
    private ExactCurrencyAmount initialBalance;
    private String initialDate;
    private String contractId;
    private String productName;
    private Double interestRate;

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public String getDescription() {
        return description;
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public ExactCurrencyAmount getInitialBalance() {
        return initialBalance;
    }

    public String getInitialDate() {
        return initialDate;
    }

    public String getContractId() {
        return contractId;
    }

    public String getProductName() {
        return productName;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private LoanAccountDto account = new LoanAccountDto();

        public Builder withAccountNumber(String accountNumber) {
            account.accountNumber = accountNumber;
            return this;
        }

        public Builder withUniqueIdentifier(String uniqueIdentifier) {
            account.uniqueIdentifier = uniqueIdentifier;
            return this;
        }

        public Builder withDescription(String description) {
            account.description = description;
            return this;
        }

        public Builder withExactBalance(String amount, String currency) {
            account.exactBalance = ExactCurrencyAmount.of(parseValue(amount), currency);
            return this;
        }

        public Builder withInitialBalance(String amount, String currency) {
            account.initialBalance = ExactCurrencyAmount.of(parseValue(amount), currency);
            return this;
        }

        public Builder withInitialDate(String initialDate) {
            account.initialDate = initialDate;
            return this;
        }

        public Builder withContractId(String contractId) {
            account.contractId = contractId;
            return this;
        }

        public Builder withProductName(String productName) {
            account.productName = productName;
            return this;
        }

        public Builder withInterestRate(String interestRateAsString) {
            account.interestRate = Double.parseDouble(interestRateAsString.replace(',', '.'));
            return this;
        }

        public LoanAccountDto build() {
            return account;
        }
    }

    private static double parseValue(String value) {
        NumberFormat nf = NumberFormat.getInstance(new Locale("pt", "PT"));
        try {
            return nf.parse(value).doubleValue();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
