package se.tink.backend.system.tasks;

import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.system.rpc.DeleteTransactionRequest;
import se.tink.backend.system.rpc.TransactionToDelete;
import se.tink.backend.utils.StringUtils;

public class DeleteTransactionTaskHandlerTest {

    ServiceContext serviceContext;

    @Before
    public void setUp() {
        serviceContext = Mockito.mock(ServiceContext.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWontDeleteIfInputNull() {
        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(new DeleteTransactionRequest());

        DeleteTransactionTaskHandler handler = new DeleteTransactionTaskHandler(serviceContext);

        handler.handle(task);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWontDeleteIfExternalTransactionIdIsNull() {
        DeleteTransactionRequest request = new DeleteTransactionRequest();
        request.setUserId(StringUtils.generateUUID());

        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(request);

        DeleteTransactionTaskHandler handler = new DeleteTransactionTaskHandler(serviceContext);
        handler.handle(task);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWontDeleteIfAccountIdIsEmpty() {
        DeleteTransactionRequest request = new DeleteTransactionRequest();
        request.setUserId(StringUtils.generateUUID());
        request.setTransactions(Lists.newArrayList(TransactionToDelete.create("externalTransactionId", "")));

        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(request);

        DeleteTransactionTaskHandler handler = new DeleteTransactionTaskHandler(serviceContext);
        handler.handle(task);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testWontDeleteIfExternalTransactionIdIsEmpty() {
        DeleteTransactionRequest request = new DeleteTransactionRequest();
        request.setUserId(StringUtils.generateUUID());
        request.setTransactions(Lists.newArrayList(TransactionToDelete.create("", "accountId")));

        DeleteTransactionTask task = new DeleteTransactionTask();
        task.setPayload(request);

        DeleteTransactionTaskHandler handler = new DeleteTransactionTaskHandler(serviceContext);
        handler.handle(task);
    }
}
