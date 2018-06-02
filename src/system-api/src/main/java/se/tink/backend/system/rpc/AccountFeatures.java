package se.tink.backend.system.rpc;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AccountFeatures {
    private List<Loan> loans;
    private List<Portfolio> portfolios;

    public static AccountFeatures createEmpty() {
        return new AccountFeatures();
    }

    public static AccountFeatures createForLoan(Loan loan) {
        AccountFeatures assets = createEmpty();

        if (loan != null) {
            assets.setLoans(Arrays.asList(loan));
        }

        return assets;
    }

    public static AccountFeatures createForPortfolios(Portfolio portfolio) {
        AccountFeatures features = createEmpty();

        if (portfolio != null) {
            features.setPortfolios(ImmutableList.of(portfolio));
        }

        return features;
    }

    public static AccountFeatures createForPortfolios(List<Portfolio> portfolios) {
        AccountFeatures features = createEmpty();

        if (portfolios != null) {
            features.setPortfolios(portfolios);
        }

        return features;
    }

    public List<Loan> getLoans() {
        return loans == null ? Collections.emptyList() : loans;
    }

    public List<Portfolio> getPortfolios() {
        return portfolios == null ? Collections.emptyList() : portfolios;
    }

    public void setLoans(List<Loan> loans) {
        this.loans = loans;
    }

    public void setPortfolios(List<Portfolio> portfolios) {
        this.portfolios = portfolios;
    }
}
