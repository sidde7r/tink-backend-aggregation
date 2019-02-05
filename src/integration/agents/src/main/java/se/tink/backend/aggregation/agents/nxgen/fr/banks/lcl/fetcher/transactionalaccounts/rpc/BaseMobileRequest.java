package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class BaseMobileRequest extends AbstractForm {

    private BaseMobileRequest() {
        this.put(LclConstants.AuthenticationValuePairs.MOBILE.getKey(),
                LclConstants.AuthenticationValuePairs.MOBILE.getValue());
    }

    public static BaseMobileRequest create() {
        return new BaseMobileRequest();
    }
}
