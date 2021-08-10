package se.tink.backend.aggregation.events;

import com.google.inject.Inject;
import com.google.protobuf.Message;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Account;
import se.tink.backend.agents.rpc.HolderIdentity;
import se.tink.backend.agents.rpc.HolderRole;
import se.tink.eventproducerservice.events.grpc.AccountHoldersRefreshedEventProto;
import se.tink.libraries.serialization.proto.utils.ProtobufTypeUtil;

public class AccountHolderRefreshedEventProducer {

    private static final Logger log =
            LoggerFactory.getLogger(AccountHolderRefreshedEventProducer.class);

    @Inject
    public AccountHolderRefreshedEventProducer() {}

    public List<Message> produceEvents(
            String clusterId,
            String appId,
            String providerName,
            String correlationId,
            List<Account> accounts) {

        if (!clusterId.contains("oxford")) {
            return Collections.emptyList();
        }
        return accounts.stream()
                .filter(account -> account.getAccountHolder() != null)
                .map(a -> toEvent(a, clusterId, appId, providerName, correlationId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Message toEvent(
            Account account,
            String clusterId,
            String appId,
            String providerName,
            String correlationId) {
        try {
            AccountHoldersRefreshedEventProto.AccountHoldersRefreshedEvent.Builder builder =
                    AccountHoldersRefreshedEventProto.AccountHoldersRefreshedEvent.newBuilder()
                            .setTimestamp(ProtobufTypeUtil.toProtobufTimestamp(Instant.now()))
                            .setClusterId(clusterId)
                            .setAppId(appId)
                            .setUserId(account.getUserId())
                            .setProviderName(providerName)
                            .setCorrelationId(correlationId)
                            .setAccountId(account.getId())
                            .setHoldersCount(account.getAccountHolder().getIdentities().size())
                            .addAllHoldersRoles(
                                    account.getAccountHolder().getIdentities().stream()
                                            .map(HolderIdentity::getRole)
                                            .map(this::mapHolderRoleToGrpc)
                                            .collect(Collectors.toList()));

            return builder.build();

        } catch (RuntimeException e) {
            log.warn(
                    "Could not produce event: AccountHoldersRefreshedEvent for (userId, correlationId): ({}, {})",
                    account.getUserId(),
                    correlationId,
                    e);
            return null;
        }
    }

    private AccountHoldersRefreshedEventProto.HolderRole mapHolderRoleToGrpc(
            HolderRole holderRole) {
        if (holderRole == null) {
            return AccountHoldersRefreshedEventProto.HolderRole.HOLDER_ROLE_UNKNOWN;
        } else {
            switch (holderRole) {
                case HOLDER:
                    return AccountHoldersRefreshedEventProto.HolderRole.HOLDER_ROLE_HOLDER;
                case AUTHORIZED_USER:
                    return AccountHoldersRefreshedEventProto.HolderRole.HOLDER_ROLE_AUTHORIZED_USER;
                default:
                    return AccountHoldersRefreshedEventProto.HolderRole.HOLDER_ROLE_UNKNOWN;
            }
        }
    }
}
