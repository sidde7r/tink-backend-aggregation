package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class AccountDetailsRequest extends AbstractForm {

    private AccountDetailsRequest(String accountNumber) {
        this.put(LclConstants.AccountDetailsRequest.ACCOUNT, accountNumber);
        this.put(LclConstants.AuthenticationValuePairs.MOBILE.getKey(),
                LclConstants.AuthenticationValuePairs.MOBILE.getValue());
    }

    public static AccountDetailsRequest create(String agency, String accountNumber, String cleLetter) {
        return new AccountDetailsRequest(formatAccountNumber(agency, accountNumber, cleLetter));
    }

    private static String formatAccountNumber(String agency, String accountNumber, String cleLetter) {
        return String.format("%s/%s/%s", agency, accountNumber, cleLetter);
    }
}
