package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import com.google.common.base.Preconditions;
import java.util.function.Predicate;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.ExternalRecipientEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.fetchers.transferdestination.rpc.TransferDestinationAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.PayeeEntity;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class SwedbankBasePredicates {
    private static final AccountIdentifierFormatter DEFAULT_FORMAT =
            new DefaultAccountIdentifierFormatter();

    public static Predicate<BankEntity> filterBankId(String bankId) {
        Preconditions.checkNotNull(bankId, "You must provide a bankId for comparison.");
        return bankEntity -> bankId.equalsIgnoreCase(bankEntity.getBankId());
    }

    public static Predicate<ExternalRecipientEntity> filterExternalRecipients(
            AccountIdentifier accountIdentifier) {
        return ere -> {
            AccountIdentifier ereAccountIdentifier = ere.generalGetAccountIdentifier();
            String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);

            if (ereAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                    ereAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
        };
    }

    public static Predicate<TransferDestinationAccountEntity> filterTransferDestinationAccounts(
            AccountIdentifier accountIdentifier) {

        return tdae -> {
            AccountIdentifier tdaeAccountIdentifier = tdae.generalGetAccountIdentifier();
            String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);

            if (tdaeAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                    tdaeAccountIdentifier.getIdentifier(DEFAULT_FORMAT));
        };
    }

    public static Predicate<PayeeEntity> filterPayees(AccountIdentifier accountIdentifier) {
        String originalAccountIdentifier = accountIdentifier.getIdentifier(DEFAULT_FORMAT);
        AccountIdentifierType originalAccountType = accountIdentifier.getType();

        return pe -> {
            AccountIdentifier peAccountIdentifier = pe.generalGetAccountIdentifier();

            if (peAccountIdentifier == null || originalAccountIdentifier == null) {
                return false;
            }

            return originalAccountIdentifier.equals(
                            peAccountIdentifier.getIdentifier(DEFAULT_FORMAT))
                    && originalAccountType.equals(peAccountIdentifier.getType());
        };
    }
}
