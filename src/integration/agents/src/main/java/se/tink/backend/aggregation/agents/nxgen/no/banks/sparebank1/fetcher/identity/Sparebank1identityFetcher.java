package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity;

import java.time.LocalDate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Identity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity.entities.IdentityDataEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class Sparebank1identityFetcher implements IdentityDataFetcher {
    private static final Pattern DATE_OF_BIRTH_PATTERN =
            Pattern.compile("(\\d{2})(\\d{2})(\\d{2})");
    private final PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        IdentityDataEntity identity = Sparebank1Identity.loadIdentityData(persistentStorage);

        return IdentityData.builder()
                .setFullName(identity.getFullName())
                .setDateOfBirth(mapObfuscatedSsnToDOB(identity.getObfuscatedSsn()))
                .build();
    }

    private LocalDate mapObfuscatedSsnToDOB(String ssn) {
        String dob = ssn.substring(0, 6);
        Matcher matcher = DATE_OF_BIRTH_PATTERN.matcher(dob);
        if (!matcher.matches()) {
            return null;
        }
        String day = matcher.group(1);
        String month = matcher.group(2);
        String year = matcher.group(3);
        return LocalDate.of(
                assumeCenturyOfYear(Integer.parseInt(year)),
                Integer.parseInt(month),
                Integer.parseInt(day));
    }

    private int assumeCenturyOfYear(int year) {
        int yearNow = LocalDate.now().getYear();
        int numberOfCenturies = yearNow / 100;
        int resultInCurrentCentury = numberOfCenturies * 100 + year;
        if (resultInCurrentCentury >= yearNow) {
            return (numberOfCenturies - 1) * 100 + year;
        }
        return resultInCurrentCentury;
    }
}
