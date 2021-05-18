package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.BeneficiaryEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.rpc.CreatePaymentRequest;

public class DateUtilTest {

    @Test
    public void testFranceTplusOneDate() {
        CreatePaymentRequest paymentRequest = mock(CreatePaymentRequest.class);
        BeneficiaryEntity beneficiary = mock(BeneficiaryEntity.class);
        when(paymentRequest.getBeneficiary()).thenReturn(beneficiary);
        AccountEntity fr = new AccountEntity("FR1420041010050500013M02606");
        when(beneficiary.getCreditorAccount()).thenReturn(fr);
        when(paymentRequest.getRequestedExecutionDate())
                .thenReturn("2016-12-30T00:00:00.000+01:00");
        assertThat(DateUtil.getExecutionDate(paymentRequest))
                .isEqualTo("2016-12-31T00:00:00.000+01:00");
    }

    @Test
    public void testGreatBritainplusZeroDate() {
        CreatePaymentRequest paymentRequest = mock(CreatePaymentRequest.class);
        BeneficiaryEntity beneficiary = mock(BeneficiaryEntity.class);
        when(paymentRequest.getBeneficiary()).thenReturn(beneficiary);
        AccountEntity gb = new AccountEntity("GB29NWBK60161331926819");
        when(beneficiary.getCreditorAccount()).thenReturn(gb);
        when(paymentRequest.getCreationDateTime()).thenReturn("2016-12-30T00:00:00.000+01:00");
        assertThat(DateUtil.getExecutionDate(paymentRequest))
                .isEqualTo("2016-12-30T00:00:00.000+01:00");
    }
}
