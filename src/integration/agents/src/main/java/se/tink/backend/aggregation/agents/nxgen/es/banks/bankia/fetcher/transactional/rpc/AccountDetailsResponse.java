package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder;
import se.tink.backend.aggregation.nxgen.core.account.entity.Holder.Role;

@JsonObject
@Slf4j
public class AccountDetailsResponse {
    @JsonProperty("intervinientes")
    private List<Detail> details;

    public List<Holder> getHolders() {
        return details.stream().map(Detail::getHolderName).collect(Collectors.toList());
    }

    @JsonObject
    public static class Detail {
        @JsonProperty("tipoRelacion")
        private String holderCode;

        @JsonProperty("nombreRazonSocial")
        private String fullName;

        @JsonProperty("descripcionTipoRelacion")
        private String holderTitle;

        public Holder getHolderName() {
            return Match(holderCode)
                    .option(
                            Case($("020"), Holder.of(fullName, Role.HOLDER)),
                            Case($("025"), Holder.of(fullName, Role.AUTHORIZED_USER)),
                            Case($("027"), Holder.of(fullName, Role.AUTHORIZED_USER)),
                            Case(
                                    $(),
                                    () -> {
                                        log.warn(
                                                "The error code `{}` and holder title `{}` is unmapped}",
                                                holderCode,
                                                holderTitle);
                                        return Holder.of(fullName, Role.OTHER);
                                    }))
                    .get();
        }
    }
}
