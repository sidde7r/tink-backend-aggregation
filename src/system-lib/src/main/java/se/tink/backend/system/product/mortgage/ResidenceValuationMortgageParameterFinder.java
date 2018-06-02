package se.tink.backend.system.product.mortgage;

import com.google.common.collect.FluentIterable;
import com.google.inject.Inject;
import java.util.Map;
import java.util.Optional;
import se.tink.backend.common.repository.mysql.main.AccountRepository;
import se.tink.backend.common.repository.mysql.main.PropertyRepository;
import se.tink.backend.core.Account;
import se.tink.backend.core.User;
import se.tink.backend.core.property.Property;
import se.tink.backend.utils.LogUtils;

public class ResidenceValuationMortgageParameterFinder implements MortgageParameterFinder {
    private final AccountRepository accountRepository;
    private final PropertyRepository propertyRepository;
    private static final LogUtils log = new LogUtils(ResidenceValuationMortgageParameterFinder.class);

    @Inject
    public ResidenceValuationMortgageParameterFinder(
            PropertyRepository propertyRepository,
            AccountRepository accountRepository) {
        this.propertyRepository = propertyRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public MortgageParameters findMortgageParameters(User user) {
        Optional<Property> firstProperty = propertyRepository.findByUserId(user.getId()).stream().findFirst();

        if (!firstProperty.isPresent()) {
            return new MortgageParameters();
        }

        MortgageParameters mortgageParameters = new MortgageParameters();
        mortgageParameters.setMortgageAmount(getMortgageAmount(firstProperty.get()));
        mortgageParameters.setPropertyType(firstProperty.get().getType());
        // TODO: add market value when populated on property

        return mortgageParameters;
    }

    private Integer getMortgageAmount(Property firstProperty) {

        String userId = firstProperty.getUserId();

        Map<String, Account> accountById = FluentIterable
                .from(accountRepository.findByUserId(userId))
                .uniqueIndex(Account::getId);

        double mortgageAmount = 0;

        for (String accountId : firstProperty.getLoanAccountIds()) {

            Account account = accountById.get(accountId);

            if (account == null) {
                // This should never happen, since the mortgages are selected (and verified) from a list based
                // on the user's accounts.
                log.error(userId, String.format("The mortgage account doesn't exist [accountId:%s].", accountId));
                continue;
            }

            mortgageAmount += Math.abs(account.getBalance());
        }

        return (int) mortgageAmount;
    }
}
