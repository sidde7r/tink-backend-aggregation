package se.tink.backend.aggregation.nxgen.core.authentication;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2MultiTokenBase<T extends OAuth2TokenBase> {

    private List<T> tokens;

    public boolean isTokenValid() {
        return tokens.stream().allMatch(OAuth2TokenBase::isValid);
    }
}
