package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.authenticator.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.authenticator.rpc.AccountPermissionResponseV31;
import se.tink.backend.aggregation.nxgen.http.URL;

public class UkOpenBankingV31Configuration extends UkOpenBankingBaseConfiguration {
    @Override
    public String getIntentId(AccountPermissionResponse accountPermissionResponse) {
        AccountPermissionResponseV31 accountPermissionResponseV31 =
                (AccountPermissionResponseV31) accountPermissionResponse;
        return accountPermissionResponseV31.getData().getAccountConsentId();
    }

    @Override
    public URL createConsentRequestURL(URL authBaseURL) {
        return authBaseURL
                .concat(UkOpenBankingV31Constants.ApiServices.AISP_PREFIX)
                .concat(UkOpenBankingV31Constants.ApiServices.CONSENT_REQUEST);
    }
}
