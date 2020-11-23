package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.pis.converter.common;

import static org.assertj.core.api.Assertions.assertThat;

import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.payment.rpc.Payment;

public abstract class ConverterTestBase {

    protected static void validatePaymentsAreEqual(Payment returned, Payment expected) {
        validateAccountIdentifiersAreEqual(
                returned.getCreditor().getAccountIdentifier(),
                expected.getCreditor().getAccountIdentifier());
        assertThat(returned.getCreditor().getAccountIdentifierType())
                .isEqualTo(expected.getCreditor().getAccountIdentifierType());
        assertThat(returned.getCreditor().getAccountNumber())
                .isEqualTo(expected.getCreditor().getAccountNumber());
        assertThat(returned.getCreditor().getName()).isEqualTo(expected.getCreditor().getName());

        validateAccountIdentifiersAreEqual(
                returned.getDebtor().getAccountIdentifier(),
                expected.getDebtor().getAccountIdentifier());
        assertThat(returned.getDebtor().getAccountNumber())
                .isEqualTo(expected.getDebtor().getAccountNumber());

        assertThat(returned.getCurrency()).isEqualTo(expected.getCurrency());
        assertThat(returned.getUniqueId()).isEqualTo(expected.getUniqueId());
        assertThat(returned.getUniqueIdForUKOPenBanking())
                .isEqualTo(expected.getUniqueIdForUKOPenBanking());
        assertThat(returned.getExactCurrencyAmount()).isEqualTo(expected.getExactCurrencyAmount());
        assertThat(returned.getStatus()).isEqualTo(expected.getStatus());
        assertThat(returned.getRemittanceInformation())
                .isEqualTo(expected.getRemittanceInformation());
    }

    protected static void validateAccountIdentifiersAreEqual(
            AccountIdentifier returned, AccountIdentifier expected) {
        assertThat(returned.getIdentifier()).isEqualTo(expected.getIdentifier());
        assertThat(returned.getType()).isEqualTo(expected.getType());
    }
}
