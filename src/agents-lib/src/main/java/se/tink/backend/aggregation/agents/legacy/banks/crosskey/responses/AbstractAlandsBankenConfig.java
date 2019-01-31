package se.tink.backend.aggregation.agents.banks.crosskey.responses;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.banks.crosskey.PaginationTypes;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.libraries.account.identifiers.IbanIdentifier;

public abstract class AbstractAlandsBankenConfig implements CrossKeyConfig {

    @Override
    public AccountTypes getAccountType(String accountGroup, String usageType) {
        if (Strings.nullToEmpty(usageType).toLowerCase().equals("saving")) {
            return AccountTypes.SAVINGS;
        }

        switch (Strings.nullToEmpty(accountGroup).toLowerCase()) {
            case "loan":
                return AccountTypes.LOAN;
            case "check":
                return AccountTypes.CHECKING;
            default:
                return AccountTypes.OTHER;
        }
    }

    public List<AccountIdentifier> getIdentifiers(String bic, String iban) {
        List<AccountIdentifier> identifiers = Lists.newArrayList();

        identifiers.add(new IbanIdentifier(bic, iban));

        return identifiers;
    }

    @Override
    public PaginationTypes getPaginationType() {
        return PaginationTypes.DATE;
    }
}
