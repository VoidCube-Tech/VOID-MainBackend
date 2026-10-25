# Modelo comercial, licenças e cobrança

Este registro consolida as regras comerciais encontradas nos PDFs. Use os status indicados: preços aprovados orientam comunicação e implementação; itens pendentes precisam de decisão antes de virarem regra de negócio.

Fontes principais: **Registro Mestre**, pp. 2–3, 6–10; **Arquitetura de Produtos**, pp. 18–24.

## Preços vigentes registrados

O Registro Mestre v1.1, de 17/10/2026, declara vigentes os preços aprovados em 16/10/2026. Eles substituem valores anteriores dos mesmos produtos.

| Produto | Licença inicial | Mensalidade | Status |
| --- | ---: | ---: | --- |
| Página de Vendas | R$ 800–1.500 | R$ 120/mês | APROVADO |
| Gestão Comercial / CRM | R$ 2.000 | R$ 350/mês | APROVADO |
| Gestão de Eventos | R$ 1.500 | R$ 100/mês | APROVADO |
| Site Empresarial | — | — | PENDENTE |
| Controle de Acesso / Check-in | — | — | PENDENTE |

Valores de implantação, add-ons, hardware, descontos por volume e preços de outros módulos não estão fechados nesses registros. Não extrapole os três preços aprovados para produtos diferentes.

## Unidade de licença e composição do preço

- A licença de um módulo ou add-on é atribuída a uma Empresa. A Organização pode comprar em volume e distribuir licenças às Empresas sob sua gestão.
- A licença é cobrada uma vez por aquisição; a mensalidade remunera suporte, manutenção, operação e capacidade contratada.
- O modelo não deve cobrar por usuário, funcionário, pedido ou registro. Preços podem variar por módulo, add-on, capacidade e consumo.
- Itens de uso variável podem incluir computação, armazenamento adicional, IA, APIs e serviços externos. O cliente precisa ver consumo, limite e projeção.
- Cada módulo deve declarar mensalidade, capacidade incluída, limites, consumo variável, integrações, dependências, add-ons e direitos de uso.

Composição recorrente conceitual:

**mensalidade recorrente = módulos e add-ons + capacidade computacional + armazenamento adicional + consumo variável + serviços externos + suporte especial − descontos/créditos aplicáveis**

A fórmula orienta a composição, mas coeficientes e faixas de preço ainda precisam de definição comercial.

## Continuidade da licença e versões

- A licença acompanha o módulo e as versões cobertas enquanto a assinatura mensal permanece ativa e regular.
- Uma interrupção superior a seis meses encerra o direito operacional associado à licença. Para reativar, é necessária nova compra segundo a política vigente.
- Não descreva a licença isoladamente como “vitalícia”: essa expressão omite as condições de continuidade, operação e suporte.
- Pacotes comerciais são composições de licenças independentes com desconto percentual e/ou capacidade bônus. O desconto acordado deve manter sua versão registrada.
- A Organização distribui licenças a Empresas; regras de transferência, devolução e substituição precisam ser explícitas no contrato e no sistema.

## Cobrança, créditos e limites

- A cobrança pode ser visualizada por Organização, Empresa, filial, centro de custo ou projeto, conforme a granularidade habilitada.
- Créditos pré-pagos podem reduzir cobrança futura. Limites podem existir no nível da Organização, Empresa ou módulo.
- O cliente deve poder configurar alertas e limites de consumo. A cobrança deve mostrar consumo atual e projeção de fechamento.
- A interrupção de serviço não deve continuar cobrando como se a operação estivesse ativa durante todo o período suspenso.
- O prazo e a frequência de mudanças de preço devem ser previsíveis. A preferência registrada é comunicar mudanças com antecedência próxima de um ano; exceções devem ser justificadas.

## Inadimplência, suspensão e retenção

Fluxo registrado para inadimplência:

1. Vencimento da cobrança.
2. Prazo de tolerância de sete dias; suporte pode estendê-lo por acordo.
3. Suspensão dos módulos após a tolerância.
4. Dados operacionais ficam retidos por até seis meses para exportação ou reativação.
5. Após aviso final, aguardar mais sete dias e então remover o banco operacional do cliente.

As regras associadas são:

- Meses suspensos não contam como meses operacionais integralmente faturáveis.
- Durante a retenção, o cliente pode regularizar e reativar o serviço ou exportar os próprios dados.
- Depois do encerramento do prazo e do aviso final, os dados operacionais podem ser excluídos.
- A política de backup é separada da retenção operacional. Se um backup antigo for restaurado, o sistema deve reaplicar os registros de exclusão de dados já solicitados.
- Prazos legais, formato do aviso, exceções de contrato e retenção de backups ainda precisam de revisão técnica e jurídica.

## Pontos comerciais pendentes

- Preços e limites do Site Empresarial e de Controle de Acesso / Check-in.
- Faixas e critérios para o preço da Página de Vendas.
- Preços dos módulos fora do catálogo comercial inicial.
- Capacidade incluída, unidade de consumo e coeficientes de computação e armazenamento.
- Regras exatas de desconto por volume, transferência de licenças e créditos.
- Prazos de reajuste, cobrança proporcional, impostos e exceções contratuais.
- Retenção legal, política de backup e procedimento de exclusão final.

Não codifique os itens pendentes como defaults permanentes. Se uma funcionalidade depender deles, registre a decisão pendente e mantenha a regra configurável somente quando já houver uma definição concreta.

