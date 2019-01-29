package se.tink.libraries.enums;

import org.apache.commons.lang.NotImplementedException;
import se.tink.libraries.account.AccountIdentifier;

public enum SwedishGiroType {
    BG, PG, AG;

    public AccountIdentifier.Type toAccountIdentifierType() {
        switch (this) {
        case BG:
            return AccountIdentifier.Type.SE_BG;
        case PG:
            return AccountIdentifier.Type.SE_PG;
        default:
        case AG:
            throw new NotImplementedException();
        }
    }
}
