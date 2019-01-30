package se.tink.backend.aggregation.agents.banks.sbab.util;

import org.junit.Test;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import static org.assertj.core.api.Assertions.assertThat;

public class SBABDestinationAccountIdentifierFormatterTest {
    @Test
    public void ensure10DigitsHandelsbanken_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("6163153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("6163000153152");
    }

    @Test
    public void ensure13DigitsHandelsbanken_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("6163153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("6163153152151");
    }

    @Test
    public void ensure14DigitsHandelsbanken_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("61631531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("61631531521511");
    }

    @Test
    public void ensure10DigitsPlusGirot_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("9960153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("99600000153152");
    }

    @Test
    public void ensure14DigitsPlusGirot_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("99622153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("99622153152151");
    }

    @Test
    public void ensure15DigitsPlusGirot_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("996221531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("996221531521511");
    }

    @Test
    public void ensure10SparbankenSyd_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("9571153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("95710000153152");
    }

    @Test
    public void ensure14SparbankenSyd_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("95712153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("95712153152151");
    }

    @Test
    public void ensure15SparbankenSyd_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("957121531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("957121531521511");
    }

    @Test
    public void ensure10SparbankenOresund_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("9345153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("93450000153152");
    }

    @Test
    public void ensure14SparbankenOresund_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("93452153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("93452153152151");
    }

    @Test
    public void ensure15SparbankenOresund_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("934521531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("934521531521511");
    }

    @Test
    public void ensure10DanskeBank_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("9181153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("91810000153152");
    }

    @Test
    public void ensure14DanskeBank_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("91812153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("91812153152151");
    }

    @Test
    public void ensure15DanskeBank_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("918121531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("918121531521511");
    }

    @Test
    public void ensure10Swedbank_getCorrectPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("89812153152");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("898120000153152");
    }

    @Test
    public void ensure15Swedbank_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("898122153152151");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("898122153152151");
    }

    @Test
    public void ensure16Swedbank_getNoPadding() {
        AccountIdentifier accountIdentifier = new SwedishIdentifier("8981221531521511");

        String formattedDestination = accountIdentifier.getIdentifier(new SBABDestinationAccountIdentifierFormatter());

        assertThat(formattedDestination).isEqualTo("8981221531521511");
    }
}
