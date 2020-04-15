package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.clientchoice;

import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChosenSecurityFunctionProvider {

    public static String getChosenSecurityFunction(FinTsDialogContext context) {
        List<String> securityFunctions = context.getAllowedSecurityFunctions();
        // currently we choose the first security function by default - no need to ask user
        return securityFunctions.get(0);
    }
}
