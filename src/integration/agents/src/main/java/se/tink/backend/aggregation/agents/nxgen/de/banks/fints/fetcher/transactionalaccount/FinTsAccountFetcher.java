package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsAccountInformation;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsDialogContext;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account.AccountClient;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.client.account.BalanceRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.account.FinTsTransactionalAccountMapper;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.FinTsResponse;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISAL;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HISPA;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.security.tan.SegmentType;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@Slf4j
@AllArgsConstructor
public class FinTsAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final FinTsDialogContext dialogContext;
    private final AccountClient accountClient;
    private final FinTsTransactionalAccountMapper mapper;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return getAccountsToMap().stream()
                .map(mapper::toTinkAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private List<FinTsAccountInformation> getAccountsToMap() {
        List<FinTsAccountInformation> transactionalAccounts = getTransactionalAccounts();
        if (transactionalAccounts.isEmpty()) {
            return Collections.emptyList();
        }
        fetchAdditionalAccountInformation(transactionalAccounts);
        fetchBalances(transactionalAccounts);
        return transactionalAccounts;
    }

    private List<FinTsAccountInformation> getTransactionalAccounts() {
        return dialogContext.getAccounts().stream()
                .filter(this::hasProperType)
                .collect(Collectors.toList());
    }

    private boolean hasProperType(FinTsAccountInformation accountInformation) {
        return accountInformation.getAccountType() == AccountTypes.CHECKING
                || accountInformation.getAccountType() == AccountTypes.SAVINGS;
    }

    private void fetchAdditionalAccountInformation(List<FinTsAccountInformation> accounts) {
        FinTsResponse sepaResponse = accountClient.getSepaDetailsForAllAccounts();
        HISPA hispa = sepaResponse.findSegmentThrowable(HISPA.class);
        List<HISPA.Detail> hispaDetails = hispa.getAccountDetails();

        for (FinTsAccountInformation accInfo : accounts) {
            accInfo.setSepaDetails(
                    hispaDetails.stream()
                            .filter(
                                    details ->
                                            detailsMatchBasicInfo(details, accInfo.getBasicInfo()))
                            .findFirst()
                            .orElse(null));
        }
    }

    private boolean detailsMatchBasicInfo(HISPA.Detail details, HIUPD basicInfo) {
        return Objects.equals(basicInfo.getAccountNumber(), details.getAccountNumber())
                && Objects.equals(basicInfo.getSubAccountNumber(), details.getSubAccountNumber())
                && Objects.equals(basicInfo.getCountryCode(), details.getCountryCode())
                && Objects.equals(basicInfo.getBlz(), details.getBlz());
    }

    private void fetchBalances(List<FinTsAccountInformation> accounts) {
        BalanceRequestBuilder requestBuilder =
                BalanceRequestBuilder.getRequestBuilder(dialogContext);

        for (FinTsAccountInformation accInfo : accounts) {
            if (accInfo.getBasicInfo().isOperationSupported(SegmentType.HKSAL)) {
                accInfo.setBalance(
                        accountClient
                                .getBalanceForAccount(requestBuilder, accInfo.getBasicInfo())
                                .findSegmentThrowable(HISAL.class));
            } else {
                log.warn(
                        "Could not find operation to retrieve balances for one of the accounts. Account type: {}",
                        accInfo.getAccountType());
            }
        }
    }
}
