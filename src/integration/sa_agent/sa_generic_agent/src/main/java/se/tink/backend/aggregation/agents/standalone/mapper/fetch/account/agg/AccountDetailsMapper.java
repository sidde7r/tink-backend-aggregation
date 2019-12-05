package se.tink.backend.aggregation.agents.standalone.mapper.fetch.account.agg;

import se.tink.backend.agents.rpc.AccountDetails;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;

public class AccountDetailsMapper
        implements Mapper<AccountDetails, se.tink.sa.services.fetch.account.AccountDetails> {

    private GoogleDateMapper googleDateMapper;

    public void setGoogleDateMapper(GoogleDateMapper googleDateMapper) {
        this.googleDateMapper = googleDateMapper;
    }

    @Override
    public AccountDetails map(
            se.tink.sa.services.fetch.account.AccountDetails source, MappingContext context) {
        final AccountDetails resp = new AccountDetails();
        resp.setInterest(source.getInterest());
        resp.setNumMonthsBound(source.getNumMonthsBound());
        resp.setType(source.getType());
        resp.setNextDayOfTermsChange(
                googleDateMapper.map(source.getNextDayOfTermsChange(), context));
        return resp;
    }
}
