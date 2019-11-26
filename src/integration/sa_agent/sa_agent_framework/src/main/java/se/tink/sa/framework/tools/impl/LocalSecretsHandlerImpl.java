package se.tink.sa.framework.tools.impl;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import se.tink.sa.framework.tools.SecretsHandler;

@Slf4j
@Getter
public class LocalSecretsHandlerImpl implements SecretsHandler {

    @Value("${secrets.service.clientId:@null}")
    private String clientId;

    @Value("${secrets.service.clientSecret:@null}")
    private String clientSecret;

    @Value("${secrets.service.certificate:@null}")
    private String certificate;

    @Value("${secrets.service.certificateSerialNumber:@null}")
    private String certificateSerialNumber;
}
