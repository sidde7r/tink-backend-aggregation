package se.tink.backend.main.providers.twilio;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import java.util.Objects;
import java.util.regex.Pattern;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.common.config.TwilioConfiguration;
import se.tink.libraries.net.BasicJerseyClientFactory;
import se.tink.backend.utils.LogUtils;

public class TwilioClient {
    private static final LogUtils log = new LogUtils(TwilioClient.class);

    private static final String STORE_LINK = "http://m.onelink.me/82b5cdcf";

    private final ApacheHttpClient4 client;
    private static final String BASE_URL = "https://api.twilio.com/";
    private static final String SEND_URI = "2010-04-01/Accounts/ACa473d47e71cd8a4be381dbc0dbf42c85/Messages";

    public TwilioClient(TwilioConfiguration configuration) {
        BasicJerseyClientFactory clientFactory = new BasicJerseyClientFactory();
        client = clientFactory.createCookieClient();
        client.addFilter(new HTTPBasicAuthFilter(configuration.getAccountSid(), configuration.getAuthToken()));
    }

    public boolean sendStoreLinks(String recipient) {
        recipient = extrapolatePhoneNumber(recipient);

        if (recipient == null) {
            return false;
        }

        SmsRequest request = new SmsRequest();
        request.setRecipient(recipient);
        request.setMessage(String.format("Ladda ned Tink och få till din ekonomi! \n%s", STORE_LINK));

        try {
            SmsResponse response = createClientRequest(SEND_URI).post(SmsResponse.class, request);

            return !Strings.isNullOrEmpty(response.getSid()) && !Objects.equals(response.getErrorCode(), "21211");
        } catch (UniformInterfaceException e) {
            log.error(String.format("[Twilio]: Couldn't sendStoreLinks store links to( %s ), response: %s",
                    mask(recipient), e.getResponse().getEntity(String.class)));

            return false;
        }
    }

    String extrapolatePhoneNumber(String phoneNumber) {
        if (Strings.isNullOrEmpty(phoneNumber)) {
            return null;
        }

        phoneNumber = phoneNumber.replaceAll("\\s|-", "");

        if (phoneNumber.startsWith("07")) {
            phoneNumber = phoneNumber.replaceFirst("^07", "+467");
        } else if (phoneNumber.startsWith("0046")) {
            phoneNumber = phoneNumber.replaceFirst("^0046", "+46");
        }

        if (!Pattern.matches("^\\+467\\d{8}$", phoneNumber)) {
            log.warn("[Twilio]: Invalid phone number: " + mask(phoneNumber));
            return null;
        }

        return phoneNumber;
    }

    private WebResource.Builder createClientRequest(String uri) {
        return client.resource(BASE_URL + uri + ".json")
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE);
    }

    private String mask(String phoneNumber) {
        return StringUtils.rightPad(phoneNumber.substring(0, phoneNumber.length()), phoneNumber.length());
    }
}
