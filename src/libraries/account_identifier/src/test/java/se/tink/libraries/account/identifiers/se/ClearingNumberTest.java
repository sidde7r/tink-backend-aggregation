package se.tink.libraries.account.identifiers.se;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ClearingNumberTest {

    @Test
    public void testNonExistingBefore() {
        assertThat(ClearingNumber.get("999").isPresent()).isFalse();
        assertThat(ClearingNumber.get("0").isPresent()).isFalse();
        assertThat(ClearingNumber.get("-1").isPresent()).isFalse();
    }

    @Test
    public void testNonExistingInbetween() {
        assertThat(ClearingNumber.get("9010").isPresent()).isFalse();
        assertThat(ClearingNumber.get("9125").isPresent()).isFalse();
    }

    @Test
    public void testNonExistingAfter() {
        assertThat(ClearingNumber.get("9970").isPresent()).isFalse();
        assertThat(ClearingNumber.get("10000").isPresent()).isFalse();
        assertThat(ClearingNumber.get("218391741").isPresent()).isFalse();
        assertThat(ClearingNumber.get(Integer.toString(Integer.MAX_VALUE)).isPresent()).isFalse();
    }

    @Test
    public void testRangeBoundries() {
        assertEquals(ClearingNumber.get("1100").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("1101").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("1150").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("1198").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("1199").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("1200").get().getBankName(), "Danske Bank");
        assertEquals(ClearingNumber.get("1201").get().getBankName(), "Danske Bank");
        assertEquals(ClearingNumber.get("1399").get().getBankName(), "Danske Bank");
        assertEquals(ClearingNumber.get("1400").get().getBankName(), "Nordea");

        assertEquals(ClearingNumber.get("3299").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("3300").get().getBankName(), "Nordea");
        assertEquals(ClearingNumber.get("3301").get().getBankName(), "Nordea");
    }

    @Test
    public void testClearingNumberLength() {
        assertEquals(4, ClearingNumber.get("1100").get().getClearingNumberLength());
        assertEquals(4, ClearingNumber.get("3782").get().getClearingNumberLength());
        assertEquals(4, ClearingNumber.get("6999").get().getClearingNumberLength());
        assertEquals(5, ClearingNumber.get("8000").get().getClearingNumberLength());
        assertEquals(5, ClearingNumber.get("8500").get().getClearingNumberLength());
        assertEquals(5, ClearingNumber.get("8999").get().getClearingNumberLength());
        assertEquals(4, ClearingNumber.get("9180").get().getClearingNumberLength());
        assertEquals(4, ClearingNumber.get("9269").get().getClearingNumberLength());
        assertEquals(4, ClearingNumber.get("9579").get().getClearingNumberLength());
    }
}
