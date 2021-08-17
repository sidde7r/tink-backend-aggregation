package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit.fetcher;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionalAccountMapper;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party.Role;

public class UnicreditITTransactionalAccountMapper extends UnicreditTransactionalAccountMapper {

    private static final Pattern JOINT_ACCOUNT_SEPARATOR_PATTERN =
            Pattern.compile("( e |, )", Pattern.CASE_INSENSITIVE);

    @Override
    protected List<Party> parseOwnerName(String ownerName) {
        return JOINT_ACCOUNT_SEPARATOR_PATTERN
                .splitAsStream(ownerName)
                .map(singleOwner -> new Party(singleOwner, Role.HOLDER))
                .collect(Collectors.toList());
    }
}
