package se.tink.ediclient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.Consts;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class EdiApiClient {
    private static final Logger LOG = LoggerFactory.getLogger(EdiApiClient.class);

    private static final String EDI_BASE_URL =
            "https://eidas-dev-issuer.staging.aggregation.tink.network";

    static byte[] pollForModulus(BigInteger modulus) throws IOException {
        String hexModulus = modulus.toString(16);

        while (true) {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet request = new HttpGet(EDI_BASE_URL + "/search/mod/" + hexModulus);
            HttpResponse response = httpClient.execute(request);
            if (response.getStatusLine().getStatusCode() == 200) {
                LOG.info("got a cert!");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                response.getEntity().writeTo(baos);
                return baos.toByteArray();
            } else if (response.getStatusLine().getStatusCode() == 404) {
                LOG.info("Still waiting");
            } else {
                LOG.info(response.getStatusLine().toString());
                throw new RuntimeException("FAILED");
            }
        }
    }

    static String urlForCsr(String csr) throws NoSuchAlgorithmException {
        String state =
                "confirm-" + EdiCryptoUtils.sha256(csr) + "-" + EdiCryptoUtils.randomString(10);
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("request", csr));
        params.add(new BasicNameValuePair("state", state));

        return EDI_BASE_URL + "/request.html#" + URLEncodedUtils.format(params, Consts.ASCII);
    }
}
