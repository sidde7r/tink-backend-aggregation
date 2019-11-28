package se.tink.sa.framework.rest.client;

import java.util.Arrays;
import java.util.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import se.tink.sa.framework.common.exceptions.QsealcSignerException;
import se.tink.sa.framework.tools.QsealcAlg;

@Component
public class EidasProxyClient {

    private static final String TINK_QSEALC_APPID = "X-Tink-QSealC-AppId";
    private static final String TINK_QSEALC_CLUSTERID = "X-Tink-QSealC-ClusterId";
    private static final String TINK_REQUESTER = "X-SignRequester";

    @Value("${security.eidas.proxy.address}")
    private String eidasProxyAddress;

    @Value("${security.eidas.proxy.protocol:https}")
    private String eidasProxyProtocol;

    @Autowired
    @Qualifier("eidasRestTemplate")
    private RestTemplate restTemplate;

    public byte[] callSecretsService(
            byte[] toSign, String appId, String clusterId, QsealcAlg qsealcAlg, String requester) {

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_OCTET_STREAM));
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        if (StringUtils.isNotBlank(appId)) {
            headers.set(TINK_QSEALC_APPID, appId);
        }
        headers.set(TINK_QSEALC_CLUSTERID, clusterId);
        headers.set(TINK_REQUESTER, requester);

        HttpEntity httpEntity = new HttpEntity(Base64.getEncoder().encode(toSign), headers);

        ResponseEntity<byte[]> response =
                restTemplate.postForEntity(buildEidasUrl(qsealcAlg), httpEntity, byte[].class);

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new QsealcSignerException("Unexpected status code " + response.getStatusCode());
        }

        return response.getBody();
    }

    private String buildEidasUrl(QsealcAlg qsealcAlg) {
        StringBuilder sb =
                new StringBuilder()
                        .append(eidasProxyProtocol)
                        .append("://")
                        .append(eidasProxyAddress)
                        .append("/")
                        .append(qsealcAlg.getSigningType())
                        .append("/");
        return sb.toString();
    }
}
