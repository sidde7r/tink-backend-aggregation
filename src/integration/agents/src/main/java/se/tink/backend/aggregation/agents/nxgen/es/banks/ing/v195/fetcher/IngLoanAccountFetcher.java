package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngHolder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.entity.IngProduct;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IngLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private final IngApiClient ingApiClient;

    public IngLoanAccountFetcher(IngApiClient ingApiClient) {
        this.ingApiClient = ingApiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return this.ingApiClient.getApiRestProducts().getProducts().stream()
                .filter(IngProduct::isActiveLoanAccount)
                .map(product -> mapLoanAccount(product))
                .collect(Collectors.toList());
    }

    private LoanAccount mapLoanAccount(IngProduct product) {
        LoanDetails loanDetails;
        if (IngConstants.AccountTypes.MORTGAGE_ACCOUNT.equals(product.getType())) {
            loanDetails = mapLoanDetailsForMortgage(product);
        } else if (IngConstants.AccountTypes.LOAN_ACCOUNT.equals(product.getType())) {
            loanDetails = mapLoanDetailsForLoanAccount(product);
        } else {
            throw new IllegalStateException("Unrecognised loan product type");
        }

        // T.A.E stands for a Spanish term "La Tasa Anual Equivalente". Mortgages do not have this
        // value set but other
        // types of loans do.
        // Mortgages have the interest rate in a field called "nominalType". We use this if it is
        // available, and if not
        // we use the TAE rate
        Double interestRate = product.getNominalType();
        if (interestRate == null) {
            interestRate = product.getTaeAsDouble();
        }

        return LoanAccount.builder(product.getProductNumber())
                .setInterestRate(interestRate)
                .setDetails(loanDetails)
                .setAccountNumber(product.getProductNumber())
                .setExactBalance(
                        ExactCurrencyAmount.of(product.getBalance(), product.getCurrency()))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolders().get(0).getAnyName()))
                .setAccountNumber(product.getProductNumber())
                .setName(product.getName())
                .build();
    }

    private static LoanDetails mapLoanDetailsForMortgage(IngProduct product) {
        return LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                .setAmortized(
                        ExactCurrencyAmount.of(
                                product.getInitialAmount() - product.getPendingAmount(),
                                product.getCurrency()))
                .setInitialBalance(
                        ExactCurrencyAmount.of(product.getInitialAmount(), product.getCurrency()))
                .setMonthlyAmortization(
                        ExactCurrencyAmount.of(
                                product.getNextPaymentAmount(), product.getCurrency()))
                .setApplicants(
                        product.getHolders().stream()
                                .map(IngHolder::getAnyName)
                                .collect(Collectors.toList()))
                .setCoApplicant(product.getHolders().size() > 1)
                .setInitialDate(getDateIfPresent(product.getSignClientDate()))
                .setNextDayOfTermsChange(getDateIfPresent(product.getNextRevisionDate()))
                .setLoanNumber(product.getProductNumber())
                .build();
    }

    private static LoanDetails mapLoanDetailsForLoanAccount(IngProduct product) {
        return LoanDetails.builder(LoanDetails.Type.OTHER)
                .setAmortized(
                        ExactCurrencyAmount.of(product.getPaidAmount(), product.getCurrency()))
                .setInitialBalance(
                        ExactCurrencyAmount.of(product.getInitialAmount(), product.getCurrency()))
                .setMonthlyAmortization(
                        ExactCurrencyAmount.of(
                                product.getNextPayOffAmount(), product.getCurrency()))
                .setApplicants(
                        product.getHolders().stream()
                                .map(IngHolder::getAnyName)
                                .collect(Collectors.toList()))
                .setCoApplicant(product.getHolders().size() > 1)
                .setInitialDate(getDateIfPresent(product.getInitDate()))
                .setLoanNumber(product.getProductNumber())
                .build();
    }

    private static Date getDateIfPresent(String dateString) {
        return dateString == null ? null : IngUtils.toJavaLangDate(dateString);
    }
}
