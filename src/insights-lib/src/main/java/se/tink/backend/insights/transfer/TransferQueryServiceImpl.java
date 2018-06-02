package se.tink.backend.insights.transfer;

import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.common.repository.cassandra.TransferRepository;
import se.tink.backend.insights.core.valueobjects.EInvoice;
import se.tink.backend.insights.core.valueobjects.UserId;
import se.tink.backend.insights.transfer.mapper.EInvoiceMapper;
import se.tink.backend.utils.guavaimpl.Predicates;

public class TransferQueryServiceImpl implements TransferQueryService {

    private TransferRepository transferRepository;

    @Inject
    public TransferQueryServiceImpl(TransferRepository transferRepository) {
        this.transferRepository = transferRepository;
    }

    @Override
    public List<EInvoice> getEInvoices(UserId userId) {
        return transferRepository.findAllByUserId(userId.value())
                .stream()
                .filter(Predicates.TRANSFER_EINVOICES::apply)
                .map(EInvoiceMapper::translate)
                .collect(Collectors.toList());
    }
}
