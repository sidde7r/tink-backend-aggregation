package se.tink.backend.aggregation.utils.accountidentifier;

import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Details;

public class IntraBankChecker {

    /**
     * Check if the account identifiers belong to the same bank (works for Swedish Market)
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isSwedishMarketIntraBank(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        boolean isIntraBank = false;
        if (AccountIdentifierType.SE.equals(destinationAccount.getType())) {
            Optional<Details> sourceAccountClearingNumber =
                    AccountIdentifierType.IBAN.equals(sourceAccount.getType())
                            ? ClearingNumber.get(
                                    new SwedishIdentifier(
                                                    sourceAccount
                                                            .to(IbanIdentifier.class)
                                                            .getBban())
                                            .getClearingNumber())
                            : ClearingNumber.get(
                                    sourceAccount.to(SwedishIdentifier.class).getClearingNumber());

            Optional<Details> destinationAccountClearingNumber =
                    ClearingNumber.get(
                            destinationAccount.to(SwedishIdentifier.class).getClearingNumber());

            isIntraBank =
                    sourceAccountClearingNumber.isPresent()
                            && destinationAccountClearingNumber.isPresent()
                            && (sourceAccountClearingNumber
                                    .get()
                                    .getBankName()
                                    .equalsIgnoreCase(
                                            destinationAccountClearingNumber.get().getBankName()));
        }
        return isIntraBank;
    }

    /**
     * Check if the account identifiers belong to the same bank
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isAccountIdentifierIntraBank(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        if (isSwedishMarketIntraBank(sourceAccount, destinationAccount)) {
            return true;
        } else if (isIbanIntraBank(sourceAccount, destinationAccount)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the account identifiers belong to the same bank
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isIbanIntraBank(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        if (!AccountIdentifierType.IBAN.equals(destinationAccount.getType())
                && !AccountIdentifierType.IBAN.equals(sourceAccount.getType())) {
            return false;
        }
        IbanIdentifier source = sourceAccount.to(IbanIdentifier.class);
        IbanIdentifier dest = destinationAccount.to(IbanIdentifier.class);
        if (source.getBankCode().equalsIgnoreCase(dest.getBankCode())) {
            return true;
        }
        return false;
    }
}
