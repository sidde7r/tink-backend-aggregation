package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske.authenticator.JyskeSecurityHelper;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;

public class JyskePublicKeys {
    private RSAPublicKey productNemIdPublicKey;
    private RSAPublicKey mobileServicePublicKey;

    JyskePublicKeys() {
        this.productNemIdPublicKey =
                JyskeSecurityHelper.convertToPublicKey(
                        Base64.getDecoder().decode(JyskeConstants.Crypto.PRODUCT_NEMID_PUBLIC_KEY));
        this.mobileServicePublicKey =
                RSA.getPubKeyFromBytes(
                        Base64.getDecoder()
                                .decode(JyskeConstants.Crypto.MOBILE_SERVICE_PUBLIC_KEY));
    }

    RSAPublicKey getProductNemIdPublicKey() {
        return productNemIdPublicKey;
    }

    RSAPublicKey getMobileServicePublicKey() {
        return mobileServicePublicKey;
    }
}
