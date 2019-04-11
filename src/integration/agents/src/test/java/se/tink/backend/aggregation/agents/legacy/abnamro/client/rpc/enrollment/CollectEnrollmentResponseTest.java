package se.tink.backend.aggregation.agents.abnamro.client.rpc.enrollment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CollectEnrollmentResponseTest {
    @Test
    public void testNotCompleted() {
        CollectEnrollmentResponse response = new CollectEnrollmentResponse();
        assertThat(response.isCompleted()).isFalse();
    }

    @Test
    public void testCompleted() {
        CollectEnrollmentResponse response = new CollectEnrollmentResponse();
        response.setBcNumber("1234567");

        assertThat(response.isCompleted()).isTrue();
        assertThat(response.getBcNumber()).isEqualTo("1234567");
    }
}
