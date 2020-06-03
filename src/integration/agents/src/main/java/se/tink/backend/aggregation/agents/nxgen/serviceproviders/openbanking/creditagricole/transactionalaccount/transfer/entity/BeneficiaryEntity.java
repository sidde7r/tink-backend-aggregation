package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.transfer.entity;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.general.models.GeneralAccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities.BeneficiaryDto;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class BeneficiaryEntity implements GeneralAccountEntity {

    private final AccountIdentifier accountIdentifier;

    private final String bank;

    private final String name;

    public static BeneficiaryEntity from(BeneficiaryDto beneficiaryDto) {
        return new BeneficiaryEntity(
                new IbanIdentifier(beneficiaryDto.getCreditorAccount().getIban()),
                beneficiaryDto.getCreditorAgent().getBicFi(),
                beneficiaryDto.getCreditor().getName());
    }

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
