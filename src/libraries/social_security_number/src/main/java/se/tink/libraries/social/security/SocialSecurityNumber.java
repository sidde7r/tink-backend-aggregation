package se.tink.libraries.social.security;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import se.tink.libraries.credentials.demo.DemoCredentials;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SocialSecurityNumber {

    public static class Sweden {

        private static final Pattern PATTERN_SWEDISH = Pattern.compile("(19|20)?\\d{2}(0|1)\\d(0|1|2|3)\\d\\d{4}");
        private String parsedSocialSecurityNumber = null; // yyyyMMddnnnn
        private String birth = null;

        private final boolean isValid;

        public Sweden(String s) {
            if (s == null) {
                this.isValid = false;
                return;
            }

            String text = s.replace("-", "").replace(" ", "");

            Matcher m = PATTERN_SWEDISH.matcher(text);
            if (!m.matches()) {
                this.isValid = false;
                return;
            }

            int len = text.length();
            String fourLast = text.substring(len - 4, len);

            boolean isValid = false;
            try {
                String datePart = DateUtils.turnPastSixDigitsDateIntoEightDigits(text.substring(0, len - 4));

                parsedSocialSecurityNumber = datePart + fourLast;
                birth = calculateBirth();

                //noinspection SimplifiableIfStatement
                if (DemoCredentials.isDemoUser(s)) {
                    isValid = true;
                } else {
                    isValid = hasCorrectCheckDigit(parsedSocialSecurityNumber);
                }
            } catch(ParseException ignored) {
            }

            this.isValid = isValid;
        }

        private boolean hasCorrectCheckDigit(String twelveDigitSSN) {
            if (twelveDigitSSN == null || !Objects.equals(twelveDigitSSN.length(), 12)) {
                return false;
            }

            String tenDigitSSN = twelveDigitSSN.substring(2);
            int computedCheckDigit = calculateCheckDigit(tenDigitSSN);

            String lastCharacter = twelveDigitSSN.substring(twelveDigitSSN.length() - 1);
            int givenCheckDigit = Integer.parseInt(lastCharacter);

            return ((computedCheckDigit + givenCheckDigit) % 10) == 0;
        }

        private static int calculateCheckDigit(String tenDigitString) {
            String stringExceptCheckDigit = tenDigitString.substring(0, 9);

            int calculatedCheckDigit = 0;
            for (int i = 0; i < stringExceptCheckDigit.length(); i++) {
                int digit = Integer.parseInt(stringExceptCheckDigit.substring(i, i+1));

                if ((i % 2) == 0) {
                    digit = (digit * 2);
                }

                if (digit > 9) {
                    calculatedCheckDigit += (1 + (digit % 10));
                } else {
                    calculatedCheckDigit += digit;
                }
            }

            return calculatedCheckDigit;
        }

        public boolean isValid() {
            return isValid;
        }

        /**
         * @return YYYYMMDDNNNN
         */
        public String asString() {
            checkValidity();
            return parsedSocialSecurityNumber;
        }

        /**
         * @return YYYYMMDDNNNN
         */
        public String asStringWithoutValidityCheck() {
            return parsedSocialSecurityNumber;
        }

        /**
         * @return YYYYMMDD-NNNN
         */
        public String asStringWithDash() {
            checkValidity();
            return parsedSocialSecurityNumber.substring(0, 8) + "-" + parsedSocialSecurityNumber.substring(8, 12);
        }

        /**
         * @return Birth year if valid social security number
         */
        public int getBirthYear() {
            checkValidity();
            return Integer.parseInt(birth.substring(0, 4));
        }

        /**
         * @return Birth on format yyyy-MM-dd if valid social security number
         */
        public String getBirth() {
            checkValidity();
            return birth;
        }

        /**
         * @return Birth as a date if valid social security number
         */
        public Date getBirthDate() {
            String birth = getBirth();

            try {
                return ThreadSafeDateFormat.FORMATTER_DAILY.parse(birth);
            } catch (ParseException e) {
                throw new IllegalStateException(
                        "Personnumber is not valid. This shouldn't have happened. Birth: " + birth);
            }
        }

        /**
         * @return Age based on the Date sent to the method
         */
        public int getAge(LocalDate currentDate) {
            LocalDate birthDate = getBirthDate().toInstant().atZone(ZoneId.of("CET")).toLocalDate();
            return (int) ChronoUnit.YEARS.between(birthDate, currentDate);
        }

        private void checkValidity() {
            if (!isValid()) {
                throw new IllegalStateException("Personnumber is not valid. Caller should make sure to call isValid.");
            }
        }

        private String calculateBirth() {

            return new StringBuilder().append(parsedSocialSecurityNumber.substring(0, 4)).append("-")
                    .append(parsedSocialSecurityNumber.substring(4, 6)).append("-")
                    .append(parsedSocialSecurityNumber.substring(6, 8)).toString();
        }

    }
}
