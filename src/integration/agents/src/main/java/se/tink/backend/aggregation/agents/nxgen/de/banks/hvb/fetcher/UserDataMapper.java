package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.identitydata.IdentityData;

public class UserDataMapper {

    private static final Pattern BANKING_NUMBER_PATTERN =
            Pattern.compile("\\d{4}(\\d{2})(\\d{2})(\\d{2})"); // nnnnDDMMYY

    public IdentityData toIdentityData(UserDataResponse userDataResponse) {
        return IdentityData.builder()
                .addFirstNameElement(userDataResponse.getUser().getName())
                .addSurnameElement(userDataResponse.getUser().getSurname())
                .setDateOfBirth(getBirthDateFromUserDataResponse(userDataResponse))
                .build();
    }

    private Optional<UserDataResponse.ResponseDetails> getUserDetails(
            UserDataResponse userDataResponse) {

        if (userDataResponse.getDetails().isEmpty()) {
            throw new IllegalArgumentException("No user details available in the response");
        }
        return Optional.of(userDataResponse.getDetails().get(0));
    }

    private LocalDate getBirthDateFromUserDataResponse(UserDataResponse userDataResponse) {
        UserDataResponse.ResponseDetails details =
                getUserDetails(userDataResponse).orElseThrow(IllegalArgumentException::new);
        String directBankingNumber = details.getDirectBankingNumber();
        Matcher matcher = BANKING_NUMBER_PATTERN.matcher(directBankingNumber);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Banking number does not match expected pattern, cannot map date of birth");
        }
        String day = matcher.group(1);
        String month = matcher.group(2);
        String year = matcher.group(3);
        return LocalDate.of(
                assumeCenturyOfYear(Integer.parseInt(year)),
                Integer.parseInt(month),
                Integer.parseInt(day));
    }

    /**
     * Assumption of the date of birth for a person 1-100 years old.
     *
     * @param year 2 digit year representation
     * @return 4 digit year representation with assumed century
     */
    private int assumeCenturyOfYear(int year) {
        Preconditions.checkArgument(year >= 0 && year <= 99);
        int yearNow = LocalDate.now().getYear();
        int numberOfCenturies = yearNow / 100;
        int resultInCurrentCentury = numberOfCenturies * 100 + year;
        if (resultInCurrentCentury >= yearNow) {
            return (numberOfCenturies - 1) * 100 + year;
        }
        return resultInCurrentCentury;
    }
}
