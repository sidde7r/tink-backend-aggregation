package se.tink.backend.sms.gateways.cmtelecom;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import javax.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import se.tink.backend.sms.gateways.cmtelecom.config.CmTelecomConfig;
import se.tink.backend.sms.gateways.SmsGateway;
import se.tink.backend.sms.gateways.cmtelecom.model.BulkSmsRequest;
import se.tink.backend.sms.gateways.cmtelecom.model.BulkSmsResponse;
import se.tink.backend.sms.gateways.rpc.SmsRequest;
import se.tink.backend.sms.gateways.rpc.SmsResponse;
import se.tink.libraries.log.LogUtils;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CmTelecomGateway implements SmsGateway {
    private static final LogUtils log = new LogUtils(CmTelecomGateway.class);
    private static final MetricId BULK_SMS_METRIC = MetricId.newId("cm_telecom_gateway_bulk_sms");

    private final MetricRegistry metricRegistry;
    private final CmTelecomConfig config;
    private final Client client;

    @Inject
    public CmTelecomGateway(CmTelecomConfig config, MetricRegistry metricRegistry) {
        Preconditions.checkState(!Strings.isNullOrEmpty(config.getApiKey()), "ApiKey must not be null or empty");
        Preconditions.checkState(!Strings.isNullOrEmpty(config.getEndpoint()), "Endpoint must not be null or empty");

        this.client = new BasicJerseyClientFactory().createBasicClient();
        this.metricRegistry = metricRegistry;
        this.config = config;
    }

    @Override
    public SmsResponse send(SmsRequest request) {
        BulkSmsRequest bulkSmsRequest = BulkSmsRequest.builder()
                .apiKey(config.getApiKey())
                .from(request.getSender())
                .to(request.getTo())
                .message(request.getMessage())
                .build();

        ClientResponse response = client.resource(config.getEndpoint())
                .accept(MediaType.APPLICATION_JSON)
                .type(MediaType.APPLICATION_JSON)
                .post(ClientResponse.class, bulkSmsRequest);

        SmsResponse smsResponse = new SmsResponse();

        String entity = response.hasEntity() ? response.getEntity(String.class) : null;
        smsResponse.setPayload(entity);
        smsResponse.setSuccess(false);

        if (response.getStatus() == HttpStatus.SC_OK && entity != null) {
            BulkSmsResponse bulkSmsResponse = SerializationUtils.deserializeFromString(entity, BulkSmsResponse.class);

            smsResponse.setSuccess(bulkSmsResponse.isSuccess());
        }

        if (!smsResponse.isSuccess()) {
            log.error(String.format("Failed to deliver SMS (Status = '%d', Content Type = '%s', Content Length = '%d')",
                    response.getStatus(), response.getType(), entity == null ? 0 : entity.length()));
        }

        metricRegistry.meter(BULK_SMS_METRIC.label("success", smsResponse.isSuccess())).inc();

        return smsResponse;
    }
}

