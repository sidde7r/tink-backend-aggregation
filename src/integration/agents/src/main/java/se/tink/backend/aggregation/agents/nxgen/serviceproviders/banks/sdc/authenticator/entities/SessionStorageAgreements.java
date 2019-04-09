package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.authenticator.entities;

import java.util.ArrayList;

/*
 * Store agreements in session to be able to fetch accounts for all agreements during refresh
 */
public class SessionStorageAgreements extends ArrayList<SessionStorageAgreement> {

    public SessionStorageAgreement findAgreementForAccountBankId(String accountBankId) {
        return stream()
                .filter((a) -> a.hasAccountBankId(accountBankId))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No matching agreement"));
    }
}
