package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.transactionalaccount.entities;

import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities.AccountIdentificationDto;

@Getter
public class AccountIdentificationEntity extends AccountIdentificationDto {

    private AccountIdentificationOtherEntity other;
}
