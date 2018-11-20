package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.NextGenDemoConstants;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.core.Amount;
import se.tink.libraries.i18n.Catalog;

public class LoanGenerator {
    private static final String MORTGAGE_ID = "9999-333333333333";
    private static final String BLANCO_ID = "9999-333334444444";

    public static Collection<LoanAccount> fetchLoanAccounts(String currency, Catalog catalog) {
        ArrayList<LoanAccount> loanAccounts = Lists.newArrayList();

        loanAccounts.add(LoanAccount.builder(MORTGAGE_ID,
                new Amount(currency, NextGenDemoConstants.getSekToCurrencyConverter(currency) * -2300000D))
                .setAccountNumber(MORTGAGE_ID)
                .setName(catalog.getString("Bolån"))
                .setBankIdentifier(MORTGAGE_ID)
                .setInterestRate(0.019)
                .setDetails(buildLoanDetails(LoanDetails.Type.MORTGAGE))
                .build());

        loanAccounts.add(LoanAccount.builder(BLANCO_ID,
                new Amount(currency, NextGenDemoConstants.getSekToCurrencyConverter(currency) * -50000D))
                .setAccountNumber(BLANCO_ID)
                .setName(catalog.getString("Santander"))
                .setBankIdentifier(BLANCO_ID)
                .setInterestRate(1.19)
                .setDetails(buildLoanDetails(LoanDetails.Type.BLANCO))
                .build());

        return loanAccounts;
    }

    //TODO Add more details
    private static LoanDetails buildLoanDetails(LoanDetails.Type type) {
        return LoanDetails.builder(type)
                .build();
    }
}
