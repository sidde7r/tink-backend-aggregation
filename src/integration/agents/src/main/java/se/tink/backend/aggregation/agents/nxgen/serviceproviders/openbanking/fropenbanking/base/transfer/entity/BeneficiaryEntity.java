package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.transfer.entity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.libraries.account.AccountIdentifier;

@RequiredArgsConstructor
public class BeneficiaryEntity implements GeneralAccountEntity {

    private final AccountIdentifier accountIdentifier;

    private final String bank;

    private final String name;

    @Override
    public AccountIdentifier generalGetAccountIdentifier() {
        return accountIdentifier;
    }

    @Override
    public String generalGetBank() {
        return bank;
    }

    @Override
    public String generalGetName() {
        return name;
    }
}
