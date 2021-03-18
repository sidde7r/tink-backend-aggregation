package se.tink.libraries.enums;

import org.apache.commons.lang3.NotImplementedException;
import se.tink.libraries.account.enums.AccountIdentifierType;

public enum SwedishGiroType {
    BG,
    PG,
    AG;

    public AccountIdentifierType toAccountIdentifierType() {
        switch (this) {
            case BG:
                return AccountIdentifierType.SE_BG;
            case PG:
                return AccountIdentifierType.SE_PG;
            default:
            case AG:
                throw new NotImplementedException("Type AG is not implemented");
        }
    }
}
