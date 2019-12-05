package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import java.net.URI;
import se.tink.backend.agents.rpc.TransferDestination;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class TransferDestinationMapper
        implements Mapper<
                TransferDestination, se.tink.sa.services.fetch.account.TransferDestination> {
    @Override
    public TransferDestination map(
            se.tink.sa.services.fetch.account.TransferDestination source, MappingContext context) {
        TransferDestination resp = new TransferDestination();
        resp.setBalance(source.getBalance());
        resp.setUri(URI.create(source.getUri().getPath()));
        resp.setName(source.getName());
        resp.setType(source.getType());
        return resp;
    }
}
