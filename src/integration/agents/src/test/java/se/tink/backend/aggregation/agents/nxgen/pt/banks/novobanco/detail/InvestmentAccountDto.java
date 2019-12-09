package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail;

import java.math.BigDecimal;
import java.util.List;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class InvestmentAccountDto {
    private String accountNumber;
    private String uniqueIdentifier;
    private String description;
    private ExactCurrencyAmount exactBalance;
    private List<PortfolioDto> portfolios;

    public InvestmentAccountDto(
            String accountNumber,
            String uniqueIdentifier,
            String description,
            ExactCurrencyAmount exactBalance,
            List<PortfolioDto> portfolios) {
        this.accountNumber = accountNumber;
        this.uniqueIdentifier = uniqueIdentifier;
        this.description = description;
        this.exactBalance = exactBalance;
        this.portfolios = portfolios;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getUniqueIdentifier() {
        return uniqueIdentifier;
    }

    public ExactCurrencyAmount getExactBalance() {
        return exactBalance;
    }

    public String getDescription() {
        return description;
    }

    public List<PortfolioDto> getPortfolios() {
        return portfolios;
    }

    public static class PortfolioDto {
        private String uniqueIdentifier;
        private Double cashValue;
        private Double totalProfit;
        private Double totalValue;

        public PortfolioDto(
                String uniqueIdentifier,
                BigDecimal cashValue,
                BigDecimal totalProfit,
                BigDecimal totalValue) {
            this.uniqueIdentifier = uniqueIdentifier;
            this.cashValue = cashValue.doubleValue();
            this.totalProfit = totalProfit.doubleValue();
            this.totalValue = totalValue.doubleValue();
        }

        public String getUniqueIdentifier() {
            return uniqueIdentifier;
        }

        public Double getCashValue() {
            return cashValue;
        }

        public Double getTotalProfit() {
            return totalProfit;
        }

        public Double getTotalValue() {
            return totalValue;
        }
    }
}
