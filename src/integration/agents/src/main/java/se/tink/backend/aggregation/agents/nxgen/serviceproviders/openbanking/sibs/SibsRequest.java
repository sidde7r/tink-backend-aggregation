package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import com.google.common.base.Strings;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.Formats;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.HeaderValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.utils.SibsUtils;
import se.tink.backend.aggregation.nxgen.http.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;

public class SibsRequest {

    private final RequestBuilder request;
    private final String transactionId;
    private final String requestId;
    private final String requestTimestamp;

    public RequestBuilder getRequest() {
        return request;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    private SibsRequest(
            RequestBuilder request,
            String transactionId,
            String requestId,
            String requestTimestamp) {
        this.request = request;
        this.transactionId = transactionId;
        this.requestId = requestId;
        this.requestTimestamp = requestTimestamp;
    }

    public static SibsRequestBuilder builder(
            TinkHttpClient client, SibsConfiguration configuration, URL url) {
        return new SibsRequestBuilder(client, configuration, url);
    }

    public static class SibsRequestBuilder {

        private final TinkHttpClient client;
        private final SibsConfiguration configuration;
        private final URL url;

        private String digest = null;
        private String consent = null;
        private boolean isSigned = false;

        public SibsRequestBuilder(TinkHttpClient client, SibsConfiguration configuration, URL url) {
            this.client = client;
            this.configuration = configuration;
            this.url = url;
        }

        public SibsRequestBuilder inSession(String consent) {
            this.consent = consent;
            return this;
        }

        public SibsRequestBuilder signed() {
            isSigned = true;
            return this;
        }

        public SibsRequestBuilder withDigest(String digest) {
            this.digest = digest;
            return this;
        }

        public SibsRequest build() {

            String transactionId = null;
            String requestId = null;
            String requestTimestamp = null;

            RequestBuilder request =
                    client.request(url)
                            .accept(MediaType.APPLICATION_JSON)
                            .type(MediaType.APPLICATION_JSON);

            if (isSigned) {

                transactionId = SibsUtils.getRequestId();
                requestId = SibsUtils.getRequestId();
                requestTimestamp =
                        new SimpleDateFormat(Formats.CONSENT_REQUEST_DATE_FORMAT)
                                .format(new Date());

                String signature =
                        SibsUtils.getSignature(
                                digest,
                                transactionId,
                                requestId,
                                requestTimestamp,
                                configuration.getClientSigningKeyPath(),
                                configuration.getClientSigningCertificateSerialNumber());

                request =
                        request.header(HeaderKeys.X_IBM_CLIENT_ID, configuration.getClientId())
                                .header(
                                        HeaderKeys.TPP_CERTIFICATE,
                                        SibsUtils.readSigningCertificate(
                                                configuration.getClientSigningCertificatePath()))
                                .header(HeaderKeys.SIGNATURE, signature)
                                .header(HeaderKeys.TPP_TRANSACTION_ID, transactionId)
                                .header(HeaderKeys.TPP_REQUEST_ID, requestId)
                                .header(HeaderKeys.DATE, requestTimestamp);
            }

            if (!Strings.isNullOrEmpty(consent)) {
                request = request.header(HeaderKeys.CONSENT_ID, consent);
            }

            if (!Strings.isNullOrEmpty(digest)) {
                request = request.header(HeaderKeys.DIGEST, HeaderValues.DIGEST_PREFIX + digest);
            }

            return new SibsRequest(request, transactionId, requestId, requestTimestamp);
        }
    }
}
