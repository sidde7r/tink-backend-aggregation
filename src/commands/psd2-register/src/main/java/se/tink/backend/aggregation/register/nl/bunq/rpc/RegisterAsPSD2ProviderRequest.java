package se.tink.backend.aggregation.register.nl.bunq.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RegisterAsPSD2ProviderRequest {
    @JsonProperty("client_payment_service_provider_certificate")
    private String clientPaymentServiceProviderCertificate;

    @JsonProperty("client_payment_service_provider_certificate_chain")
    private String clientPaymentServiceProviderCertificateChain;

    @JsonProperty("client_public_key_signature")
    private String clientPublicKeySignature;

    public RegisterAsPSD2ProviderRequest(
            String clientPaymentServiceProviderCertificate,
            String clientPaymentServiceProviderCertificateChain,
            String clientPublicKeySignature) {
        this.clientPaymentServiceProviderCertificate = clientPaymentServiceProviderCertificate;
        this.clientPaymentServiceProviderCertificateChain =
                clientPaymentServiceProviderCertificateChain;
        this.clientPublicKeySignature = clientPublicKeySignature;
    }
}
