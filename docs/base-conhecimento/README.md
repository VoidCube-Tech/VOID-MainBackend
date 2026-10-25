# Base de conhecimento VoidCube

Esta pasta transforma os documentos PDF do projeto em registros Markdown pesquisáveis e organizados por assunto. É uma base de informação sobre plataforma, produtos, regras comerciais e operação; **não é roadmap nem plano de implementação por etapas**.

## Índice

| Arquivo | Consultar para |
| --- | --- |
| [PLATAFORMA.md](PLATAFORMA.md) | Organização/tenant, identidade, Core, Dashboard, permissões, metadados e contratos de módulos. |
| [PRODUTOS.md](PRODUTOS.md) | Catálogo comercial, módulos horizontais, escopo e relações entre produtos. |
| [COMERCIAL.md](COMERCIAL.md) | Preços aprovados, licenças, mensalidades, consumo, pacotes, cobrança, inadimplência e retenção. |
| [OPERACAO.md](OPERACAO.md) | Infraestrutura, capacidade, Edge, hardware, observabilidade, segurança, backups, canais white-label e exportação. |
| [AUTOMACOES_E_INTEGRACOES.md](AUTOMACOES_E_INTEGRACOES.md) | Eventos, ações, regras, automação, IA, Playground, API e webhooks. |
| [VENDAS_E_MARKETING.md](VENDAS_E_MARKETING.md) | Posicionamento, catálogo público, prospecção, canais, nichos, conteúdo e decisões de marketing. |

Use [`../ARCH.md`](../ARCH.md) para saber onde essas capacidades devem ficar no código. A base de conhecimento registra regras e intenção de produto; ela não prova que uma funcionalidade já esteja implementada.

## Como interpretar os registros

- **APROVADO**: decisão explícita que orienta produto, comunicação ou engenharia.
- **AÇÃO**: trabalho aprovado, mas ainda não concluído.
- **PENDENTE**: falta fechar escopo, preço, valor ou validação.
- **SUBSTITUÍDO**: hipótese ou decisão anterior superada por uma decisão posterior.
- **RECOMENDAÇÃO TÉCNICA**: conclusão de auditoria; não equivale a uma decisão de reunião.
- **EXEMPLO**: composição ou cenário ilustrativo, sem compromisso comercial por si só.
- **ALVO**: arquitetura desejada nos documentos, que pode não existir no código atual.

Os registros preservam a natureza original das afirmações. Uma recomendação ou exemplo não deve ser promovido a decisão aprovada sem nova confirmação.

## Fontes e precedência

1. Uma decisão explícita mais recente substitui hipótese ou decisão anterior sobre o mesmo assunto.
2. O `Registro Mestre de Decisões`, versão 1.1 de 17/10/2026, consolida decisões de produto e marketing e declara vigentes os preços aprovados em 16/10/2026. Valores anteriores desses três produtos são históricos/substituídos.
3. `Arquitetura de Produtos e Operação`, versão 1.0 de 14/10/2026, detalha o modelo de plataforma e o catálogo; pontos comerciais que ela deixou abertos continuam pendentes, salvo decisão posterior explícita.
4. O código e `docs/ARCH.md` descrevem o estado do repositório. Se um PDF mencionar classes ou funcionalidades que não existem neste checkout, trate isso como registro do snapshot auditado no PDF, não como código atual.
5. Onde não houver decisão explícita, preserve o status pendente e peça definição ou proponha uma decisão técnica; não invente regra de negócio.

## Nota sobre auditoria antiga do backend

O Registro Mestre descreve uma auditoria de 17/10/2026 que encontrou modelos `Tenant`, `User` e `Plan` em um snapshot do `VOID-MainBackend` e recomenda uma migração planejada para `Organization`, `Company` e `Membership`. O checkout descrito em [`../ARCH.md`](../ARCH.md) é hoje um esqueleto Spring Boot sem esses modelos. Portanto, use a direção de domínio como alvo, mas não presuma a existência de dados legados ou inicie uma migração sem verificar o código e o escopo atuais.

O PDF também registra PostgreSQL 16 no source auditado. A configuração atual deste repositório não fixa a versão do servidor; essa menção histórica não deve ser usada como configuração local vigente.

## Fontes originais

- [`../VoidCube_Decisoes_Consolidadas_2026-10-17.pdf`](../VoidCube_Decisoes_Consolidadas_2026-10-17.pdf) — Registro Mestre v1.1, 17/10/2026. As referências de páginas dos registros apontam para este PDF quando citadas como **Registro Mestre**.
- [`../voidcube_arquitetura_produtos_operacao.pdf`](../voidcube_arquitetura_produtos_operacao.pdf) — Arquitetura de Produtos e Operação v1.0, 14/10/2026. As referências citadas como **Arquitetura de Produtos** apontam para este PDF.

Os PDFs permanecem como evidência original. Em caso de dúvida sobre uma paráfrase, confira a página citada e mantenha o status definido pela fonte.
