package se.tink.backend.aggregation.agents.nxgen.dk.banks.danskebank.fetcher.identitydata;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.authenticator.rpc.FinalizeAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@AllArgsConstructor
public class DanskeBankDKIdentityFetcher implements IdentityDataFetcher {
    private PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return persistentStorage
                .get(Storage.IDENTITY_INFO, FinalizeAuthenticationResponse.class)
                .map(
                        user ->
                                IdentityData.builder()
                                        .setSsn(user.getUserId())
                                        .addFirstNameElement(user.getUserInfo().getFirstName())
                                        .addSurnameElement(user.getUserInfo().getLastname())
                                        .setDateOfBirth(parseDateOfBirth(user.getUserId()))
                                        .build())
                .orElseThrow(NoSuchElementException::new);
    }

    private LocalDate parseDateOfBirth(String userId) {
        final int day = Integer.parseInt(userId.substring(0, 2));
        final int month = Integer.parseInt(userId.substring(2, 4));
        final int shortYear = Integer.parseInt(userId.substring(4, 6));
        final int yearCenturyPart = Integer.parseInt(userId.substring(6, 7));

        final int year = calculateYear(shortYear, yearCenturyPart);

        return LocalDate.of(year, month, day);
    }

    // https://da.wikipedia.org/wiki/CPR-nummer#Under_eller_over_100_%C3%A5r
    private int calculateYear(int shortYear, int yearCenturyPart) {
        int century;
        if (yearCenturyPart >= 0 && yearCenturyPart <= 3) {
            century = 1900;
        } else if (yearCenturyPart == 4 || yearCenturyPart == 9) {
            if (shortYear >= 0 && shortYear <= 36) {
                century = 2000;
            } else {
                century = 1900;
            }
        } else {
            century = 2000;
        }
        return century + shortYear;
    }
}
