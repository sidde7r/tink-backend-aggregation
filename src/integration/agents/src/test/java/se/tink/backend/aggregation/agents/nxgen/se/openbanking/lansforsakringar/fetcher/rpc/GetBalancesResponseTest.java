package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class GetBalancesResponseTest {

    private GetBalancesResponse getBalancesResponse;

    @Test
    public void shouldReturnExactCurrencyAmount() {
        // given
        getBalancesResponse = getBalancesResponse();

        // when
        ExactCurrencyAmount result = getBalancesResponse.getBalance();

        // then
        assertEquals(ExactCurrencyAmount.inSEK(5.0), result);
    }

    @Test
    public void shouldReturnIllegalStateExceptionInCaseWrongBalanceType() {
        // given
        getBalancesResponse = getBalancesResponseWrongBalanceType();

        // then
        assertThatThrownBy(() -> getBalancesResponse.getBalance())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No balance found in the response");
    }

    @Test
    public void shouldReturnEmptyExactCurrencyAmount() {
        // given
        getBalancesResponse = getEmptyBalancesResponse();

        // then
        assertThatThrownBy(() -> getBalancesResponse.getBalance())
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("No balance found in the response");
    }

    private GetBalancesResponse getBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"balances\":[{\"balanceAmount\":{\"amount\":5.00,\"currency\":\"SEK\"},\"balanceType\":\"AUTHORIZED\"},{\"balanceAmount\":{\"amount\":5.00,\"currency\":\"SEK\"},\"balanceType\":\"EXPECTED\"}]}\n",
                GetBalancesResponse.class);
    }

    private GetBalancesResponse getBalancesResponseWrongBalanceType() {
        return SerializationUtils.deserializeFromString(
                "{\"balances\":[{\"balanceAmount\":{\"amount\":5.00,\"currency\":\"EUR\"},\"balanceType\":\"NOTAUTHORIZED\"},{\"balanceAmount\":{\"amount\":5.00,\"currency\":\"SEK\"},\"balanceType\":\"EXPECTED\"}]}\n",
                GetBalancesResponse.class);
    }

    private GetBalancesResponse getEmptyBalancesResponse() {
        return SerializationUtils.deserializeFromString(
                "{\"balances\":[]}\n", GetBalancesResponse.class);
    }
}
