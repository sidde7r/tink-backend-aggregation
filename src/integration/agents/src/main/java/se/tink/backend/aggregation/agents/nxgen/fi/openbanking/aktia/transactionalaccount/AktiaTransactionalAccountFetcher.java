package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.AktiaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.dto.response.AccountSummaryDto;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.apiclient.response.AccountsSummaryResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.transactionalaccount.converter.AktiaTransactionalAccountConverter;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RequiredArgsConstructor
public class AktiaTransactionalAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final AktiaApiClient aktiaApiClient;

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        final AccountsSummaryResponse accountsSummaryResponse = aktiaApiClient.getAccountsSummary();
        validateAccountsSummaryResponse(accountsSummaryResponse);

        final AccountSummaryDto accountSummaryDto =
                accountsSummaryResponse.getAccountsSummaryResponseDto().getAccountSummary();

        return accountSummaryDto.getAccountSummaryList().stream()
                .map(AktiaTransactionalAccountConverter::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private static void validateAccountsSummaryResponse(
            AccountsSummaryResponse accountsSummaryResponse) {
        if (!accountsSummaryResponse.isSuccessful()) {
            throw new IllegalArgumentException("Fetching accounts failed.");
        }
    }
}
