package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.google.common.collect.Lists;
import java.util.List;
import org.junit.Test;
import se.tink.backend.aggregation.agents.abnamro.client.model.ErrorEntity;
import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionResponseTest {

    @Test
    public void testIsSuccess() throws Exception {

        CreateSubscriptionResponse response = new CreateSubscriptionResponse();

        assertThat(response.isSuccess()).isFalse();

        response.setId(100L);

        assertThat(response.isSuccess()).isTrue();
    }

    @Test
    public void testIsCustomerAlreadySubscribed() throws Exception {

        CreateSubscriptionResponse response = new CreateSubscriptionResponse();

        assertThat(response.isCustomerAlreadySubscribed()).isFalse();

        List<ErrorEntity> messages = Lists.newArrayList();

        ErrorEntity message = new ErrorEntity();
        message.setMessageKey(CreateSubscriptionResponse.ErrorCodes.ALREADY_ACTIVE_SUBSCRIPTION);

        messages.add(message);

        response.setMessages(messages);

        assertThat(response.isCustomerAlreadySubscribed()).isTrue();
    }

    @Test
    public void testIsNonRetailCustomer() throws Exception {
        CreateSubscriptionResponse response = new CreateSubscriptionResponse();

        assertThat(response.isNonRetailCustomer()).isFalse();

        List<ErrorEntity> messages = Lists.newArrayList();

        ErrorEntity message = new ErrorEntity();
        message.setMessageKey(CreateSubscriptionResponse.ErrorCodes.NON_RETAIL_CUSTOMER);

        messages.add(message);

        response.setMessages(messages);

        assertThat(response.isNonRetailCustomer()).isTrue();
    }
}
