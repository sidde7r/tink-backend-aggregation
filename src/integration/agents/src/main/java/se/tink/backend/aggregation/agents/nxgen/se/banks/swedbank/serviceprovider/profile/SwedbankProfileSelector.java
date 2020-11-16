package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileEntity;
import se.tink.libraries.pair.Pair;

public interface SwedbankProfileSelector {
    List<Pair<BankEntity, ProfileEntity>> selectBankProfiles(List<BankEntity> banks);

    boolean hasPayments();
}
