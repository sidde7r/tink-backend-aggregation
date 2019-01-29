
package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CollateralCreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.CreditDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc.FetchCreditsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpBankLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(OpBankLoanFetcher.class);

    private final OpBankApiClient client;
    private final Credentials credentials;

    public OpBankLoanFetcher(OpBankApiClient client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanAccount> loansAccounts = new ArrayList<>();

        FetchCreditsResponse fetchCreditsResponse = client.fetchCredits();

        for (OpBankCreditEntity credit : fetchCreditsResponse.getCredits()) {

            if (OpBankConstants.Fetcher.FLEXI_CREDIT.equalsIgnoreCase(credit.getCreditType())) {

                CreditDetailsResponse loanDetailsResponse = client
                        .fetchFlexiCreditDetails(credit.getEncryptedAgreementNumber());
                loansAccounts.add(loanDetailsResponse.toLoanAccount(credit));

            } else if (OpBankConstants.Fetcher.SPECIAL_CREDIT.equalsIgnoreCase(credit.getCreditType())) {

                CreditDetailsResponse loanDetailsResponse = client
                        .fetchSpecialCreditDetails(credit.getEncryptedAgreementNumber());
                loansAccounts.add(loanDetailsResponse.toLoanAccount(credit));

            } else if (OpBankConstants.Fetcher.COLLATERAL_CREDIT.equalsIgnoreCase(credit.getCreditType())) {

                CollateralCreditDetailsResponse response = client
                        .fetchCollateralCreditDetails(credit.getEncryptedAgreementNumber());
                loansAccounts.add(response.toLoanAccount(credit));

            } else {
                // continuing credit is handled as credit card (credit account) by the credit card fetcher
                if (!OpBankConstants.Fetcher.CONTINUING_CREDIT.equalsIgnoreCase(credit.getCreditType())) {
                    // log the last of them, haven seen it yet
                    LOGGER.infoExtraLong(SerializationUtils.serializeToString(credit),
                            OpBankConstants.Fetcher.LOAN_LOGGING);
                }
            }
        }

        return loansAccounts;
    }
}
