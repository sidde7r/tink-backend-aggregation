package se.tink.backend.aggregation.nxgen.core.account.nxbuilders;

import se.tink.backend.aggregation.nxgen.core.account.TriTypeMapper;
import se.tink.libraries.account.enums.AccountFlag;

public interface WithFlagPolicyStep<T, Mapper extends TriTypeMapper> {

    T withFlagsFrom(Mapper mapper, String typeKey);

    T withInferredAccountFlags();

    T withPaymentAccountFlag();

    T withFlags(AccountFlag... flags);

    T withoutFlags();
}
