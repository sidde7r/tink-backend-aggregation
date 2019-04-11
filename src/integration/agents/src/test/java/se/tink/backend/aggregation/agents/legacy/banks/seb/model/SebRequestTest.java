package se.tink.backend.aggregation.agents.banks.seb.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

public class SebRequestTest {
    @Test
    public void emptyServiceInputForEmptyConstruction() {
        SebRequest request = SebRequest.empty().build();
        assertThat(request.request.ServiceInput).isEmpty();
    }

    @Test
    public void builtWithKundNrHasServiceInput() {
        ServiceInput expectedServiceInput = new ServiceInput("SEB_KUND_NR", "the customer id");

        SebRequest request = SebRequest.withSEB_KUND_NR("the customer id").build();

        assertThat(request.request.ServiceInput).hasSize(1);
        assertThat(request.request.ServiceInput.get(0))
                .isEqualToComparingFieldByField(expectedServiceInput);
    }

    @Test
    public void builtWithUserIdHasServiceInput() {
        ServiceInput expectedServiceInput = new ServiceInput("USER_ID", "the user id");

        SebRequest request = SebRequest.withUSER_ID("the user id").build();

        assertThat(request.request.ServiceInput).hasSize(1);
        assertThat(request.request.ServiceInput.get(0))
                .isEqualToComparingFieldByField(expectedServiceInput);
    }

    @Test
    public void addsServiceInputs() {
        ImmutableList<ServiceInput> expectedServiceInput =
                ImmutableList.<ServiceInput>builder()
                        .add(new ServiceInput("SEB_KUND_NR", "the user id"))
                        .add(new ServiceInput("TESTING_SECOND_VARIABLE", 1))
                        .add(new ServiceInput("TEST_VARIABLE", "testing the variable"))
                        .build();

        SebRequest request =
                SebRequest.withSEB_KUND_NR("the user id")
                        .addServiceInputEQ("TEST_VARIABLE", "testing the variable")
                        .addServiceInputEQ("TESTING_SECOND_VARIABLE", 1)
                        .build();

        assertThat(request.request.ServiceInput).hasSize(3);
        assertThat(request.request.ServiceInput)
                .usingFieldByFieldElementComparator()
                .containsAll(expectedServiceInput);
    }
}
