package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.component.loan.detail;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.util.Lists;
import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.NovoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response.AccountDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.detail.LoanAccountDto;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.GetAccountsResponse;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.rpc.loan.GetLoanDetailsResponse;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Ignore
public class LoanTestData {
    public static final String PAYLOAD_ACCOUNT_ID = "330000123456";
    public static final String ACCOUNT_DESCR = "Conta DO";
    public static final String ACCOUNT_IBAN = "PT50000201231234567890154";
    public static final String LOAN_CONTRACT_ID_1 = "0140000005";
    public static final String LOAN_RATE_INTEREST_1 = "0,03";
    public static final String LOAN_INITIAL_BALANCE_1 = "97.488,31";
    public static final String LOAN_CURRENT_BALANCE_1 = "80.140,34";
    public static final String LOAN_CURRENCY_1 = "EUR";
    public static final String LOAN_INIT_DATE_1 = "14-03-2008";
    public static final String LOAN_PRODUCT_NAME_1 = "BHS SWAP - AQUISICAO C/ HIPO";

    public static final String LOAN_CONTRACT_ID_2 = "0140000402";
    public static final String LOAN_RATE_INTEREST_2 = "0,05";
    public static final String LOAN_INITIAL_BALANCE_2 = "146.510,79";
    public static final String LOAN_CURRENT_BALANCE_2 = "142.025,79";
    public static final String LOAN_CURRENCY_2 = "EUR";
    public static final String LOAN_INIT_DATE_2 = "02-11-2018";
    public static final String LOAN_PRODUCT_NAME_2 = "BHP - AQUISICAO DEFICIENTES";

    private static final Map<String, String> loanDetailsResponse = new HashMap<>();
    private static final Map<String, LoanAccountDto> referenceLoanAccountDtos = new HashMap<>();

    public static List<NovoBancoApiClient.LoanAggregatedData> getLoanData() {
        AccountDetailsEntity accountWithLoans = getAccounts().iterator().next();

        return Lists.newArrayList(
                new NovoBancoApiClient.LoanAggregatedData(
                        accountWithLoans,
                        LOAN_CONTRACT_ID_1,
                        getLoanDetailsResponse(LOAN_CONTRACT_ID_1)),
                new NovoBancoApiClient.LoanAggregatedData(
                        accountWithLoans,
                        LOAN_CONTRACT_ID_2,
                        getLoanDetailsResponse(LOAN_CONTRACT_ID_2)));
    }

    public static LoanAccountDto getReferenceLoanAccountDto(String contractId) {
        return referenceLoanAccountDtos.get(contractId);
    }

    public static GetLoanDetailsResponse getLoanDetailsResponse(String contractId) {
        return getDeserialized(loanDetailsResponse.get(contractId), GetLoanDetailsResponse.class);
    }

    private static <T> T getDeserialized(String payload, Class<T> cls) {
        return SerializationUtils.deserializeFromString(payload, cls);
    }

    private static Collection<AccountDetailsEntity> getAccounts() {
        GetAccountsResponse response =
                SerializationUtils.deserializeFromString(
                        getAccountsResponse(), GetAccountsResponse.class);

        return Optional.of(response.getAccountDetailsEntities())
                .map(Collection::stream)
                .orElse(Stream.empty())
                .collect(Collectors.toList());
    }

    private static String getAccountsResponse() {
        return "{\n"
                + "  \"Header\": {\n"
                + "    \"ResponseId\": \"508a719f7c994e4f96d479b76ebd7bf3\",\n"
                + "    \"OpToken\": \"f83d3575887b46f18e339c83303a93d1\",\n"
                + "    \"Time\": \"2019-11-15T12:58:15.0733009Z\",\n"
                + "    \"SessionTimeout\": 1800,\n"
                + "    \"Status\": {\n"
                + "      \"Severidade\": 0,\n"
                + "      \"Codigo\": 0\n"
                + "    },\n"
                + "    \"Contexto\": {\n"
                + "      \"Contas\": {\n"
                + "        \"Lista\": [{\n"
                + "          \"Id\": \""
                + PAYLOAD_ACCOUNT_ID
                + "\",\n"
                + "          \"Iban\": \""
                + ACCOUNT_IBAN
                + "\",\n"
                + "          \"Desc\": \""
                + ACCOUNT_DESCR
                + "\"\n"
                + "        }],\n"
                + "        \"Selected\": \""
                + PAYLOAD_ACCOUNT_ID
                + "\"\n"
                + "      }\n"
                + "    }\n"
                + "  },\n"
                + "  \"Body\": {\n"
                + "    \"DataHoje\": \"2019-11-15\",\n"
                + "    \"Movimentos\": null,\n"
                + "    \"Saldo\": {\n"
                + "      \"Disponivel\": 650.37,\n"
                + "      \"Cativo\": 0.0,\n"
                + "      \"Contabilistico\": 650.37,\n"
                + "      \"Autorizado\": 650.37,\n"
                + "      \"Descoberto\": 0.0,\n"
                + "      \"Moeda\": \"EUR\"\n"
                + "    },\n"
                + "    \"Moeda\": \"EUR\",\n"
                + "    \"ComSCA\": false\n"
                + "  }\n"
                + "}";
    }

    static {
        loanDetailsResponse.put(
                LOAN_CONTRACT_ID_1,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"473b0d793f4744c68eb560a536179546\",\n"
                        + "    \"OpToken\": \"149d638406f04b978b5ba1902224b3f0\",\n"
                        + "    \"Time\": \"2019-11-15T13:19:34.7376913Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Severidade\": 0,\n"
                        + "      \"Codigo\": 0\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"Detalhe\": {\n"
                        + "      \"Header\": {\n"
                        + "        \"Titulo\": \"BHS SWAP - AQUISICAO C/ HIPO\",\n"
                        + "        \"SubTitulo\": {\n"
                        + "          \"AM\": 1,\n"
                        + "          \"T\": 3,\n"
                        + "          \"L\": \"Próxima prestação - Nº 140\",\n"
                        + "          \"V\": \"EUR\",\n"
                        + "          \"DV\": 192.04\n"
                        + "        },\n"
                        + "        \"Linhas\": [{\n"
                        + "          \"L\": \"Data da próxima prestação\",\n"
                        + "          \"V\": \"08-12-2019\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"Spread\",\n"
                        + "          \"V\": \"0,40%\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"TAN\",\n"
                        + "          \"V\": \""
                        + LOAN_RATE_INTEREST_1
                        + "%"
                        + "\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"Capital em divida\",\n"
                        + "          \"V\": \""
                        + LOAN_CURRENT_BALANCE_1
                        + " EUR"
                        + "\"\n"
                        + "        }]\n"
                        + "      },\n"
                        + "      \"Linhas\": [{\n"
                        + "        \"T\": 30,\n"
                        + "        \"Linhas\": [{\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Contrato\",\n"
                        + "          \"V\": \""
                        + LOAN_CONTRACT_ID_1
                        + "\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Designação\",\n"
                        + "            \"V\": \""
                        + LOAN_PRODUCT_NAME_1
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Data Início do contrato\",\n"
                        + "            \"V\": \""
                        + LOAN_INIT_DATE_1
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital contratado\",\n"
                        + "            \"V\": \"97.488,31 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital utilizado\",\n"
                        + "            \"V\": \""
                        + LOAN_INITIAL_BALANCE_1
                        + " EUR"
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Prazo contratado\",\n"
                        + "            \"V\": \"564\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Moeda\",\n"
                        + "            \"V\": \""
                        + LOAN_CURRENCY_1
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Regime\",\n"
                        + "            \"V\": \"Geral\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Spread\",\n"
                        + "            \"V\": \"0,40%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação ao spread\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Penalização ao spread\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Escalão de bonificação\",\n"
                        + "            \"V\": \"0\"\n"
                        + "          }]\n"
                        + "        }, {\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Próxima prestação\",\n"
                        + "          \"V\": \"Nº 140\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Data valor\",\n"
                        + "            \"V\": \"08-12-2019\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Montante da prestação\",\n"
                        + "            \"V\": \"192,04 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital amortizado\",\n"
                        + "            \"V\": \"188,73 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital em divida na data de emissão do aviso\",\n"
                        + "            \"V\": \"79.951,61 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Indexante\",\n"
                        + "            \"V\": \"-0,41%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"TAN\",\n"
                        + "            \"V\": \"-0,01%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros\",\n"
                        + "            \"V\": \"-0,33 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação de juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Comissão de liquidação\",\n"
                        + "            \"V\": \"3,50 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                        + "            \"V\": \"0,14 EUR\"\n"
                        + "          }]\n"
                        + "        }, {\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Última prestação paga\",\n"
                        + "          \"V\": \"Nº 139\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Data valor\",\n"
                        + "            \"V\": \"08-11-2019\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Montante da prestação\",\n"
                        + "            \"V\": \"192,04 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital amortizado\",\n"
                        + "            \"V\": \"188,73 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital em divida na data de emissão do aviso\",\n"
                        + "            \"V\": \"80.140,34 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Indexante\",\n"
                        + "            \"V\": \"-0,41%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"TAN\",\n"
                        + "            \"V\": \"-0,01%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros\",\n"
                        + "            \"V\": \"-0,33 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação de juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Comissão de liquidação\",\n"
                        + "            \"V\": \"3,50 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                        + "            \"V\": \"0,14 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto s/comissão de atraso\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros de Mora\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto s/juros de mora\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }]\n"
                        + "        }]\n"
                        + "      }]\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");
        loanDetailsResponse.put(
                LOAN_CONTRACT_ID_2,
                "{\n"
                        + "  \"Header\": {\n"
                        + "    \"ResponseId\": \"1512cab7baf3419ab39ac94208d524da\",\n"
                        + "    \"OpToken\": \"149d638406f04b978b5ba1902224b3f0\",\n"
                        + "    \"Time\": \"2019-11-15T13:19:58.1363913Z\",\n"
                        + "    \"SessionTimeout\": 1800,\n"
                        + "    \"Status\": {\n"
                        + "      \"Severidade\": 0,\n"
                        + "      \"Codigo\": 0\n"
                        + "    }\n"
                        + "  },\n"
                        + "  \"Body\": {\n"
                        + "    \"Detalhe\": {\n"
                        + "      \"Header\": {\n"
                        + "        \"Titulo\": \"BHP - AQUISICAO DEFICIENTES\",\n"
                        + "        \"SubTitulo\": {\n"
                        + "          \"AM\": 1,\n"
                        + "          \"T\": 3,\n"
                        + "          \"L\": \"Próxima prestação - Nº 13\",\n"
                        + "          \"V\": \"EUR\",\n"
                        + "          \"DV\": 377.39\n"
                        + "        },\n"
                        + "        \"Linhas\": [{\n"
                        + "          \"L\": \"Data da próxima prestação\",\n"
                        + "          \"V\": \"02-12-2019\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"Spread\",\n"
                        + "          \"V\": \"0,00%\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"TAN\",\n"
                        + "          \"V\": \""
                        + LOAN_RATE_INTEREST_2
                        + "%"
                        + "\"\n"
                        + "        }, {\n"
                        + "          \"L\": \"Capital em divida\",\n"
                        + "          \"V\": \""
                        + LOAN_CURRENT_BALANCE_2
                        + " EUR"
                        + "\"\n"
                        + "        }]\n"
                        + "      },\n"
                        + "      \"Linhas\": [{\n"
                        + "        \"T\": 30,\n"
                        + "        \"Linhas\": [{\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Contrato\",\n"
                        + "          \"V\": \""
                        + LOAN_CONTRACT_ID_2
                        + "\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Designação\",\n"
                        + "            \"V\": \""
                        + LOAN_PRODUCT_NAME_2
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Data Início do contrato\",\n"
                        + "            \"V\": \""
                        + LOAN_INIT_DATE_2
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital contratado\",\n"
                        + "            \"V\": \"146.510,79 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital utilizado\",\n"
                        + "            \"V\": \""
                        + LOAN_INITIAL_BALANCE_2
                        + " EUR"
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Prazo contratado\",\n"
                        + "            \"V\": \"392\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Moeda\",\n"
                        + "            \"V\": \""
                        + LOAN_CURRENCY_2
                        + "\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Regime\",\n"
                        + "            \"V\": \"Bonificado\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Tipo de taxa\",\n"
                        + "            \"V\": \"Variável\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Spread\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação ao spread\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Penalização ao spread\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Escalão de bonificação\",\n"
                        + "            \"V\": \"0\"\n"
                        + "          }]\n"
                        + "        }, {\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Próxima prestação\",\n"
                        + "          \"V\": \"Nº 13\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Data valor\",\n"
                        + "            \"V\": \"02-12-2019\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Montante da prestação\",\n"
                        + "            \"V\": \"377,39 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital amortizado\",\n"
                        + "            \"V\": \"373,75 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital em divida na data de emissão do aviso\",\n"
                        + "            \"V\": \"141.652,04 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Indexante\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"TAN\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação de juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Comissão de liquidação\",\n"
                        + "            \"V\": \"3,50 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                        + "            \"V\": \"0,14 EUR\"\n"
                        + "          }]\n"
                        + "        }, {\n"
                        + "          \"T\": 32,\n"
                        + "          \"L\": \"Última prestação paga\",\n"
                        + "          \"V\": \"Nº 12\",\n"
                        + "          \"Linhas\": [{\n"
                        + "            \"L\": \"Data valor\",\n"
                        + "            \"V\": \"02-11-2019\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Montante da prestação\",\n"
                        + "            \"V\": \"377,39 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital amortizado\",\n"
                        + "            \"V\": \"373,75 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Capital em divida na data de emissão do aviso\",\n"
                        + "            \"V\": \"142.025,79 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Indexante\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"TAN\",\n"
                        + "            \"V\": \"0,00%\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Bonificação de juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/juros\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Comissão de liquidação\",\n"
                        + "            \"V\": \"3,50 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto de selo s/comissão de liquidação\",\n"
                        + "            \"V\": \"0,14 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto s/comissão de atraso\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Juros de Mora\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }, {\n"
                        + "            \"L\": \"Imposto s/juros de mora\",\n"
                        + "            \"V\": \"0,00 EUR\"\n"
                        + "          }]\n"
                        + "        }]\n"
                        + "      }]\n"
                        + "    }\n"
                        + "  }\n"
                        + "}");
    }

    static {
        referenceLoanAccountDtos.put(
                LOAN_CONTRACT_ID_1,
                LoanAccountDto.builder()
                        .withAccountNumber(PAYLOAD_ACCOUNT_ID)
                        .withDescription(ACCOUNT_DESCR)
                        .withUniqueIdentifier(LOAN_CONTRACT_ID_1)
                        .withContractId(LOAN_CONTRACT_ID_1)
                        .withExactBalance(LOAN_CURRENT_BALANCE_1, LOAN_CURRENCY_1)
                        .withInitialBalance(LOAN_INITIAL_BALANCE_1, LOAN_CURRENCY_1)
                        .withInterestRate(LOAN_RATE_INTEREST_1)
                        .withInitialDate(LOAN_INIT_DATE_1)
                        .withProductName(LOAN_PRODUCT_NAME_1)
                        .build());

        referenceLoanAccountDtos.put(
                LOAN_CONTRACT_ID_2,
                LoanAccountDto.builder()
                        .withAccountNumber(PAYLOAD_ACCOUNT_ID)
                        .withDescription(ACCOUNT_DESCR)
                        .withUniqueIdentifier(LOAN_CONTRACT_ID_2)
                        .withContractId(LOAN_CONTRACT_ID_2)
                        .withExactBalance(LOAN_CURRENT_BALANCE_2, LOAN_CURRENCY_2)
                        .withInitialBalance(LOAN_INITIAL_BALANCE_2, LOAN_CURRENCY_2)
                        .withInterestRate(LOAN_RATE_INTEREST_2)
                        .withInitialDate(LOAN_INIT_DATE_2)
                        .withProductName(LOAN_PRODUCT_NAME_2)
                        .build());
    }
}
