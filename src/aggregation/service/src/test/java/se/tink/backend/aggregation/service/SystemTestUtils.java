package se.tink.backend.aggregation.service;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

public class SystemTestUtils {

    public static ResponseEntity<String> makePostRequest(String url, Object requestBody)
            throws Exception {

        TestRestTemplate restTemplate = new TestRestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("X-Tink-App-Id", "00000000-0000-0000-0000-000000000000");
        headers.add("X-Tink-Client-Api-Key", "00000000-0000-0000-0000-000000000000");

        HttpEntity<Object> request = new HttpEntity<Object>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

        int status = response.getStatusCodeValue();
        if (status >= 300) {
            throw new Exception(
                    "Invalid HTTP response status "
                            + "code "
                            + status
                            + " from web service server.");
        }

        return response;
    }

    public static ResponseEntity<String> makeGetRequest(String url, HttpHeaders headers)
            throws Exception {
        TestRestTemplate restTemplate = new TestRestTemplate();

        ResponseEntity<String> response =
                new TestRestTemplate()
                        .exchange(
                                url, HttpMethod.GET, new HttpEntity<Object>(headers), String.class);

        int status = response.getStatusCodeValue();
        if (status >= 300) {
            throw new Exception(
                    "Invalid HTTP response status "
                            + "code "
                            + status
                            + " from web service server.");
        }

        return response;
    }
}
