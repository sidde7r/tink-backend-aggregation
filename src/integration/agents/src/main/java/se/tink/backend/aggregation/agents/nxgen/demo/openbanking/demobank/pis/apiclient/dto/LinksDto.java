package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.pis.apiclient.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import se.tink.backend.aggregation.agents.Href;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class LinksDto {

    private Href self;
    private Href updatePsuAuthenticationRedirect;
    private Href scaRedirect;
    private Href selectAuthenticationMethod;
    private Href authoriseTransaction;
    private Href feePaymentConfirmation;
    private Href scaOAuth;
}
