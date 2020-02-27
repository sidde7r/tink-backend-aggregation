package se.tink.backend.aggregation.nxgen.scaffold;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExternalApiCallResultTest {

    @Test
    public void success200StatusTest() {
        // given
        int successStatus = 200;

        // when
        ExternalApiCallResult<Object> tested = ExternalApiCallResult.of(null, successStatus);

        // then
        assertThat(tested.is2xxSuccess()).isTrue();
        assertThat(tested.is3xxRedirect()).isFalse();
        assertThat(tested.isSuccess()).isTrue();
    }

    @Test
    public void success201StatusTest() {
        // given
        int successStatus = 201;

        // when
        ExternalApiCallResult<Object> tested = ExternalApiCallResult.of(null, successStatus);

        // then
        assertThat(tested.is2xxSuccess()).isTrue();
        assertThat(tested.is3xxRedirect()).isFalse();
        assertThat(tested.isSuccess()).isTrue();
    }

    @Test
    public void redirect301StatusTest() {
        // given
        int successStatus = 301;

        // when
        ExternalApiCallResult<Object> tested = ExternalApiCallResult.of(null, successStatus);

        // then
        assertThat(tested.is2xxSuccess()).isFalse();
        assertThat(tested.is3xxRedirect()).isTrue();
        assertThat(tested.isSuccess()).isTrue();
    }

    @Test
    public void otherStatusTest() {
        // given
        int successStatus = 400;

        // when
        ExternalApiCallResult<Object> tested = ExternalApiCallResult.of(null, successStatus);

        // then
        assertThat(tested.is2xxSuccess()).isFalse();
        assertThat(tested.is3xxRedirect()).isFalse();
        assertThat(tested.isSuccess()).isFalse();
    }
}
