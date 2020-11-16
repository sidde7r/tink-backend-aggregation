package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank.profile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankProfileSelector;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BankEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.BusinessProfileEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc.ProfileEntity;
import se.tink.libraries.pair.Pair;

/* Selects the profile matching a given organization number, or throws login error */
public class SwedbankBusinessProfileSelector implements SwedbankProfileSelector {

    private final String organizationNumber;

    public SwedbankBusinessProfileSelector(String organizationNumber) {
        this.organizationNumber = organizationNumber;
    }

    @Override
    public List<Pair<BankEntity, ProfileEntity>> selectBankProfiles(List<BankEntity> banks) {
        for (BankEntity bank : banks) {
            final Optional<BusinessProfileEntity> profile =
                    bank.getBusinessProfile(organizationNumber);
            if (profile.isPresent()) {
                return Collections.singletonList(
                        new Pair<BankEntity, ProfileEntity>(bank, profile.get()));
            }
        }
        throw LoginError.INCORRECT_CREDENTIALS.exception(
                "No business profile matched organisation number provider by user.");
    }

    @Override
    public boolean hasPayments() {
        return false;
    }
}
