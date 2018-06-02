package se.tink.backend.sms.gateways;

import se.tink.backend.sms.gateways.rpc.SmsRequest;
import se.tink.backend.sms.gateways.rpc.SmsResponse;

public interface SmsGateway {
    SmsResponse send(SmsRequest request);
}
