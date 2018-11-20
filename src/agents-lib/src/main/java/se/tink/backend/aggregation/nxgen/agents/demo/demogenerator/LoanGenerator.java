package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.LoanDetails;
import se.tink.backend.core.Amount;
import se.tink.libraries.i18n.Catalog;

public class LoanGenerator {


    public static Collection<LoanAccount> fetchLoanAccounts(String currency, Catalog catalog) {
        ArrayList<LoanAccount> loanAccounts = Lists.newArrayList();

        loanAccounts.add(LoanAccount.builder(DemoConstants.LoanAccountInformation.MORTGAGE_ID,
                new Amount(currency, DemoConstants.getSekToCurrencyConverter(currency,
                        DemoConstants.LoanAccountInformation.MORTGAGE_BALANCE)))
                .setAccountNumber(DemoConstants.LoanAccountInformation.MORTGAGE_ID)
                .setName(catalog.getString(DemoConstants.LoanAccountInformation.MORTGAGE_LOAN_NAME))
                .setBankIdentifier(DemoConstants.LoanAccountInformation.MORTGAGE_ID)
                .setInterestRate(DemoConstants.LoanAccountInformation.MORTGAGE_INTEREST_RATE)
                .setDetails(buildLoanDetails(LoanDetails.Type.MORTGAGE))
                .build());

        loanAccounts.add(LoanAccount.builder(DemoConstants.LoanAccountInformation.BLANCO_ID,
                new Amount(currency, DemoConstants.getSekToCurrencyConverter(currency,
                        DemoConstants.LoanAccountInformation.BLANCO_BALANCE)))
                .setAccountNumber(DemoConstants.LoanAccountInformation.BLANCO_ID)
                .setName(catalog.getString(DemoConstants.LoanAccountInformation.BLANCO_LOAN_NAME))
                .setBankIdentifier(DemoConstants.LoanAccountInformation.BLANCO_ID)
                .setInterestRate(DemoConstants.LoanAccountInformation.BLANCO_INTEREST_RATE)
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
