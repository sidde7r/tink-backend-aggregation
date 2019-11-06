package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

public class JyskeConfiguration implements ClientConfiguration {

    private static final Base64.Decoder BASE64_DECODER = Base64.getDecoder();

    @JsonProperty @Secret private String productNemIdPublicKeyB64;
    @JsonProperty @Secret private String aesPadding;
    @JsonProperty @Secret private String mobileServicePublicKeyB64;

    private RSAPublicKey productNemIdPublicKey;
    private RSAPublicKey mobileServicePublicKey;

    public void setProductNemIdPublicKeyB64(String productNemIdPublicKeyB64) {
        this.productNemIdPublicKeyB64 = productNemIdPublicKeyB64;
        String certificate =
                new String(BASE64_DECODER.decode(productNemIdPublicKeyB64), StandardCharsets.UTF_8);
        productNemIdPublicKey =
                JyskeSecurityHelper.convertToPublicKey(
                        certificate.getBytes(JyskeConstants.CHARSET));
    }

    public void setMobileServicePublicKeyB64(String mobileServicePublicKeyB64) {
        this.mobileServicePublicKeyB64 = mobileServicePublicKeyB64;
        mobileServicePublicKey =
                RSA.getPubKeyFromBytes(BASE64_DECODER.decode(mobileServicePublicKeyB64));
    }

    public RSAPublicKey getProductNemIdPublicKey() {
        return productNemIdPublicKey;
    }

    public String getAesPadding() {
        return aesPadding;
    }

    public RSAPublicKey getMobileServicePublicKey() {
        return mobileServicePublicKey;
    }
}
