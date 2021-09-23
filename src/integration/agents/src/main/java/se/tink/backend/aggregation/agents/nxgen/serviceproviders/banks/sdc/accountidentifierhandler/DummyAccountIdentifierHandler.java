package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.accountidentifierhandler;

import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.OtherIdentifier;

public class DummyAccountIdentifierHandler implements SdcAccountIdentifierHandler {

    @Override
    public String convertToIban(String accountNumber) {
        return accountNumber;
    }

    @Override
    public List<AccountIdentifier> getIdentifiers(String rawAccountNumber) {
        return ImmutableList.of(new OtherIdentifier(rawAccountNumber));
    }
}
