package se.tink.backend.aggregation.nxgen.agents.demo.demogenerator;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.agents.demo.DemoConstants;
import se.tink.backend.aggregation.nxgen.agents.demo.data.DemoLoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.loan.LoanModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.i18n.Catalog;

public class LoanGenerator {

    public static Collection<LoanAccount> fetchLoanAccounts(
            String currency, Catalog catalog, DemoLoanAccount accountDefinition) {
        ArrayList<LoanAccount> loanAccounts = Lists.newArrayList();

        if (Objects.isNull(accountDefinition)) {
            return loanAccounts;
        }

        // Mortgage type loan
        loanAccounts.add(
                LoanAccount.nxBuilder()
                        .withLoanDetails(
                                LoanModule.builder()
                                        .withType(LoanDetails.Type.MORTGAGE)
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        DemoConstants.getSekToCurrencyConverter(
                                                                currency,
                                                                accountDefinition
                                                                        .getMortgageBalance()),
                                                        currency))
                                        .withInterestRate(
                                                accountDefinition.getMortgageInterestName())
                                        .setInitialDate(accountDefinition.getInitialDate())
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountDefinition.getMortgageId())
                                        .withAccountNumber(accountDefinition.getMortgageId())
                                        .withAccountName(accountDefinition.getMortgageLoanName())
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN,
                                                        accountDefinition.getMortgageId()))
                                        .setProductName(
                                                catalog.getString(
                                                        accountDefinition.getMortgageLoanName()))
                                        .build())
                        .setBankIdentifier(accountDefinition.getBlancoId())
                        .build());

        // Blanco type loam
        loanAccounts.add(
                LoanAccount.nxBuilder()
                        .withLoanDetails(
                                LoanModule.builder()
                                        .withType(LoanDetails.Type.BLANCO)
                                        .withBalance(
                                                ExactCurrencyAmount.of(
                                                        DemoConstants.getSekToCurrencyConverter(
                                                                currency,
                                                                accountDefinition
                                                                        .getBlancoBalance()),
                                                        currency))
                                        .withInterestRate(accountDefinition.getBlancoInterestName())
                                        .setInitialDate(accountDefinition.getInitialDate())
                                        .build())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountDefinition.getBlancoId())
                                        .withAccountNumber(accountDefinition.getBlancoId())
                                        .withAccountName(accountDefinition.getBlancoLoanName())
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.IBAN,
                                                        accountDefinition.getBlancoId()))
                                        .setProductName(
                                                catalog.getString(
                                                        accountDefinition.getMortgageLoanName()))
                                        .build())
                        .setBankIdentifier(accountDefinition.getBlancoId())
                        .build());

        return loanAccounts;
    }
}
