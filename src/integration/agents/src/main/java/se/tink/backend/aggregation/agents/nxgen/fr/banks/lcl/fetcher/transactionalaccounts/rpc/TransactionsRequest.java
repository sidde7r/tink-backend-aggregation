package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.rpc;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.fetcher.transactionalaccounts.entities.AccountDetailsEntity;
import se.tink.backend.aggregation.nxgen.http.AbstractForm;

public class TransactionsRequest extends AbstractForm {

    private TransactionsRequest(String agency, String accountNumber, String cleLetter) {
        this.put(LclConstants.TransactionsRequest.AGENCY, agency);
        this.put(LclConstants.TransactionsRequest.ACCOUNT_NUMBER, accountNumber);
        this.put(LclConstants.TransactionsRequest.CLE_LETTER, cleLetter);
        this.put(LclConstants.TransactionsRequest.MODE_KEY, LclConstants.TransactionsRequest.MODE_VALUE);
        this.put(LclConstants.TransactionsRequest.TYPE_KEY, LclConstants.TransactionsRequest.TYPE_VALUE);
        this.put(LclConstants.TransactionsRequest.TAG_HTML_KEY, LclConstants.TransactionsRequest.TAG_HTML_VALUE);
        this.put(LclConstants.AuthenticationValuePairs.MOBILE.getKey(),
                LclConstants.AuthenticationValuePairs.MOBILE.getValue());
    }

    public static TransactionsRequest create(AccountDetailsEntity accountDetailsEntity) {
        String cleLetter = accountDetailsEntity.getClefLetter();

        return new TransactionsRequest(
                accountDetailsEntity.getAgency(), accountDetailsEntity.getAccountNumber() + cleLetter, cleLetter);
    }
}
