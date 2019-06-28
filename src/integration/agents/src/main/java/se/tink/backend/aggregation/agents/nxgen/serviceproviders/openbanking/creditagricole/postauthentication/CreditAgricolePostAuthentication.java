package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.postauthentication;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleApiClient;

public class CreditAgricolePostAuthentication {

    public void getAdditionalParamsPostAuthentication(CreditAgricoleApiClient apiClient)
            throws SessionException {
        apiClient.getUserIdIntoSession();
    }
}
