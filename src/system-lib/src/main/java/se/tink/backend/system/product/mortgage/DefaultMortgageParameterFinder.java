package se.tink.backend.system.product.mortgage;

import com.google.inject.Inject;
import se.tink.backend.common.product.CredentialsMortgageAmountFinder;
import se.tink.backend.core.User;
import static se.tink.backend.core.property.PropertyType.HOUSE;

/**
 * Defaulting product parameters to:
 * - 1 applicant
 * - Market value based on mortgage being 70% of the property's value
 * - Mortgages based on connected credentials
 * - Default to HOUSE property if no address, otherwise pick from fraud details
 */
public class DefaultMortgageParameterFinder implements MortgageParameterFinder {
    private static final double DEFAULT_LOAN_TO_VALUE_RATE = 0.7;

    private final CredentialsMortgageAmountFinder mortgageAmountFinder;
    private final FraudDetailsPropertyTypeFinder fraudDetailsPropertyTypeFinder;

    @Inject
    public DefaultMortgageParameterFinder(
            CredentialsMortgageAmountFinder mortgageAmountFinder,
            FraudDetailsPropertyTypeFinder fraudDetailsPropertyTypeFinder) {
        this.mortgageAmountFinder = mortgageAmountFinder;
        this.fraudDetailsPropertyTypeFinder = fraudDetailsPropertyTypeFinder;
    }

    @Override
    public MortgageParameters findMortgageParameters(User user) {
        double mortgageAmount = mortgageAmountFinder.getMortgageAmount(user);

        return new MortgageParameters(
                fraudDetailsPropertyTypeFinder.findPropertyType(user).orElse(HOUSE),
                (int) (mortgageAmount / DEFAULT_LOAN_TO_VALUE_RATE),
                (int) mortgageAmount,
                1);
    }
}
