package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.BanquePopulaireConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.entities.ContractOverviewEntity;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.fetcher.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.rpc.ContractsResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BanquePopulaireLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(BanquePopulaireLoanFetcher.class);

    private final BanquePopulaireApiClient apiClient;

    public BanquePopulaireLoanFetcher(BanquePopulaireApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        List<LoanAccount> loans = new ArrayList<>();

        ContractsResponse contractsResponse = apiClient.getAllContracts();
        contractsResponse.stream()
                .filter(this::filterLoanAccounts)
                .forEach(account -> {
                    LoanDetailsResponse loanDetails =
                            apiClient.getLoanAccountDetails(account.createContractBankIdentifier());
                    LoanAccount loan = loanDetails.toTinkLoanAccount(account);

                    // log loans we haven't seen before
                    if (account.isUnhandledLoanType()) {
                        LOGGER.infoExtraLong(SerializationUtils.serializeToString(account),
                                BanquePopulaireConstants.LogTags.UNKNOWN_LOAN_TYPE);
                        LOGGER.infoExtraLong(SerializationUtils.serializeToString(loanDetails),
                                BanquePopulaireConstants.LogTags.UNKNOWN_LOAN_TYPE);
                    } else {
                        loans.add(loan);
                    }
                });

        return loans;
    }

    private boolean filterLoanAccounts(ContractOverviewEntity contract) {
        // there are many types of contracts, do not log the ones we are absolutely not interested in
        if (contract.isNonAccountContract()) {
            return false;
        }
        // log unknown contract types to see what we have to work with
        if (contract.isUnknownContractType()) {
            LOGGER.infoExtraLong(SerializationUtils.serializeToString(contract),
                    BanquePopulaireConstants.LogTags.UNKNOWN_ACCOUNT_TYPE);
            return false;
        }

        return (contract.getTinkAccountType() == AccountTypes.LOAN);
    }
}
