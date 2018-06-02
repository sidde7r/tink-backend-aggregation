package se.tink.backend.grpc.v1.streaming;

import io.grpc.stub.ServerCallStreamObserver;
import java.util.LinkedList;
import org.junit.Test;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import se.tink.grpc.v1.models.Account;
import se.tink.grpc.v1.models.Accounts;
import se.tink.grpc.v1.rpc.StreamingResponse;
import se.tink.libraries.metrics.MetricRegistry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

public class StreamingResponseHandlerTest {

    @Test
    public void checkMessageOrder() {
        LinkedList<StreamingResponse> receivedResponses = new LinkedList<>();
        ServerCallStreamObserver<StreamingResponse> streamObserver = new FakeServerCallStreamObserver(receivedResponses);

        User user = new User() {{
            setProfile(new UserProfile() {{
                setCurrency("SEK");
            }});
        }};

        StreamingResponseHandler streamingResponseHandler = new StreamingResponseHandler(user, streamObserver,
                null, null, null, null, null, null, null, new MetricRegistry());
        streamingResponseHandler.sendFirehoseMessage(streamingResponseWithAccountName("1"));
        streamingResponseHandler.sendFirehoseMessage(streamingResponseWithAccountName("2"));
        streamingResponseHandler.sendFirehoseMessage(streamingResponseWithAccountName("3"));

        streamingResponseHandler.sendContext(streamingResponseWithAccountName("init"));

        streamingResponseHandler.sendFirehoseMessage(streamingResponseWithAccountName("4"));

        assertThat(receivedResponses).hasSize(5);
        assertThat(receivedResponses.get(0).getAccounts().getAccount(0).getName()).isEqualTo("init");
        assertThat(receivedResponses.get(1).getAccounts().getAccount(0).getName()).isEqualTo("1");
        assertThat(receivedResponses.get(2).getAccounts().getAccount(0).getName()).isEqualTo("2");
        assertThat(receivedResponses.get(3).getAccounts().getAccount(0).getName()).isEqualTo("3");
        assertThat(receivedResponses.get(4).getAccounts().getAccount(0).getName()).isEqualTo("4");
    }

    private StreamingResponse streamingResponseWithAccountName(String accountName) {
        return StreamingResponse.newBuilder().setAccounts(Accounts.newBuilder().addAccount(Account.newBuilder().setName(accountName))).build();
    }

    private class FakeServerCallStreamObserver extends ServerCallStreamObserver {
        private LinkedList<StreamingResponse> responses;
        public FakeServerCallStreamObserver(LinkedList<StreamingResponse> responses) {
            this.responses = responses;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public void setOnCancelHandler(Runnable runnable) {

        }

        @Override
        public void setCompression(String s) {

        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setOnReadyHandler(Runnable runnable) {

        }

        @Override
        public void disableAutoInboundFlowControl() {

        }

        @Override
        public void request(int i) {

        }

        @Override
        public void setMessageCompression(boolean b) {

        }

        @Override
        public void onNext(Object o) {
            responses.add((StreamingResponse) o);
        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }
}
