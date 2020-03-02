package se.tink.backend.aggregation.nxgen.core.authentication;

import java.util.List;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class HmacMultiToken extends OAuth2MultiTokenBase<HmacToken> {

    public HmacMultiToken(List<HmacToken> tokens) {
        super(tokens);
    }
}
