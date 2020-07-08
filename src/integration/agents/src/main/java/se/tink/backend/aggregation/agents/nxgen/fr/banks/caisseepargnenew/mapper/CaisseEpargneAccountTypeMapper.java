package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargnenew.mapper;

import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccountTypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;

public class CaisseEpargneAccountTypeMapper {

    private static final TransactionalAccountTypeMapper ACCOUNT_TYPE_MAPPER =
            TransactionalAccountTypeMapper.builder()
                    .put(TransactionalAccountType.CHECKING, "02", "04")
                    .put(TransactionalAccountType.SAVINGS, "10")
                    .build();

    public static Optional<TransactionalAccountType> getAccountType(String rawAccountType) {
        return ACCOUNT_TYPE_MAPPER.translate(rawAccountType);
    }
}
