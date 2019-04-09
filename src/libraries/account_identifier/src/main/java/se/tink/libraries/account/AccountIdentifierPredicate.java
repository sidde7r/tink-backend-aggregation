package se.tink.libraries.account;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import java.util.Objects;
import org.apache.http.NameValuePair;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

public class AccountIdentifierPredicate {
    public static final Predicate<NameValuePair> NAMEVALUEPAIR_NAME_PARAMETER =
            queryParameter -> {
                //noinspection SimplifiableIfStatement
                if (queryParameter == null
                        || Strings.isNullOrEmpty(queryParameter.getName())
                        || Strings.isNullOrEmpty(queryParameter.getValue())) {
                    return false;
                }

                return Objects.equals(queryParameter.getName(), "name");
            };

    public static final Predicate<AccountIdentifier> IS_VALID_SWEDISHIDENTIFIER =
            new Predicate<AccountIdentifier>() {
                @Override
                public boolean apply(AccountIdentifier accountIdentifier) {
                    return IS_VALID.apply(accountIdentifier)
                            && accountIdentifier.is(AccountIdentifier.Type.SE);
                }
            };

    public static final Predicate<AccountIdentifier> IS_VALID =
            accountIdentifier -> accountIdentifier != null && accountIdentifier.isValid();

    public static final Function<AccountIdentifier, String> SWEDISHIDENTIFIER_TO_BANKDISPLAYNAME =
            accountIdentifier -> {
                SwedishIdentifier swedishIdentifier = accountIdentifier.to(SwedishIdentifier.class);
                if (swedishIdentifier.getBank() != null) {
                    return swedishIdentifier.getBankName();
                }

                return null;
            };
}
