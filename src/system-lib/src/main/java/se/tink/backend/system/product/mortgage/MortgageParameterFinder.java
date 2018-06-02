package se.tink.backend.system.product.mortgage;

import se.tink.backend.core.User;

public interface MortgageParameterFinder {
    MortgageParameters findMortgageParameters(User user);
}
