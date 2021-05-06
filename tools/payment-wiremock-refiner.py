import json
import os
import re
import sys
filename = sys.argv[1]
masking_string = 'HASHED:'
linechanger = '\n'
state = 'STATE'
state_count = 1
api_call_count = 1
-white_list_headers = ['Accept', 'Content-Type', 'Date', 'x-fapi-financial-id', 'x-fapi-interaction-id' ]
parsed_content = []
parsing_phase = ''

def prepare_new_file_content(content):
  	if masking_string in content:
  	    parsed_content.append(content.replace(masking_string, 'MASKED'))
   	else:
   	    parsed_content.append(content)


def assign_set_match_state(line):
    #REQUET N / RESPONSE N
    pair = line.split(' ')
    #REQUEST / RESPONSE
    request_or_response = pair[0]
    #N
    number = int(pair[1])
    if number > 3 and request_or_response == 'REQUEST':
        return 'REQUEST ' + str(api_call_count) + ' MATCH ' + state + str(state_count) + linechanger
    elif number > 2 and request_or_response == 'RESPONSE':
        return 'RESPONSE ' + str(api_call_count) + ' SET ' + state + str(state_count) + linechanger
    else:
        return line

with open(filename) as f:
    for line in f:
        #REQUEST and giving a STATE
        if line.startswith('REQUEST'):
            prepare_new_file_content(assign_set_match_state(line))
            state_count = state_count + 1
        #RESPONSE and giving a STATE
        elif line.startswith('RESPONSE'):
            prepare_new_file_content(assign_set_match_state(line))
            api_call_count = api_call_count + 1
        #Empty Line
        elif line.rstrip() == '':
                prepare_new_file_content(line)
                if parsing_phase == 'HEADER':
                    parsing_phase = 'PAYLOAD'
        #HTTP Method and Url
        elif line.startswith('GET') or line.startswith('POST') or line.startswith('PUT') or line.startswith('DELETE'):
                prepare_new_file_content(line)
                parsing_phase = 'HEADER'
        #HTTP RESPONSE
        elif re.match('[0-9][0-9][0-9]',line.rstrip()):
                prepare_new_file_content(line)
                parsing_phase = 'HEADER'
        #JSON PAYLOAD REQUEST and RESPONSE
        elif line.startswith('{'):
            try:
                asJson = json.loads(line)
            except ValueError as e:
                prepare_new_file_content(line)
            else:
                prepare_new_file_content(json.dumps(asJson, indent=4, sort_keys=True) + linechanger)
        #Form PAYLOAD
        elif '=' in line and '&' in line and not ('/' in line):
            prepare_new_file_content(line)
        elif parsing_phase == 'PAYLOAD':
            prepare_new_file_content(line)
        #HEADERS Pattern
        elif line.split(":")[0] in white_list_headers:
            prepare_new_file_content(line)
        else:
            print 'Omitting ', line


print 'Only Keep the Headers Below'
print(white_list_headers)
print 'To add extra headers please add manually at white_list_headers in this py script'
print 'Input AAP File:', sys.argv[1]
head, tail = os.path.split(sys.argv[1])
outPutFilename = head + '/parsed_' + tail
print 'Output AAP File:', outPutFilename
f = open(outPutFilename, "w")
for x in parsed_content:
    f.write(x)
f.close()


javaFilename = head + '/BankenPaymentWiremockTest.java'
f = open(javaFilename, "w")
f.write( "import java.time.LocalDate;\n" +
                "import java.time.ZoneId;\n" +
                "import java.util.Date;\n" +
                "import org.junit.Test;\n" +
                "import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.AgentWireMockPaymentTest;\n" +
                "import se.tink.backend.aggregation.agents.framework.compositeagenttest.wiremockpayment.command.TransferCommand;\n" +
                "import se.tink.backend.aggregation.agents.utils.remittanceinformation.RemittanceInformationUtils;\n" +
                "import se.tink.backend.aggregation.configuration.AgentsServiceConfigurationReader;\n" +
                "import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;\n" +
                "import se.tink.libraries.account.AccountIdentifier;\n" +
                "import se.tink.libraries.amount.ExactCurrencyAmount;\n" +
                "import se.tink.libraries.enums.MarketCode;\n" +
                "import se.tink.libraries.payment.rpc.Creditor;\n" +
                "import se.tink.libraries.payment.rpc.Debtor;\n" +
                "import se.tink.libraries.payment.rpc.Payment;\n" +
                "import se.tink.libraries.transfer.enums.TransferType;\n" +
                "import se.tink.libraries.transfer.rpc.RemittanceInformation;\n" +
                "import se.tink.libraries.transfer.rpc.Transfer;\n" +
                "\n" +
                "public class BankenPaymentWiremockTest {\n" +
                "    private static final String CONFIGURATION_PATH = \"\";\n" +
                "\n" +
                "    @Test\n" +
                "    public void testPayment() throws Exception {\n" +
                "\n" +
                "        // given\n" +
                "        final String wireMockFilePath = \"\";\n" +
                "\n" +
                "        final AgentsServiceConfiguration configuration =\n" +
                "                AgentsServiceConfigurationReader.read(CONFIGURATION_PATH);\n" +
                "\n" +
                "        final AgentWireMockPaymentTest agentWireMockPaymentTest =\n" +
                "                AgentWireMockPaymentTest.builder(MARKET, providerName, wireMockFilePath)\n" +
                "                        .withConfigurationFile(configuration)\n" +
                "                        .addCallbackData(\"code\", \"DUMMY_AUTH_CODE\")\n" +
                "                        .withTransfer(createMockPayment())// Keep me and remove the line below\n" +
                "                        .withPayment(createMockedDomesticPayment()) //Keep me and remove the line above\n" +
                "                        .withHttpDebugTrace()\n" +
                "                        .buildWithoutLogin(TransferCommand.class);\n" +
                "\n" +
                "        agentWireMockPaymentTest.executePayment();\n" +
                "    }\n" +
                "\n" +
                "    private Transfer createMockPayment() {\n" +
                "        Transfer transfer = new Transfer();\n" +
                "        transfer.setSource(AccountIdentifier.create(AccountIdentifierType.A_TPYE, \"\"));\n" +
                "        transfer.setDestination(AccountIdentifier.create(AccountIdentifierType.A_TYPE, \"\"));\n" +
                "        transfer.setAmount(ExactCurrencyAmount.in);\n" +
                "        transfer.setType(TransferType.PAYMENT);\n" +
                "        transfer.setDueDate(\n" +
                "                Date.from(LocalDate.of(2020, 6, 22).atStartOfDay(ZoneId.of(\"CET\")).toInstant()));\n" +
                "        RemittanceInformation remittanceInformation = new RemittanceInformation();\n" +
                "        remittanceInformation.setValue(\"\");\n" +
                "        transfer.setRemittanceInformation(remittanceInformation);\n" +
                "\n" +
                "        return transfer;\n" +
                "    }\n" +
                "\n" +
                "    private Payment createMockedDomesticPayment() {\n" +
                "        ExactCurrencyAmount amount = ExactCurrencyAmount.of(\"1.00\", \"TINK DOLLAR\");\n" +
                "        LocalDate executionDate = LocalDate.now();\n" +
                "        String currency = \"TINK DOLLAR\";\n" +
                "        return new Payment.Builder()\n" +
                "                .withCreditor(\n" +
                "                        new Creditor(\n" +
                "                                AccountIdentifier.create(\n" +
                "                                        AccountIdentifierType.A_TYPE, \"\"),\n" +
                "                                \"Recipient Name\"))\n" +
                "                .withDebtor(new Debtor(AccountIdentifier.create(AccountIdentifierType.A_TYPE, \"\")))\n" +
                "                .withExactCurrencyAmount(amount)\n" +
                "                .withExecutionDate(executionDate)\n" +
                "                .withCurrency(currency)\n" +
                "                .withRemittanceInformation(\n" +
                "                        RemittanceInformationUtils.generateUnstructuredRemittanceInformation(\n" +
                "                                \"\"))\n" +
                "                .withUniqueId(\"\")\n" +
                "                .build();\n" +
                "    }\n" +
                "}")

print 'Output java File:', javaFilename
