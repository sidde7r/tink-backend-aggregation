package se.tink.libraries.queue.sqs;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.RejectedExecutionException;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.queue.QueueProducer;

public class SqsConsumerTest {
    private static final Message MESSAGE = getMessage();
    private static final String MESSAGE_BODY = "BODY";
    private SqsQueue sqsQueue;
    private AmazonSQS amazonSQS;
    private ReceiveMessageResult receiveMessageResult;
    private QueueProducer queueProducer;
    private QueueMessageAction queueMessageAction;

    @Before
    public void init() {
        sqsQueue = mock(SqsQueue.class);
        amazonSQS = mock(AmazonSQS.class);
        when(sqsQueue.getSqs()).thenReturn(amazonSQS);
        receiveMessageResult = mock(ReceiveMessageResult.class);
        queueProducer = mock(QueueProducer.class);
        queueMessageAction = mock(QueueMessageAction.class);
    }

    @Test
    public void shouldConsumeIfMessagesAreAvailable() throws IOException {
        when(receiveMessageResult.getMessages()).thenReturn(Collections.singletonList(MESSAGE));
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveMessageResult);

        SqsConsumer sqsConsumer =
                spy(new SqsConsumer(sqsQueue, queueProducer, queueMessageAction, "Regular"));

        // when
        sqsConsumer.consume();

        // then
        verify(receiveMessageResult, times(1)).getMessages();
        verify(sqsConsumer, times(1)).delete(MESSAGE);
        verify(sqsConsumer, times(1)).tryConsumeUntilNotRejected(MESSAGE);
    }

    @Test
    public void shouldNotConsumeIfNoMessages() throws IOException {
        when(receiveMessageResult.getMessages()).thenReturn(Collections.emptyList());
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveMessageResult);
        SqsConsumer sqsConsumer =
                spy(new SqsConsumer(sqsQueue, queueProducer, queueMessageAction, "Regular"));

        // when
        sqsConsumer.consume();

        // then
        verify(sqsConsumer, never()).delete(MESSAGE);
        verify(sqsConsumer, never()).tryConsumeUntilNotRejected(MESSAGE);
    }

    @Test
    public void shouldRequeueMessageIfGotRejectedExecutionException() throws IOException {
        when(receiveMessageResult.getMessages()).thenReturn(Collections.singletonList(MESSAGE));
        when(amazonSQS.receiveMessage(any(ReceiveMessageRequest.class)))
                .thenReturn(receiveMessageResult);
        doThrow(new RejectedExecutionException()).when(queueMessageAction).handle(anyString());
        SqsConsumer sqsConsumer =
                spy(new SqsConsumer(sqsQueue, queueProducer, queueMessageAction, "Regular"));

        // when
        sqsConsumer.consume();

        // then
        verify(queueProducer, times(1)).requeue("BODY");
    }

    private static Message getMessage() {
        Message message = new Message();
        message.setBody(MESSAGE_BODY);
        return message;
    }
}
