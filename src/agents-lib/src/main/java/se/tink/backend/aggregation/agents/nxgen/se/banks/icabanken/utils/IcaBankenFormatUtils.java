package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.utils;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.libraries.account.identifiers.formatters.AccountIdentifierFormatter;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class IcaBankenFormatUtils {

    public static final Splitter AND_SPLITTER = Splitter.on(IcaBankenConstants.IdTags.SWEDISH_AND_SEPARATOR)
            .trimResults();

    public static final Splitter WHITESPACE_SPLITTER = Splitter.on(CharMatcher.breakingWhitespace());

    public static final DefaultAccountIdentifierFormatter DEFAULT_FORMATTER = new DefaultAccountIdentifierFormatter();

    public static final AccountIdentifierFormatter ACCOUNT_IDENTIFIER_FORMATTER = new IcaBankenAccountIdentifierFormatter();
}
