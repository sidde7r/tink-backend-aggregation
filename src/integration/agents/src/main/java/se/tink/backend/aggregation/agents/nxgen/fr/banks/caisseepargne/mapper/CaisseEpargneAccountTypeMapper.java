package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.mapper;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class CaisseEpargneAccountTypeMapper {

    private static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "02", "04")
                    .put(TransactionalAccountType.SAVINGS, "10", "01", "06")
                    // AS is life insurance, 37 is investments
                    .ignoreKeys("AS", "37")
                    .build();

    public static Optional<TransactionalAccountType> getAccountType(String rawAccountType) {
        return ACCOUNT_TYPE_MAPPER.translate(rawAccountType);
    }
}
