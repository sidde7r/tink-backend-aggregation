package se.tink.backend.aggregation.agents.nxgen.es.banks.bankia.fetcher.transactional.rpc;

import static io.vavr.API.$;
import static io.vavr.API.Case;
import static io.vavr.API.Match;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

@JsonObject
@Slf4j
public class AccountDetailsResponse {
    @JsonProperty("intervinientes")
    private List<Detail> details;

    public List<Party> getParties() {
        return details.stream().map(Detail::getParty).collect(Collectors.toList());
    }

    @JsonObject
    public static class Detail {
        @JsonProperty("tipoRelacion")
        private String holderCode;

        @JsonProperty("nombreRazonSocial")
        private String fullName;

        @JsonProperty("descripcionTipoRelacion")
        private String holderTitle;

        public Party getParty() {
            return Match(holderCode)
                    .option(
                            Case($("020"), new Party(fullName, Role.HOLDER)),
                            Case($("025"), new Party(fullName, Role.AUTHORIZED_USER)),
                            Case($("027"), new Party(fullName, Role.AUTHORIZED_USER)),
                            Case(
                                    $(),
                                    () -> {
                                        log.warn(
                                                "The error code `{}` and holder title `{}` is unmapped}",
                                                holderCode,
                                                holderTitle);
                                        return new Party(fullName, Role.OTHER);
                                    }))
                    .get();
        }
    }
}
