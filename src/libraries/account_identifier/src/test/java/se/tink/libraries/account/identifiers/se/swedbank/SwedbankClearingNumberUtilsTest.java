package se.tink.libraries.account.identifiers.se.swedbank;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.NonValidIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;

@RunWith(Enclosed.class)
public class SwedbankClearingNumberUtilsTest {
    public static class AppendFifthClearingDigit {
        @Test
        public void appendsLuhnCheckToClearing() {
            String fiveDigitClearingDigit =
                    SwedbankClearingNumberUtils.appendFifthClearingDigit("8327");
            assertThat(fiveDigitClearingDigit).isEqualTo("83279");
        }

        @Test
        public void appendsLuhnCheckToAnotherClearing() {
            String fiveDigitClearingDigit =
                    SwedbankClearingNumberUtils.appendFifthClearingDigit("8422");
            assertThat(fiveDigitClearingDigit).isEqualTo("84228");
        }
    }

    public static class IsFifthDigitPartOfValidClearingNumber {
        @Test
        public void isTrueForCheckDigitClearing() {
            String number = "8422831270465"; // The fifth char, '8', is a Luhn check digit

            boolean checkForNumber = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number);

            assertThat(checkForNumber).isTrue();
        }

        @Test
        public void isFalseForNonCheckDigitClearings() {
            String number0 = "8422031270465";
            String number1 = "8422131270465";
            String number2 = "8422231270465";
            String number3 = "8422331270465";
            String number4 = "8422431270465";
            String number5 = "8422531270465";
            String number6 = "8422631270465";
            String number7 = "8422731270465";
            String number9 = "8422931270465";

            boolean checkForNumber0 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number0);
            boolean checkForNumber1 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number1);
            boolean checkForNumber2 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number2);
            boolean checkForNumber3 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number3);
            boolean checkForNumber4 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number4);
            boolean checkForNumber5 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number5);
            boolean checkForNumber6 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number6);
            boolean checkForNumber7 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number7);
            boolean checkForNumber9 = SwedbankClearingNumberUtils.isFifthDigitValidCheck(number9);

            assertThat(checkForNumber0).isFalse();
            assertThat(checkForNumber1).isFalse();
            assertThat(checkForNumber2).isFalse();
            assertThat(checkForNumber3).isFalse();
            assertThat(checkForNumber4).isFalse();
            assertThat(checkForNumber5).isFalse();
            assertThat(checkForNumber6).isFalse();
            assertThat(checkForNumber7).isFalse();
            assertThat(checkForNumber9).isFalse();
        }
    }

    public static class InsertFifthClearingDigit {
        @Test
        public void insertsAtCorrectPosition() {
            String fourDigitClearing = "8422";
            String fifthClearingDigit = "8";
            String accountNumberPart = "123123123";

            String withoutFifthDigit = fourDigitClearing + accountNumberPart;

            String result = SwedbankClearingNumberUtils.insertFifthClearingDigit(withoutFifthDigit);

            assertThat(result)
                    .isEqualTo(fourDigitClearing + fifthClearingDigit + accountNumberPart);
        }
    }

    public static class PadWithZeros {
        @Test
        public void padsWhenZerosAreMissing() {
            String identifier = "8422831270465"; // Complete, non-padded account identifier

            String padded =
                    SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(
                            identifier);
            assertThat(padded).isEqualTo("842280031270465");
        }

        @Test
        public void doesNotPadForCompleteAccountNumber() {
            String identifier = "821499246657853"; // Already complete 15 digit account identifier

            String padded =
                    SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(
                            identifier);
            assertThat(padded).isEqualTo(identifier);
        }

        @Test
        public void padsIdentifier() {
            SwedishIdentifier identifier =
                    new SwedishIdentifier(
                            "8422831270465"); // Complete, non-padded account identifier

            String padded =
                    SwedbankClearingNumberUtils.padWithZerosBetweenClearingAndAccountNumber(
                            identifier);
            assertThat(padded).isEqualTo("842280031270465");
        }

        @Test
        public void padsAccountNumber() {
            String number = "31270465"; // Complete, non-padded account identifier

            String padded = SwedbankClearingNumberUtils.padWithZerosBeforeAccountNumber(number);
            assertThat(padded).isEqualTo("0031270465");
        }
    }

    public static class IsSwedbank8xxxIdentifier {
        @Test
        public void trueForSwedbankClearingStartingWith8() {
            SwedishIdentifier notPadded = new SwedishIdentifier("8422831270465");
            SwedishIdentifier padded = new SwedishIdentifier("842280031270465");

            assertThat(SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(notPadded)).isTrue();
            assertThat(SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(padded)).isTrue();
        }

        @Test
        public void falseForSwedbankClearingStartingWith7() {
            SwedishIdentifier swedbank7xxx = new SwedishIdentifier("7123112233");

            assertThat(SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(swedbank7xxx))
                    .isFalse();
        }

        @Test
        public void falseForOtherClearings() {
            SwedishIdentifier nordea = new SwedishIdentifier("33008607015537");

            assertThat(SwedbankClearingNumberUtils.isSwedbank8xxxAccountNumber(nordea)).isFalse();
        }
    }

    public static class RemoveZerosBetweenClearingAndAccountNumber {
        @Test
        public void createsANewIdentifierObject() {
            SwedishIdentifier notPadded = new SwedishIdentifier("8422831270465");
            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                            notPadded);
            assertThat(zerosRemoved).isNotSameAs(notPadded);
        }

        @Test
        public void accountNumberNotStartingWith0IsUnchanged() {
            SwedishIdentifier notPadded = new SwedishIdentifier("8422831270465");
            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                            notPadded);

            assertThat(zerosRemoved).isEqualTo(new SwedishIdentifier("8422831270465"));
        }

        @Test
        public void accountNumberNeedsAtLeast6Digits() {
            SwedishIdentifier accountNumberThatNeedsInitialZeros =
                    new SwedishIdentifier("842280000000123");
            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                            accountNumberThatNeedsInitialZeros);

            assertThat(zerosRemoved).isEqualTo(new SwedishIdentifier("84228000123"));
        }

        @Test
        public void removesZerosForAccountNumberStartingWithZeros() {
            SwedishIdentifier padded = new SwedishIdentifier("842280031270465");
            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(padded);

            assertThat(zerosRemoved).isEqualTo(new SwedishIdentifier("8422831270465"));
        }

        @Test
        public void doesNotRemoveZerosInEndOrMiddleOfAccountNumber() {
            SwedishIdentifier withZerosInOtherPlaces = new SwedishIdentifier("842281270046500");
            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                            withZerosInOtherPlaces);

            assertThat(zerosRemoved).isEqualTo(withZerosInOtherPlaces);
        }

        @Test(expected = IllegalArgumentException.class)
        public void throwsIllegalArgumentForNon8xxxSwedbankAccountNumber() {
            SwedishIdentifier non8xxxIdentifier = new SwedishIdentifier("712300112233");
            SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(
                    non8xxxIdentifier);
        }

        @Test
        public void preservesNameOnIdentifier() {
            SwedishIdentifier padded = new SwedishIdentifier("842280031270465");
            padded.setName("The name");

            SwedishIdentifier zerosRemoved =
                    SwedbankClearingNumberUtils.removeZerosBetweenClearingAndAccountNumber(padded);

            assertThat(zerosRemoved.getName().orElse(null)).isEqualTo("The name");
        }
    }

    public static class Validate {
        @Test
        public void passesWhenValidSwedbank8xxx() {
            SwedishIdentifier valid8xxx = new SwedishIdentifier("842280031270465");
            SwedbankClearingNumberUtils.validateIfSwedbank8xxxIdentifier(valid8xxx);
        }

        @Test(expected = IllegalStateException.class)
        public void throwsWhenInvalidIdentifier() {
            AccountIdentifier otherNonValid = new NonValidIdentifier(null);
            SwedbankClearingNumberUtils.validateIfSwedbank8xxxIdentifier(otherNonValid);
        }

        @Test(expected = IllegalStateException.class)
        public void throwsWhenInvalid8xxxClearingCheck() {
            SwedishIdentifier invalidClearing =
                    new SwedishIdentifier(
                            "842200031270465"); // The clearing 8422-0 is not valid Luhn
            SwedbankClearingNumberUtils.validateIfSwedbank8xxxIdentifier(invalidClearing);
        }

        @Test(expected = IllegalStateException.class)
        public void throwsWhenInvalid8xxxAccountNumberCheck() {
            SwedishIdentifier invalidAccountNumber =
                    new SwedishIdentifier(
                            "842280031270460"); // The account check digit 0 is not valid Luhn
            SwedbankClearingNumberUtils.validateIfSwedbank8xxxIdentifier(invalidAccountNumber);
        }
    }
}
