package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.CollateralsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.loan.rpc.LoanOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.EngagementOverviewResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LinksEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.LoanAccountEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.LoanAccount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class SwedbankDefaultLoanFetcher implements AccountFetcher<LoanAccount> {
    private static final Logger log = LoggerFactory.getLogger(SwedbankDefaultLoanFetcher.class);
    private final SwedbankDefaultApiClient apiClient;

    public SwedbankDefaultLoanFetcher(SwedbankDefaultApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        EngagementOverviewResponse engagementOverviewResponse = apiClient.engagementOverview();

        List<LoanAccountEntity> loanAccountEntities = engagementOverviewResponse.getLoanAccounts();

        if (loanAccountEntities == null) {
            return Collections.emptyList();
        }

        ArrayList<LoanAccount> loanAccounts = new ArrayList<>();

        for (LoanAccountEntity loanAccountEntity : loanAccountEntities) {
            LoanDetailsResponse loanDetailsResponse = apiClient.loanDetails(loanAccountEntity.getLinks().getNext());
            Optional<String> interest = loanDetailsResponse.getInterest();

            if (!interest.isPresent()) {
                continue;
            }

            loanAccountEntity.toLoanAccount(interest.get(), loanDetailsResponse.getDueDate().orElse(null))
                    .ifPresent(loanAccounts::add);
        }

        try {
            // Swedbank has endpoint with more loan information.
            // Currently we only know a part of this endpoints.
            // TODO: Implement and use this endpoint when we have information how all entities look
            String loanResponse = apiClient.loanOverview();
            if (!Strings.isNullOrEmpty(loanResponse)) {
                log.info("{}: {}", SwedbankBaseConstants.LogTags.LOAN_RESPONSE, loanResponse);

                LoanOverviewResponse loanOverviewResponse = SerializationUtils
                        .deserializeFromString(loanResponse, LoanOverviewResponse.class);
                LinksEntity links = loanOverviewResponse.getLinks();
                if (links != null) {
                    log.info("{}: {}", SwedbankBaseConstants.LogTags.MORTGAGE_OVERVIEW_RESPONSE,
                            apiClient.optionalRequest(links.getNext()));
                }

                List<CollateralsEntity> collaterals = loanOverviewResponse.getCollaterals();
                if (collaterals != null) {
                    collaterals.stream()
                            .map(CollateralsEntity::getLoans)
                            .flatMap(Collection::stream)
                            .map(LoanEntity::getLinks)
                            .map(LinksEntity::getNext)
                            .map(apiClient::optionalRequest)
                            .forEach(response -> log.info("{}; {}", SwedbankBaseConstants.LogTags.LOAN_DETAILS_RESPONSE,
                                    response));
                }
            }
        } catch (Exception e) {
            log.warn("Failed to fetch loan information", e);
        }


        return loanAccounts;
    }
}
