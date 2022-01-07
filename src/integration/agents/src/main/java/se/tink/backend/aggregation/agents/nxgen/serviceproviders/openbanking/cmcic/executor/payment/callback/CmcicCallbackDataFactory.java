package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.executor.payment.callback;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.CaseInsensitiveMap;

@Slf4j
public class CmcicCallbackDataFactory {

    public CmcicCallbackData fromCallbackData(Map<String, String> callbackData) {
        // Query parameters can be case insesitive returned by bank,this is to take care of that
        // situation and we avoid failing the payment.
        Map<String, String> caseInsensitiveCallbackData = new CaseInsensitiveMap<>(callbackData);
        Map<String, String> urlDecodedCallbackData =
                decodeURLEncodedData(caseInsensitiveCallbackData);
        CmcicCallbackStatus status = CmcicCallbackStatus.extract(urlDecodedCallbackData.keySet());
        Map<String, String> expectedCallbackData =
                findExpectedCallbackData(status, urlDecodedCallbackData);
        Map<String, String> unexpectedCallbackData =
                findUnexpectedCallbackData(status, urlDecodedCallbackData);
        return new CmcicCallbackData(status, expectedCallbackData, unexpectedCallbackData);
    }

    private boolean isExpectedProperty(CmcicCallbackStatus status, Entry<String, String> entry) {
        return status.getExpectedProperties().contains(entry.getKey());
    }

    private Map<String, String> findExpectedCallbackData(
            CmcicCallbackStatus status, Map<String, String> callbackData) {
        return callbackData.entrySet().stream()
                .filter(entry -> isExpectedProperty(status, entry))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, String> findUnexpectedCallbackData(
            CmcicCallbackStatus status, Map<String, String> callbackData) {
        return callbackData.entrySet().stream()
                .filter(entry -> !isExpectedProperty(status, entry))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    private Map<String, String> decodeURLEncodedData(
            Map<String, String> caseInsensitiveCallbackData) {
        Map<String, String> urlDecodedCallbackData =
                new HashMap<>(caseInsensitiveCallbackData.size(), 1.0f);
        for (Entry<String, String> entry : caseInsensitiveCallbackData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            try {
                String decodedKey = URLDecoder.decode(key, StandardCharsets.UTF_8.toString());
                String decodedValue = URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
                urlDecodedCallbackData.put(decodedKey, decodedValue);
            } catch (UnsupportedEncodingException e) {
                log.warn("Callback data cannot be decoded.", e);
                urlDecodedCallbackData.put(key, value);
            }
        }
        return urlDecodedCallbackData;
    }
}
