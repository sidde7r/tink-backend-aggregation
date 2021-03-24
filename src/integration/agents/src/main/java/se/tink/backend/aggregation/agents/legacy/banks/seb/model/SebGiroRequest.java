package se.tink.backend.aggregation.agents.banks.seb.model;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.formatters.DefaultAccountIdentifierFormatter;

public class SebGiroRequest extends SebRequest {

    private static final String BG_VARIABLE_NAME = "BG_NR";
    private static final String PG_VARIABLE_NAME = "PG_NR";

    public static SebGiroRequest create(AccountIdentifier bgOrPgDestination) {
        SebGiroRequest sebGiroRequest = new SebGiroRequest();

        List<ServiceInput> serviceInput = createServiceInput(bgOrPgDestination);
        sebGiroRequest.request.ServiceInput.addAll(serviceInput);

        return sebGiroRequest;
    }

    private static List<ServiceInput> createServiceInput(AccountIdentifier bgOrPgDestination) {
        String name = getName(bgOrPgDestination);
        String searchFormattedDestination = getFormattedVariable(bgOrPgDestination);

        List<ServiceInput> serviceInputs = Lists.newArrayList();
        serviceInputs.add(new ServiceInput(name, searchFormattedDestination));

        if (bgOrPgDestination.is(AccountIdentifierType.SE_PG)) {
            serviceInputs.add(new ServiceInput("TERM_CLNR", "5695"));
        }

        return serviceInputs;
    }

    private static String getName(AccountIdentifier bgOrPgDestination) {
        if (bgOrPgDestination.is(AccountIdentifierType.SE_BG)) {
            return BG_VARIABLE_NAME;
        } else {
            return PG_VARIABLE_NAME;
        }
    }

    private static String getFormattedVariable(AccountIdentifier bgOrPgDestination) {
        return Strings.padStart(
                bgOrPgDestination.getIdentifier(new DefaultAccountIdentifierFormatter()), 10, '0');
    }
}
