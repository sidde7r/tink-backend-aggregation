package se.tink.backend.aggregation.service;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
public class SystemTest {

    private static final String HOST = "appundertest";
    private static final int PORT = 9095;

    @Test
    public void testPing() {
        // given
        String url = String.format("http://%s:%d/aggregation/ping", HOST, PORT);
        TestRestTemplate testRestTemplate = new TestRestTemplate();
        String response = testRestTemplate.getForObject(url, String.class);

        // then
        assertThat(response, equalTo("pong"));
    }
}
