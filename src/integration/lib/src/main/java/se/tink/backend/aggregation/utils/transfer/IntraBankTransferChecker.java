package se.tink.backend.aggregation.utils.transfer;

import java.util.Optional;
import se.tink.libraries.account.AccountIdentifier;
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
}
