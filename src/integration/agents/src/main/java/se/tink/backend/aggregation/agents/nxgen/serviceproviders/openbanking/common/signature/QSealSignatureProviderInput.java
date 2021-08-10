package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.common.signature;

import java.util.List;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Getter
public class QSealSignatureProviderInput {

    @NotNull private CertificateSerialNumberType certificateSerialNumberType;
    @NotNull private HttpRequest request;

    @Size(min = 1)
    private List<String> signatureHeaders;

    @NotNull private String qseal;
}
