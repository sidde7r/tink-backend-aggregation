package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile;

import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileEntity;
import se.tink.libraries.pair.Pair;

/* Selects all private profiles */
public class SwedbankPrivateProfileSelector implements SwedbankProfileSelector {

    @Override
    public List<Pair<BankEntity, ProfileEntity>> selectBankProfiles(List<BankEntity> banks) {
        return banks.stream()
                .map(bank -> new Pair<BankEntity, ProfileEntity>(bank, bank.getPrivateProfile()))
                .collect(Collectors.toList());
    }
}
