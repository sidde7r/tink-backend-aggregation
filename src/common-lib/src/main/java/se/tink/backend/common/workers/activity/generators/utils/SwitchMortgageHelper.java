package se.tink.backend.common.workers.activity.generators.utils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import java.util.List;
import java.util.Objects;
import se.tink.backend.common.repository.cassandra.LoanDataRepository;
import se.tink.backend.common.workers.activity.ActivityGeneratorContext;
import se.tink.backend.core.Account;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.Loan;
import se.tink.backend.core.User;
import se.tink.backend.core.product.ProductArticle;
import se.tink.backend.core.product.ProductPropertyKey;
import se.tink.backend.utils.guavaimpl.Orderings;
import se.tink.backend.utils.guavaimpl.Predicates;
import se.tink.backend.utils.guavaimpl.predicates.AccountPredicate;
import se.tink.credentials.demo.DemoCredentials;

public class SwitchMortgageHelper {

    private LoanDataRepository loanDataRepository;
    private User user;

    public SwitchMortgageHelper(ActivityGeneratorContext context){
        loanDataRepository = context.getServiceContext().getRepository(LoanDataRepository.class);
        user = context.getUser();
    }

    public boolean canSaveMoneyBySwitchingMortgageProvider(List<Account> accounts, List<Credentials> credentialsList, List<ProductArticle> products) {


        if (accounts == null || accounts.isEmpty()) {
            return false;
        }

        if (credentialsList == null || credentialsList.isEmpty()) {
            return false;
        }

        FluentIterable<ProductArticle> productArticles = FluentIterable
                .from(products)
                .filter(Predicates.PRODUCT_ARTICLE_WITH_INTEREST_RATE);

        if (productArticles.isEmpty()) {
            return false;
        }

        ProductArticle productArticleWithLowestInterestRate = productArticles.toList().stream()
                .min(Orderings.PRODUCTS_BY_INTEREST).get();

        double lowestAvailableInterestRate = ((Number) productArticleWithLowestInterestRate
                .getProperty(ProductPropertyKey.INTEREST_RATE)).doubleValue();

        ImmutableListMultimap<String, Account> accountsByCredentialsId = FluentIterable.from(accounts)
                .filter(AccountPredicate.IS_NOT_EXCLUDED)
                .filter(AccountPredicate.IS_NOT_CLOSED)
                .filter(Predicates.or(AccountPredicate.IS_LOAN, AccountPredicate.IS_MORTGAGE))
                .index(Account::getCredentialsId);

        if (accountsByCredentialsId.isEmpty()) {
            return false;
        }

        String ssn = user.getProfile().getFraudPersonNumber();

        for (Credentials credentials : credentialsList) {
            // Ignore credentials that have a different username than the user's SSN.
            if (!Objects.equals(ssn, credentials.getField(Field.Key.USERNAME))) {
                continue;
            }

            if (DemoCredentials.isDemoUser(credentials.getField(Field.Key.USERNAME))) {
                continue;
            }

            double loanAmount = 0;
            double amountInterestSumProduct = 0;

            for (Account a : accountsByCredentialsId.get(credentials.getId())) {

                Loan loan = loanDataRepository.findMostRecentOneByAccountId(a.getId());

                if (loan != null && Objects.equals(loan.getType(), Loan.Type.MORTGAGE) && loan.getBalance() != null
                        && loan.getInterest() != null) {
                    loanAmount += Math.abs(loan.getBalance());
                    amountInterestSumProduct += Math.abs(loan.getBalance()) * loan.getInterest();
                }
            }

            if (loanAmount > 0) {
                double currentInterestRate = amountInterestSumProduct / loanAmount;
                if (currentInterestRate > lowestAvailableInterestRate) {
                    return true;
                }
            }
        }

        return false;
    }

}
