package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.INVOICE;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.KID;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.REFERENCE;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.RF;
import static se.tink.libraries.transfer.enums.RemittanceInformationType.UNSTRUCTURED;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto.RemittanceInformationStructuredDto;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.enums.RemittanceInformationType;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class DemobankDtoMappersTest {

    private DemobankDtoMappers mappers;

    @Before
    public void setup() {
        mappers = new DemobankDtoMappers();
    }

    @Test
    public void createReferenceRemittanceInfoTest() {
        // given
        final String value = "Periodic payment";
        final RemittanceInformationType type = REFERENCE;

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(
                        createPaymentWithRemittanceInfo(value, type));

        // then
        assertEquals(actual.getReference(), value);
        assertEquals(actual.getReferenceType(), type.name());
    }

    @Test
    public void createOcrRemittanceInfoTest() {
        // given
        final String value = "1235462111";
        final RemittanceInformationType type = RemittanceInformationType.OCR;

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(
                        createPaymentWithRemittanceInfo(value, type));

        // then
        assertEquals(actual.getReference(), value);
        assertEquals(actual.getReferenceType(), type.name());
    }

    @Test
    public void shouldMapKidRemittanceInformation() {
        // given
        final String value = "12345";

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(createPaymentWithRemittanceInfo(value, KID));

        // then
        assertEquals(actual.getReference(), value);
        assertEquals("KID", actual.getReferenceType());
    }

    @Test
    public void shouldMapRfRemittanceInformation() {
        // given
        final String value = "RF12345";

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(createPaymentWithRemittanceInfo(value, RF));

        // then
        assertEquals(actual.getReference(), value);
        assertEquals("RF", actual.getReferenceType());
    }

    @Test
    public void shouldMapInvoiceRemittanceInformation() {
        // given
        final String value = "12345";

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(
                        createPaymentWithRemittanceInfo(value, INVOICE));

        // then
        assertEquals(actual.getReference(), value);
        assertEquals("INVOICE", actual.getReferenceType());
    }

    @Test
    public void returnNullStructuredRemittanceInfoTest() {
        // given
        final String value = "Top-up";

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(
                        createPaymentWithRemittanceInfo(value, UNSTRUCTURED));

        // then
        assertNull(actual);
    }

    private Payment createPaymentWithRemittanceInfo(String value, RemittanceInformationType type) {
        RemittanceInformation remittanceInformation = new RemittanceInformation();
        remittanceInformation.setValue(value);
        remittanceInformation.setType(type);

        return new Payment.Builder().withRemittanceInformation(remittanceInformation).build();
    }
}
