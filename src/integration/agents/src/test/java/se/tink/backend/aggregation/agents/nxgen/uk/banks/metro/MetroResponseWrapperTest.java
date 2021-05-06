package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import static io.vavr.API.$;
import static org.assertj.core.api.Assertions.assertThat;

import agents_platform_agents_framework.org.springframework.http.HttpStatus;
import agents_platform_agents_framework.org.springframework.http.ResponseEntity;
import io.vavr.API;
import io.vavr.control.Either;
import java.util.Arrays;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.ErrorResponse;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.model.rpc.mobileapp.SecurityNumberSeedResponse;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.AgentBankApiError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.InvalidCredentialsError;
import se.tink.backend.aggregation.agentsplatform.agentsframework.error.ServerError;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class MetroResponseWrapperTest {

    @Test
    public void shouldWrapResponseAsSuccess() {
        // given
        ResponseEntity<String> responseEntity =
                ResponseEntity.ok(
                        "{\"accountCardLocked\":false,\"deviceSlotAvailable\":false,\"ibId\":\"115240534571\",\"magicWordLocked\":false,\"seed\":\"4,5,6\",\"status\":\"IB_REGISTERED\"}");

        // when
        Either<AgentBankApiError, SecurityNumberSeedResponse> response =
                MetroResponseWrapper.of(responseEntity, SecurityNumberSeedResponse.class).wrap();

        // then
        assertThat(response.isRight()).isTrue();
        assertThat(response.get().indexPositions()).isEqualTo(Arrays.asList(4, 5, 6));
    }

    @Test
    public void shouldMapFailure() {
        // given

        ResponseEntity<String> responseEntity =
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(
                                "{\"code\":\"INVALID_USER_DETAILS\",\"message\":\"The Customer ID or Username you have entered was not recognised. Please try again.\",\"messageType\":\"INLINE\"}");
        // when
        Either<AgentBankApiError, SecurityNumberSeedResponse> response =
                MetroResponseWrapper.of(responseEntity, SecurityNumberSeedResponse.class)
                        .mapFailure(
                                API.Case(
                                        $(
                                                res -> {
                                                    ErrorResponse error =
                                                            SerializationUtils
                                                                    .deserializeFromString(
                                                                            res.getBody(),
                                                                            ErrorResponse.class);
                                                    return error != null
                                                            && error.getCode()
                                                                    .equals("INVALID_USER_DETAILS");
                                                }),
                                        new InvalidCredentialsError()),
                                API.Case(
                                        $(res -> res.getStatusCode().is5xxServerError()),
                                        new ServerError()))
                        .wrap();

        // then
        assertThat(response.isLeft()).isTrue();
        assertThat(response.getLeft()).isInstanceOf(InvalidCredentialsError.class);
    }
}
