package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.revolut.common;

import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;

@Slf4j
public class RevolutAccountIdentifierFilter {

    private RevolutAccountIdentifierFilter() {}

    public static List<AccountIdentifierEntity> getFilteredAccountIdentifiers(
            AccountEntity account) {
        return account.getIdentifiers().stream()
                .filter(RevolutAccountIdentifierFilter::supportedAccountTypes)
                .distinct()
                .collect(Collectors.toList());
    }

    // This method was introduced to ignore accounts with US.RoutingNumberAccountNumber
    // SchemeName. This should be removed whenever we will start to support this account type. For
    // the same account (with the same accountId) there is always IBAN and
    // RoutingNumberAccountNumber, so we will use IBAN as identifier for those accounts.
    private static boolean supportedAccountTypes(AccountIdentifierEntity accId) {
        if (accId.getIdentifierType()
                == UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.NOT_SUPPORTED) {
            log.info(
                    "[RevolutTransactionalAccountMapper] Account identifier {} is not supported. Ignoring this identifier.",
                    accId.getIdentifierType());
            return false;
        }
        return true;
    }
}
