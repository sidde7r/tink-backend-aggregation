package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authentication.storage;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.rpc.FetchAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.StepDataStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class StepDataStorageTest {

    private SessionStorage sessionStorage;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String jsonAccountResponse =
            "{"
                    + "\"accounts\": "
                    + "["
                    + "{\"resourceId\": \"20444455555333555558888888855555\","
                    + "\"iban\": \"EE032311123456789015\","
                    + "\"cashAccountType\": \"CACC\", "
                    + "\"currency\": \"EUR\","
                    + "\"product\": \"CURRENT\"}, "
                    + "{\"resourceId\": \"20444455555333555558888888855522\","
                    + "\"iban\": \"EE182311987654321015\","
                    + "\"cashAccountType\": \"CACC\", "
                    + "\"currency\": \"EUR\","
                    + "\"product\": \"LIMIT\"} "
                    + "]}";

    @Test
    public void shouldPutAndGetAccountResponse() throws JsonProcessingException {

        sessionStorage = new SessionStorage();
        FetchAccountResponse accountResponseBefore =
                objectMapper.readValue(jsonAccountResponse, FetchAccountResponse.class);

        sessionStorage.put(StepDataStorage.ACC_RESP, accountResponseBefore);
        Optional<FetchAccountResponse> accountResponseAfter =
                sessionStorage.get(StepDataStorage.ACC_RESP, FetchAccountResponse.class);

        assertThat(accountResponseAfter).isPresent();
        assertThat(accountResponseAfter.get().getAccounts().get(0).getIban())
                .isEqualTo("EE032311123456789015");
        assertThat(accountResponseAfter.get().getAccounts().get(1).getIban())
                .isEqualTo("EE182311987654321015");
        assertThat(accountResponseAfter.get().getAccounts().size())
                .isEqualTo(accountResponseBefore.getAccounts().size());
    }
}
