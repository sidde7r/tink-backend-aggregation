package se.tink.libraries.enums;

import se.tink.libraries.account.AccountIdentifier;

public enum TransferType {
    EINVOICE,
    PAYMENT,
    BANK_TRANSFER;

    public static final String DOCUMENTED = "BANK_TRANSFER,PAYMENT";

    public static TransferType typeOf(AccountIdentifier accountIdentifier) {
        if (accountIdentifier != null) {
            switch (accountIdentifier.getType()) {
                case SE_BG:
                case SE_PG:
                    return PAYMENT;
                default:
                    // nothing
            }
        }

        return BANK_TRANSFER;
    }

    public static boolean accountIdentifierIsCompatibleWith(
            AccountIdentifier accountIdentifier, TransferType transferType) {

        for (AccountIdentifier.Type type : transferType.getCompatibleIdentifierTypes()) {
            if (accountIdentifier.getType().equals(type)) {
                return true;
            }
        }

        return false;
    }

    public AccountIdentifier.Type[] getCompatibleIdentifierTypes() {
        switch (this) {
            case EINVOICE:
            case PAYMENT:
                return new AccountIdentifier.Type[] {
                    AccountIdentifier.Type.SE_BG, AccountIdentifier.Type.SE_PG
                };
            case BANK_TRANSFER:
                return new AccountIdentifier.Type[] {
                    AccountIdentifier.Type.BE,
                    AccountIdentifier.Type.SEPA_EUR,
                    AccountIdentifier.Type.SE,
                    AccountIdentifier.Type.SE_SHB_INTERNAL,
                    AccountIdentifier.Type.TINK
                };
            default:
                throw new IllegalStateException("Not implemented, Developer should take action");
        }
    }
}
