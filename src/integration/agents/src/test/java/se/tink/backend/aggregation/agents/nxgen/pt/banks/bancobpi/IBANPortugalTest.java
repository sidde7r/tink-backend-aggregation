package se.tink.backend.aggregation.agents.nxgen.pt.banks.bancobpi;

import org.junit.Assert;
import org.junit.Test;

public class IBANPortugalTest {

    @Test
    public void calculateNIBCheckDigitsShouldReturnCorrectCheckDigits() {
        // given
        final String rawNIB1 = "0010000054706260001";
        String expectedCheckDigits1 = "48";
        final String rawNIB2 = "0010999919625110601";
        final String expectedCheckDigits2 = "31";
        // when
        String checkDigits1 = IBANPortugal.calculateNIBCheckDigits(rawNIB1);
        String checkDigits2 = IBANPortugal.calculateNIBCheckDigits(rawNIB2);
        // then
        Assert.assertEquals(expectedCheckDigits1, checkDigits1);
        Assert.assertEquals(expectedCheckDigits2, checkDigits2);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateNIBCheckDigitsShouldThrowErrorForNotDigitOnlyValue() {
        // given
        final String rawNIB1 = "001000005470626000A";
        // when
        IBANPortugal.calculateNIBCheckDigits(rawNIB1);
        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateNIBCheckDigitsShouldThrowErrorWhenLengthIsDifferentThen19() {
        // given
        final String rawNIB1 = "00100000547062600012";
        // when
        IBANPortugal.calculateNIBCheckDigits(rawNIB1);
        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateIBANShouldThrowErrorWhenBankIdIsIncorrect() {
        // given
        // when
        IBANPortugal.generateIBAN("001", "0000", "54706260001");
        // then
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateIBANShouldThrowErrorWhenPSPRefNoIsIncorrect() {
        // given
        // when
        IBANPortugal.generateIBAN("0010", "000", "54706260001");
        // then
    }

    @Test
    public void generateIBANShouldGenerateCorrectIBAN() {
        // given
        final String expectedIBAN = "PT50 001000005470626000148";
        // when
        String iban = IBANPortugal.generateIBAN("0010", "0000", "54706260001");
        // then
        Assert.assertEquals(expectedIBAN, iban);
    }
}
