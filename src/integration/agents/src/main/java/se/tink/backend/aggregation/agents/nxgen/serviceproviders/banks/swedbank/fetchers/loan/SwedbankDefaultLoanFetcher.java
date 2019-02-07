package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.BankProfile;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LoanAccountEntity;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final AggregationLogger LOGGER = new AggregationLogger(SwedbankDefaultLoanFetcher.class);
    protected final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultLoanFetcher(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {

        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

        for (BankProfile bankProfile : apiClient.getBankProfiles()) {
            apiClient.selectProfile(bankProfile);
            EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();

            List<LoanAccountEntity> loanAccountEntities = engagementOverviewResponse.getLoanAccounts();

            // check if user has loans, if not, continue
            if (loanAccountEntities == null || loanAccountEntities.size() < 1) {
                continue;
            }

            for (LoanAccountEntity loanAccountEntity : loanAccountEntities) {
                if (loanAccountEntity.getLinks() == null || loanAccountEntity.getLinks().getNext() == null) {
                    continue;
                }

                // we get error sometimes from swedbank backend, log and continue as old agent did
                // log to check if only error for 0 balance loans
                LoanDetailsResponse loanDetailsResponse = null;
                try {
                    loanDetailsResponse = apiClient.loanDetails(loanAccountEntity.getLinks().getNext());
                } catch (Exception e) {
                    LOGGER.warnExtraLong(SerializationUtils.serializeToString(loanAccountEntity),
                            SwedbankBaseConstants.LogTags.LOAN_DETAILS_ERROR, e);
                    continue;
                }

                Optional<String> interest = loanDetailsResponse.getInterest();

                if (!interest.isPresent()) {
                    continue;
                }

                loanAccountEntity.toLoanAccount(interest.get(), loanDetailsResponse.getDueDate().orElse(null))
                        .ifPresent(loanAccounts::add);
            }

            // log loan info for endpoints we do not yet have full info for
            logLoanInfo();
        }

        return loanAccounts;
    }

    private void logLoanInfo() {
        try {
            // Swedbank has endpoint with more loan information.
            // Currently we only know a part of this endpoints.
            // TODO: Implement and use this endpoint when we have information how all entities look
            String loanResponse = apiClient.loanOverviewAsString();
            if (!Strings.isNullOrEmpty(loanResponse)) {
                LOGGER.infoExtraLong(loanResponse, SwedbankBaseConstants.LogTags.LOAN_RESPONSE);

                LoanOverviewResponse loanOverviewResponse = SerializationUtils
                        .deserializeFromString(loanResponse, LoanOverviewResponse.class);
                LinksEntity links = loanOverviewResponse.getLinks();
                if (links != null && links.getNext() != null) {
                    LOGGER.infoExtraLong(apiClient.optionalRequest(links.getNext()),
                            SwedbankBaseConstants.LogTags.MORTGAGE_OVERVIEW_RESPONSE);
                }

                List<CollateralsEntity> collaterals = loanOverviewResponse.getCollaterals();
                if (collaterals != null) {
                    for (CollateralsEntity collateral : collaterals) {
                        logLoanDetails(collateral.getLoans(), "Collateral");
                    }
                }
                logLoanDetails(loanOverviewResponse.getCarLoans(), "CarLoans");
                logLoanDetails(loanOverviewResponse.getConsumptionLoans(), "ConsumptionLoans");
                logLoanDetails(loanOverviewResponse.getOngoingLoans(), "OngoingLoans");
                logLoanDetails(loanOverviewResponse.getMortgageLoanCommitments(), "MortgageLoanCommitments");
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s Failed to fetch loan information",
                    SwedbankBaseConstants.LogTags.LOAN_DETAILS_RESPONSE.toString()), e);
        }
    }

    private void logLoanDetails(List<LoanEntity> loans, String loanType) {
        try {
            if (loans != null) {
                loans.stream()
                        .filter(loanEntity -> loanEntity.getLinks() != null)
                        .map(LoanEntity::getLinks)
                        .filter(linksEntity -> linksEntity.getNext() != null)
                        .map(LinksEntity::getNext)
                        .map(apiClient::optionalRequest)
                        .forEach(response ->
                                LOGGER.infoExtraLong(String.format("%s %s",
                                        loanType,
                                        response), SwedbankBaseConstants.LogTags.LOAN_DETAILS_RESPONSE));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s Failed to fetch loan [%s] information",
                    SwedbankBaseConstants.LogTags.LOAN_DETAILS_RESPONSE.toString(),
                    loanType),
                    e);
        }
    }
}
