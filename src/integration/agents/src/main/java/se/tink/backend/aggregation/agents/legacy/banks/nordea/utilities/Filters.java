package se.tink.backend.aggregation.agents.banks.nordea.utilities;

import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import se.tink.backend.aggregation.agents.banks.nordea.v15.model.ProductEntity;
import se.tink.backend.aggregation.agents.banks.nordea.v20.model.beneficiaries.BeneficiaryEntity;
import se.tink.libraries.account.AccountIdentifier;

public class Filters {

    private static final NordeaAccountIdentifierFormatter FORMATTER =
            new NordeaAccountIdentifierFormatter();

    public static Predicate<ProductEntity> productWithAccountNumber(final String accountNumber) {
        return entity -> Objects.equal(entity.getAccountNumber(true), accountNumber);
    }

    public static Predicate<ProductEntity> productThatCanPayWithAccountNumber(
            final String accountNumber) {
        return entity -> {
            boolean paymentAccount = entity.canMakePayment();
            return paymentAccount && Objects.equal(entity.getAccountNumber(true), accountNumber);
        };
    }

    public static Predicate<BeneficiaryEntity> beneficiariesWithAccountNumber(
            final String accountNumber) {
        return entity -> {
            String cleanedAccountNumber = entity.getCleanedAccountNumber();

            if (entity.isBankTransferEntity()) {
                AccountIdentifier identifier = entity.generalGetAccountIdentifier();
                if (identifier.isValid() && identifier.is(AccountIdentifier.Type.SE)) {
                    String beneficiaryAccountNumber;
                    try {
                        beneficiaryAccountNumber = FORMATTER.apply(identifier);
                        return accountNumber.equalsIgnoreCase(beneficiaryAccountNumber);
                    } catch (IllegalArgumentException e) {
                        return false; // Happens if the beneficiary account number is invalid.
                    }
                } else {
                    return accountNumber.equalsIgnoreCase(cleanedAccountNumber);
                }
            } else if (entity.isPaymentEntity()) {
                AccountIdentifier identifier = entity.toGiroIdentifier(cleanedAccountNumber);
                return accountNumber.equalsIgnoreCase(identifier.getIdentifier());
            } else {
                return false;
            }
        };
    }
}
