import json
import os
import re
import sys
filename = sys.argv[1]
annoyingMaskedString = 'HASHED:'
linechanger = '\n'
state = 'STATE'
stateCount = 1
supportedHeader = ['Accept', 'Content-Type', 'Date', 'X-Request-ID']
parsedContent = []
parseStatus = ''

def prepareTheNewFile(content):
  	if annoyingMaskedString in content:
  	    parsedContent.append(content.replace(annoyingMaskedString, 'MASKED'))
   	else:
   	    parsedContent.append(content)


def assignSetMatch(line):
    #REQUET N / RESPONSE N
    pair = line.split(' ')
    #REQUEST / RESPONSE
    reqOrRes = pair[0]
    #N
    number = int(pair[1])
    if number > 3 and reqOrRes == 'REQUEST':
        return line.rstrip() + ' MATCH ' + state + str(stateCount) + linechanger
    elif number > 2 and reqOrRes == 'RESPONSE':
        return line.rstrip() + ' SET ' + state + str(stateCount) + linechanger
    else:
        return line

with open(filename) as f:
    for line in f:
        #REQUEST and giving a STATE
        if line.startswith('REQUEST'):
            prepareTheNewFile(assignSetMatch(line))
            stateCount = stateCount + 1
        #RESPONSE and giving a STATE
        elif line.startswith('RESPONSE'):
            prepareTheNewFile(assignSetMatch(line))
        #Empty Line
        elif line.rstrip() == '':
                prepareTheNewFile(line)
                if parseStatus == 'HEADER':
                    parseStatus = 'PAYLOAD'
        #HTTP Method and Url
        elif line.startswith('GET') or line.startswith('POST') or line.startswith('PUT') or line.startswith('DELETE'):
                prepareTheNewFile(line)
                parseStatus = 'HEADER'
        #HTTP RESPONSE
        elif re.match('[0-9][0-9][0-9]',line.rstrip()):
                prepareTheNewFile(line)
                parseStatus = 'HEADER'
        #JSON PAYLOAD REQUEST and RESPONSE
        elif line.startswith('{'):
            try:
                asJson = json.loads(line)
            except ValueError as e:
                prepareTheNewFile(line)
            else:
                prepareTheNewFile(json.dumps(asJson, indent=4, sort_keys=True) + linechanger)
        #Form PAYLOAD
        elif '=' in line and '&' in line and not ('/' in line):
            prepareTheNewFile(line)
        elif parseStatus == 'PAYLOAD':
            prepareTheNewFile(line)
        #HEADERS Pattern
        elif line.split(":")[0] in supportedHeader:
            prepareTheNewFile(line)
        else:
            print 'Omitting ', line


print 'Only Keep the Headers Below'
print(supportedHeader)
print 'To add extra headers please add manually at supportedHeader in this py script'
print 'Input AAP File:', sys.argv[1]
head, tail = os.path.split(sys.argv[1])
outPutFilename = head + '/parsed_' + tail
print 'Output AAP File:', outPutFilename
f = open(outPutFilename, "w")
for x in parsedContent:
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
