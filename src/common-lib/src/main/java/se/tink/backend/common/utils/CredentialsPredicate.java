package se.tink.backend.common.utils;

import java.util.function.Predicate;
import se.tink.backend.common.i18n.SocialSecurityNumber;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;

public class CredentialsPredicate extends se.tink.backend.utils.guavaimpl.predicates.CredentialsPredicate {
    public static final Predicate<Credentials> CREDENTIAL_HAS_SSN_USERNAME =
            credentials -> new SocialSecurityNumber.Sweden(credentials.getField(Field.Key.USERNAME)).isValid();
}
