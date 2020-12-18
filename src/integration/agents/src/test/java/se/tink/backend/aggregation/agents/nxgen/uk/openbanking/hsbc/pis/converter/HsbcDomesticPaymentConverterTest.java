package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.pis.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createNoTypeRemittanceInformation;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createNoTypeRemittanceInformationDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPayment;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createPaymentWithRemittanceInfo;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createReferenceRemittanceInformation;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createReferenceRemittanceInformationDto;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.UkOpenBankingPaymentTestFixtures.createUnstructuredRemittanceInformation;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.common.dto.RemittanceInformationDto;
import se.tink.libraries.payment.rpc.Payment;
import se.tink.libraries.transfer.rpc.RemittanceInformation;

public class HsbcDomesticPaymentConverterTest {

    private HsbcDomesticPaymentConverter hsbcDomesticPaymentConverter;

    @Before
    public void setUp() {
        hsbcDomesticPaymentConverter = new HsbcDomesticPaymentConverter();
    }

    @Test
    public void shouldGetUnstructuredRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation =
                createUnstructuredRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                hsbcDomesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createNoTypeRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldGetReferenceRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createReferenceRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                hsbcDomesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createReferenceRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldGetNoTypeRemittanceInformation() {
        // given
        final RemittanceInformation remittanceInformation = createNoTypeRemittanceInformation();
        final Payment paymentMock = createPaymentWithRemittanceInfo(remittanceInformation);

        // when
        final RemittanceInformationDto returned =
                hsbcDomesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        final RemittanceInformationDto expected = createNoTypeRemittanceInformationDto();
        assertThat(returned).isEqualTo(expected);
    }

    @Test
    public void shouldRemittanceInformationBeEmptyStringWhenPaymentHasNone() {
        // given
        final Payment paymentMock = createPayment();

        when(paymentMock.getRemittanceInformation()).thenReturn(null);

        // when
        final RemittanceInformationDto returned =
                hsbcDomesticPaymentConverter.getRemittanceInformationDto(paymentMock);

        // then
        assertThat(returned.getReference()).isEmpty();
        assertThat(returned.getUnstructured()).isEmpty();
    }
}
