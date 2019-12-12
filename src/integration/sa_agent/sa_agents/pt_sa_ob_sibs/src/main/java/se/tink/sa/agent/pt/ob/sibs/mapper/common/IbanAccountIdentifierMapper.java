package se.tink.sa.agent.pt.ob.sibs.mapper.common;

import org.springframework.stereotype.Component;
import se.tink.sa.agent.pt.ob.sibs.rest.client.transactionalaccount.entity.account.AccountEntity;
import se.tink.sa.common.mapper.Mapper;
import se.tink.sa.common.mapper.MappingContext;
import se.tink.sa.services.fetch.account.AccountIdentifier;
import se.tink.sa.services.fetch.account.AccountIdentifierType;
import se.tink.sa.services.fetch.account.AccountIdentifierTypeBasedField;
import se.tink.sa.services.fetch.account.AccountIdentifierTypeBasedFieldType;

@Component
public class IbanAccountIdentifierMapper implements Mapper<AccountIdentifier, AccountEntity> {

    @Override
    public AccountIdentifier map(AccountEntity source, MappingContext mappingContext) {
        AccountIdentifier.Builder destBuilder = AccountIdentifier.newBuilder();
        destBuilder.setType(AccountIdentifierType.IBAN);
        destBuilder.addAccountIdentifierTypeBasedFields(buildOnIban(source.getIban()));
        return destBuilder.build();
    }

    private AccountIdentifierTypeBasedField buildOnIban(String iban) {
        AccountIdentifierTypeBasedField.Builder builder =
                AccountIdentifierTypeBasedField.newBuilder();
        builder.setFieldName(AccountIdentifierTypeBasedFieldType.FIELD_IBAN);
        builder.setValue(iban);
        return builder.build();
    }
}
