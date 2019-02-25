package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher;

import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngUtils;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.ProductsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Holder;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.responses.entities.Product;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanDetails;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class IngLoanAccountFetcher implements AccountFetcher<LoanAccount> {

    private final IngApiClient ingApiClient;
    private final SessionStorage sessionStorage;

    public IngLoanAccountFetcher(IngApiClient ingApiClient, SessionStorage sessionStorage) {
        this.ingApiClient = ingApiClient;
        this.sessionStorage = sessionStorage;
    }


    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<Product> products = this.sessionStorage
                .get(IngConstants.Tags.PRODUCT_LIST, ProductsResponse.class)
                .orElseGet(this.ingApiClient::getApiRestProducts)
                .getProducts();

        return products
                .stream()
                .filter(IngLoanAccountFetcher::loanAccountFilter)
                .map(IngLoanAccountFetcher::mapLoanAccount)
                .collect(Collectors.toList());
    }

    private static boolean loanAccountFilter(Product product) {
        boolean isLoanAccount =
                IngConstants.AccountCategories.LOANS.contains(product.getType());

        boolean isActive =
                IngConstants.AccountStatus.OPERATIVE.equals(product.getStatus().getCod());

        return isLoanAccount && isActive;
    }

    private static LoanAccount mapLoanAccount(Product product) {
        LoanDetails loanDetails;
        if (IngConstants.AccountTypes.MORTGAGE_ACCOUNT.equals(product.getType())) {
            loanDetails = mapLoanDetailsForMortgage(product);
        } else if (IngConstants.AccountTypes.LOAN_ACCOUNT.equals(product.getType())) {
            loanDetails = mapLoanDetailsForLoanAccount(product);
        } else {
            throw new IllegalStateException("Unrecognised loan product type");
        }

        // T.A.E stands for a Spanish term "La Tasa Anual Equivalente". Mortgages do not have this value set but other
        // types of loans do.
        // Mortgages have the interest rate in a field called "nominalType". We use this if it is available, and if not
        // we use the TAE rate
        Double interestRate = product.getNominalType();
        if (interestRate == null) {
            interestRate = product.getTaeAsDouble();
        }

        return LoanAccount.builder(product.getProductNumber())
                .setInterestRate(interestRate)
                .setDetails(loanDetails)
                .setAccountNumber(product.getProductNumber())
                .setBalance(new Amount(product.getCurrency(), product.getBalance()))
                .setBankIdentifier(product.getUuid())
                .setHolderName(new HolderName(product.getHolders().get(0).getAnyName()))
                .setAccountNumber(product.getProductNumber())
                .setName(product.getName())
                .build();
    }

    private static LoanDetails mapLoanDetailsForMortgage(Product product) {
        return LoanDetails.builder(LoanDetails.Type.MORTGAGE)
                .setAmortized(new Amount(product.getCurrency(), product.getInitialAmount() - product.getPendingAmount()))
                .setInitialBalance(new Amount(product.getCurrency(), product.getInitialAmount()))
                .setMonthlyAmortization(new Amount(product.getCurrency(), product.getNextPaymentAmount()))
                .setApplicants(product.getHolders().stream().map(Holder::getAnyName).collect(Collectors.toList()))
                .setCoApplicant(product.getHolders().size() > 1)
                .setInitialDate(IngUtils.toJavaLangDate(product.getSignClientDate()))
                .setNextDayOfTermsChange(IngUtils.toJavaLangDate(product.getNextRevisionDate()))
                .setLoanNumber(product.getProductNumber())
                .build();
    }

    private static LoanDetails mapLoanDetailsForLoanAccount(Product product) {
        return LoanDetails.builder(LoanDetails.Type.OTHER)
                .setAmortized(new Amount(product.getCurrency(), product.getPaidAmount()))
                .setInitialBalance(new Amount(product.getCurrency(), product.getInitialAmount()))
                .setMonthlyAmortization(new Amount(product.getCurrency(), product.getNextPayOffAmount()))
                .setApplicants(product.getHolders().stream().map(Holder::getAnyName).collect(Collectors.toList()))
                .setCoApplicant(product.getHolders().size() > 1)
                .setInitialDate(IngUtils.toJavaLangDate(product.getInitDate()))
                .setLoanNumber(product.getProductNumber())
                .build();
    }
}
