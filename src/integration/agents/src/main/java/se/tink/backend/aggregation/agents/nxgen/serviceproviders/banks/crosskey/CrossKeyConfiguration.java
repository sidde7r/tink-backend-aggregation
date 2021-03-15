package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyTransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.loan.entities.LoanDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.rpc.IdentityDataResponse;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.identitydata.IdentityData;

public abstract class CrossKeyConfiguration implements ClientConfiguration {

    public abstract String getBaseUrl();

    public abstract Optional<TransactionalAccount> parseTransactionalAccount(
            CrossKeyAccount crossKeyAccount);

    public abstract Transaction parseTinkTransaction(CrossKeyTransaction crossKeyTransaction);

    public abstract LoanAccount parseLoanAccount(
            CrossKeyAccount account, LoanDetailsEntity loanDetailsEntity);

    public abstract InvestmentAccount parseInvestmentAccount(
            CrossKeyAccount account, Portfolio portfolio);

    public IdentityData parseIdentityData(final IdentityDataResponse identityResponse) {
        if (identityResponse.isFailure()) {
            return null;
        }

        return IdentityData.builder()
                .addFirstNameElement(identityResponse.getFirstName())
                .addSurnameElement(identityResponse.getLastName())
                .setDateOfBirth(null)
                .build();
    }

    protected boolean noContent(String s) {
        return Strings.nullToEmpty(s).trim().isEmpty();
    }

    protected boolean hasContent(String s) {
        return !noContent(s);
    }

    protected Optional<String> getAppVersion() {
        return Optional.empty();
    }
}
