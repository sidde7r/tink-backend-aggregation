package se.tink.backend.aggregation.utils.transfer;

import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.account.identifiers.se.ClearingNumber;
import se.tink.libraries.account.identifiers.se.ClearingNumber.Details;

public class IntraBankTransferChecker {

    /**
     * Check if the transfer is made between the accounts in same bank (works for Swedish Market)
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isSwedishMarketIntraBankTransfer(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        boolean isIntraBank = false;
        if (AccountIdentifier.Type.SE.equals(destinationAccount.getType())) {
            Optional<Details> sourceAccountClearingNumber =
                    ClearingNumber.get(
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
     * Check if the transfer is made between the accounts in same bank
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isIntraBankTransfer(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        if (isSwedishMarketIntraBankTransfer(sourceAccount, destinationAccount)) {
            return true;
        } else if (isIbanIntraBankTransfer(sourceAccount, destinationAccount)) {
            return true;
        }
        return false;
    }

    /**
     * Check if the transfer is made between the accounts in same bank
     *
     * @param sourceAccount
     * @param destinationAccount
     * @return
     */
    public static boolean isIbanIntraBankTransfer(
            AccountIdentifier sourceAccount, AccountIdentifier destinationAccount) {
        if (!Type.IBAN.equals(destinationAccount.getType())
                && !Type.IBAN.equals(sourceAccount.getType())) {
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
