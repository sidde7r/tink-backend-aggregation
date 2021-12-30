package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher;

import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data.FinTecSystemsReport;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.fetcher.data.ReportBaseData;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class FinTecSystemsReportMapper {

    public Optional<TransactionalAccount> transformReportToTinkAccount(FinTecSystemsReport report) {
        ReportBaseData baseData = report.getBaseData();
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withInferredAccountFlags()
                .withBalance(BalanceModule.of(ExactCurrencyAmount.zero("EUR")))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(baseData.getIban())
                                .withAccountNumber(baseData.getIban())
                                .withAccountName(baseData.getIban())
                                .addIdentifier(new IbanIdentifier(baseData.getIban()))
                                .build())
                .addParties(new Party(baseData.getHolder(), Role.HOLDER))
                .build();
    }
}
