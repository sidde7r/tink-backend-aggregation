package se.tink.backend.webhook.rpc;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.firehose.v1.models.SignableOperation;

public class WebhookSignableOperationTest {
    SignableOperation signableOperation;

    @Before
    public void setup() {
        signableOperation = SignableOperation.newBuilder()
                .setCreated(1510317057000L)
                .setUpdated(1510317057000L)
                .setId("00000000000000000000000000000000")
                .setStatus(SignableOperation.Status.STATUS_AWAITING_CREDENTIALS)
                .setType(SignableOperation.Type.TYPE_ACCOUNT_CREATE)
                .setUnderlyingId("00000000000000000000000000000001")
                .setUserId("00000000000000000000000000000002")
                .setCredentialsId("00000000000000000000000000000003")
                .build();
    }

    @Test
    public void firehoseModel_convertToWebhookSignableOperation() {
        WebhookSignableOperation webhookSignableOperation = WebhookSignableOperation.fromFirehoseSignableOperation(
                signableOperation);

        Assert.assertEquals(signableOperation.getCreated(), webhookSignableOperation.getCreated());
        Assert.assertEquals(signableOperation.getUpdated(), webhookSignableOperation.getUpdated());
        Assert.assertEquals(signableOperation.getId(), webhookSignableOperation.getId());
        Assert.assertEquals("AWAITING_CREDENTIALS", webhookSignableOperation.getStatus());
        Assert.assertEquals("", webhookSignableOperation.getStatusMessage());
        Assert.assertEquals("ACCOUNT_CREATE", webhookSignableOperation.getType());
        Assert.assertEquals(signableOperation.getUnderlyingId(), webhookSignableOperation.getUnderlyingId());
        Assert.assertEquals(signableOperation.getUserId(), webhookSignableOperation.getUserId());
        Assert.assertEquals(signableOperation.getCredentialsId(), webhookSignableOperation.getCredentialsId());
    }
}
