package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.executor.payment.enums.CrosskeyPaymentStatus;
import se.tink.libraries.payment.enums.PaymentStatus;

public class CrosskeyPaymentStatusMapperTest {

  @Test
  public void shouldMapToProperPaymentStatus() {

    assertEquals(CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.AWAITING_AUTHORISATION),PaymentStatus.PENDING);
    assertEquals(CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.AUTHORISED),PaymentStatus.SIGNED);
    assertEquals(CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.ACCEPTED_SETTLEMENT_IN_PROCESS),PaymentStatus.PAID);
    assertEquals(CrosskeyPaymentStatus.mapToTinkPaymentStatus(CrosskeyPaymentStatus.UNKNOWN),PaymentStatus.UNDEFINED);

  }
}
