package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

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
        final RemittanceInformationType type = RemittanceInformationType.REFERENCE;

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
    public void returnNullStructuredRemittanceInfoTest() {
        // given
        final String value = "Top-up";
        final RemittanceInformationType type = RemittanceInformationType.UNSTRUCTURED;

        // when
        RemittanceInformationStructuredDto actual =
                mappers.createStructuredRemittanceInfo(
                        createPaymentWithRemittanceInfo(value, type));

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
