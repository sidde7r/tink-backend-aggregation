package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.i18n.Catalog;

public class LoanGenerator {

    public static Collection<LoanAccount> fetchLoanAccounts(
            String currency, Catalog catalog, DemoLoanAccount accountDefinition) {
        ArrayList<LoanAccount> loanAccounts = Lists.newArrayList();

        if (Objects.isNull(accountDefinition)) {
            return loanAccounts;
        }

        loanAccounts.add(
                LoanAccount.builder(
                                accountDefinition.getMortgageId(),
                                ExactCurrencyAmount.of(
                                        DemoConstants.getSekToCurrencyConverter(
                                                currency, accountDefinition.getMortgageBalance()),
                                        currency))
                        .setAccountNumber(accountDefinition.getMortgageId())
                        .setName(catalog.getString(accountDefinition.getMortgageLoanName()))
                        .setBankIdentifier(accountDefinition.getMortgageId())
                        .setInterestRate(accountDefinition.getMortgageInterestName())
                        .setDetails(buildLoanDetails(LoanDetails.Type.MORTGAGE))
                        .build());

        loanAccounts.add(
                LoanAccount.builder(
                                accountDefinition.getBlancoId(),
                                ExactCurrencyAmount.of(
                                        DemoConstants.getSekToCurrencyConverter(
                                                currency, accountDefinition.getBlancoBalance()),
                                        currency))
                        .setAccountNumber(accountDefinition.getBlancoId())
                        .setName(catalog.getString(accountDefinition.getBlancoLoanName()))
                        .setBankIdentifier(accountDefinition.getBlancoId())
                        .setInterestRate(accountDefinition.getBlancoInterestName())
                        .setDetails(buildLoanDetails(LoanDetails.Type.BLANCO))
                        .build());

        return loanAccounts;
    }

    // TODO Add more details
    private static LoanDetails buildLoanDetails(LoanDetails.Type type) {
        return LoanDetails.builder(type).build();
    }
}
