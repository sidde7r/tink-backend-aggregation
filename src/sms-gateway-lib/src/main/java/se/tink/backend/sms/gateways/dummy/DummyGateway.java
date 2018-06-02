package se.tink.backend.sms.gateways.dummy;

import se.tink.backend.sms.gateways.SmsGateway;
import se.tink.backend.sms.gateways.rpc.SmsRequest;
import se.tink.backend.sms.gateways.rpc.SmsResponse;
import se.tink.libraries.log.LogUtils;

public class DummyGateway implements SmsGateway {
    private static final LogUtils log = new LogUtils(DummyGateway.class);

    @Override
    public SmsResponse send(SmsRequest request) {
        log.info(String.format("Sending SMS from %s to %s message %s", request.getSender(), request.getTo(),
                request.getMessage()));

        SmsResponse response = new SmsResponse();
        response.setSuccess(true);
        return response;
    }
}
