# Catálogo de produtos e módulos

Este arquivo registra o catálogo em nível de produto e domínio. **Os módulos abaixo são visão/alvo de produto; a presença no catálogo não significa que o código exista ou que a implementação esteja aprovada para o ciclo imediato.**

Fontes principais: **Arquitetura de Produtos**, pp. 4–18 e 26; **Registro Mestre**, pp. 3, 5–6, 10.

## Famílias de oferta

| Família | Descrição | Status/limite |
| --- | --- | --- |
| Sites e Lojas | Presença digital, campanhas, sites empresariais e comércio online em configurações padronizadas. | Produto modular. Necessidade fora dos componentes padrão vira Projeto sob Demanda. |
| Gestão da Empresa | Sistema operacional modular para a operação empresarial. | Plataforma base + módulos + add-ons + integrações. |
| Projeto sob Demanda | Necessidade que não cabe nos produtos e módulos padronizados. | Escopo customizado e contato comercial; “site customizado” não é quarto pacote de sites. |

## Produtos do catálogo comercial

O Registro Mestre de 17/10 lista como escopo aprovado para o catálogo público: Página de Vendas, Site Empresarial, Gestão Comercial/CRM, Gestão de Eventos e Controle de Acesso/Check-in. A situação comercial e os preços vigentes estão centralizados em [COMERCIAL.md](COMERCIAL.md).

| Produto | Dor/resultado principal | Expansões e relações |
| --- | --- | --- |
| Página de Vendas | Apresentar uma oferta/campanha e converter contatos ou vendas. | Site Empresarial, CRM, Automações, Pagamentos/checkout. |
| Site Empresarial | Presença e confiança institucional, com conteúdo mais amplo. | Página de Vendas, CRM, Loja Virtual e integrações. |
| Gestão Comercial / CRM | Leads esquecidos, histórico disperso e follow-up inconsistente. | Automações, Atendimento, Vendas, BI e sites. |
| Gestão de Eventos | Inscrições, vagas, participantes e presença organizadas. | Vendas, Pagamentos, Controle de Acesso e página do evento. |
| Controle de Acesso / Check-in | Filas, listas manuais, ausência de histórico e regras de entrada. | Eventos, RH/ponto, relatórios, Edge/offline e add-ons/hardware. |

Cross-sell deve partir do próximo gargalo observado no cliente, não da simples existência de outro produto.

## Sites e comércio digital

### Página de Vendas

- Uma página principal centrada em uma oferta; páginas auxiliares podem cobrir privacidade, termos, checkout, confirmação e agradecimento.
- Pode consumir Formulários, Analytics, pixels/rastreamento, WhatsApp e outros módulos horizontais.
- Cada página possui licença própria; páginas adicionais podem receber desconto progressivo ou compor pacote.
- Se o escopo evoluir para catálogo/comércio equivalente a uma Loja Virtual, a oferta deve migrar para essa composição em vez de criar um atalho comercial subprecificado.

### Site Completo / Site Empresarial

O conceito de produto do documento de arquitetura chama essa configuração de **Site Completo**; o catálogo comercial mais recente chama de **Site Empresarial**. O cliente escolhe páginas dentro do escopo padrão — por exemplo início, sobre, serviços, contato, portfólio e blog. Lógica específica fora dos componentes/módulos padrão deve ser avaliada como Projeto sob Demanda.

### Loja Virtual

Comércio para produtos físicos, digitais, serviços e assinaturas recorrentes; pode permitir afiliados e marketplace com múltiplos vendedores.

- **Catálogo:** produto, mídia, SKU, variação, categoria, coleção, tags, disponibilidade e publicação.
- **Estoque:** quantidade, reserva, disponibilidade, movimentação, localização, alerta, lote e validade quando aplicável.
- **Promoções:** cupons, descontos fixos/percentuais, frete grátis, validade, limites e regras por produto/categoria/cliente.
- **Pedidos:** criação, estados, cancelamento, reembolso, acompanhamento, observações e histórico.
- **Entrega:** Correios, transportadoras, motoboy, retirada, entrega própria e conectores.
- **Fiscal:** emissão/integração somente com o módulo Fiscal contratado e configurado.
- **Carrinho abandonado:** pode alimentar recuperação por e-mail/WhatsApp e recomendações no Dashboard.

Dependências exemplificativas da Loja: Vendas e Pagamentos; Estoque é opcional/recomendado conforme a operação; Fiscal e Atendimento também podem ser opcionais. Ver [AUTOMACOES_E_INTEGRACOES.md](AUTOMACOES_E_INTEGRACOES.md) para o Playground e [COMERCIAL.md](COMERCIAL.md) para pacotes.

## Catálogo de módulos horizontais

| Domínio | Módulo | Escopo registrado |
| --- | --- | --- |
| Comercial | CRM / Clientes | Pessoas, organizações, leads, oportunidades, pipelines, lifecycle, histórico, scoring e follow-up. |
| Comercial | Vendas | Propostas, orçamentos, pedidos, comissões, metas, cancelamentos, devoluções e reembolsos. |
| Comercial | Pagamentos | PIX, cartão, boleto, assinaturas e provedores; capacidade horizontal desacoplada do provedor. |
| Comercial | Cobrança | Recorrência, régua, inadimplência, recuperação e comunicações. |
| Comercial | Afiliados e Parceiros | Indicações, links/códigos, comissões, cupons, campanhas, saldo e portal. |
| Comercial | Atendimento | Conversas, filas, SLA, roteamento e canais conectáveis. |
| Comercial | Suporte / Chamados | Tickets, prioridade, SLA, responsável, setor, anexos, status e histórico. Pode atender clientes da empresa ou separar o suporte prestado pela VoidCube. |
| Comercial | Telefonia / VoIP | Canal de voz integrado ao Atendimento quando contratado. |
| Administrativo | Financeiro | Contas, fluxo de caixa, DRE, conciliação, centros de custo, aprovações, orçamento e relatórios. |
| Administrativo | Fiscal | Documentos fiscais e integrações fiscais. É opcional e configurado por contexto/jurisdição. |
| Administrativo | RH | Funcionários, cargos, ponto, férias, folha, benefícios, recrutamento, solicitações e avaliações. |
| Administrativo | Documentos | Arquivos, pastas, contratos, modelos, versões, validade, renovação e assinatura. Contratos ficam neste módulo, não em módulo separado. |
| Administrativo | Comunicação Interna | Mensagens, canais, equipes, menções e conversas ligadas a objetos. |
| Operação | Estoque | Saldo físico/reservado/disponível, movimentações, lote, validade, localização e limites. |
| Operação | Compras | Necessidade, cotação, pedido, aprovação, recebimento e divergências. Separado de Estoque para manter fronteiras e uso independente. |
| Operação | Fornecedores | Fornecedores privados, catálogo/rede VoidCube, cotações, pedidos e portal. |
| Operação | Agenda | Agendamentos internos/externos, recursos, profissionais, locais e campos configuráveis. |
| Operação | Eventos | Eventos presenciais, online ou híbridos; inscrição, participantes, presença e programação. |
| Operação | Controle de Acesso | Credenciais, áreas, horários, catracas, leitores, regras e operação offline. |
| Operação | Projetos e Tarefas | Projetos internos/de cliente, tarefas, recorrência, horas, aprovação e workflows. |
| Operação | Patrimônio | Bens, equipamentos, responsáveis, histórico e manutenção. |
| Operação | Manutenção | Manutenção preventiva/corretiva de ativos e equipamentos. |
| Operação | Ordem de Serviço | Capacidade horizontal para manutenção, instalação, assistência, projetos e trabalho de campo. |
| Operação | Logística | Entregas, motoristas, rotas, acompanhamento e integração com Vendas/Loja. |
| Operação | Localização | Geolocalização de visitas, chegadas e serviços quando habilitada. |
| Plataforma | Formulários | Construtor horizontal consumido por CRM, Sites, RH, Eventos, Agenda, Suporte e outros. |
| Plataforma | BI / Dashboards | Relatórios, painéis customizados e análise de dados de vários módulos sob permissão. |
| Plataforma | Automação | Regras, nodes, linguagem natural, eventos, ações e simulação. |
| Plataforma | Aprovações | Fluxos reutilizáveis para Financeiro, Compras, RH, Projetos e outros. |
| Plataforma | Infraestrutura | Capacidade, pools, Edge, hardware, backups, consumo, prioridades e auto scaling. |
| Plataforma | API / Webhooks | Exposição controlada de consultas, eventos e ações internas. |
| Plataforma | IA | Análise, recomendação e execução autorizada; cobrança por consumo. |

Esta lista serve como vocabulário de domínio; não é solicitação para criar todos os pacotes de código. Dependências obrigatórias/opcionais devem ser declaradas no manifesto de módulo (ver [PLATAFORMA.md](PLATAFORMA.md)).

## Regras de domínio por conjunto de produtos

### CRM e clientes

A entidade principal é Pessoa ou Organização. “Lead”, “Oportunidade” e “Cliente” são papéis, estágios de lifecycle e relações comerciais, não registros separados que se transformam e perdem histórico. O lifecycle ilustrativo é contato → lead → oportunidade → cliente.

- Pessoa física/jurídica e organizações com múltiplos contatos.
- Pipelines e estágios configuráveis.
- Histórico de WhatsApp, e-mail, ligações, reuniões, compras, propostas, tickets e documentos.
- Scoring tradicional; scoring assistido por IA é capacidade futura.
- Campos customizados por Empresa e processo.

### Vendas, Pagamentos e Cobrança

- Vendas é horizontal e pode ser consumido por Loja, Eventos, Página de Vendas, CRM, Agenda e venda manual.
- Proposta/orçamento aceita pode gerar venda/pedido; equipes, comissão e metas são configuráveis.
- Cancelamentos, devoluções e reembolsos são capacidades centralizadas.
- Pagamentos é desacoplado do provedor e pode atender Loja, Eventos, Agenda, Vendas, Cobrança e assinaturas; provedores possíveis incluem Mercado Pago, Stripe, PagBank, bancos ou integrações futuras.
- Cobrança trata cobrança única/recorrente, régua, recuperação de inadimplência, limites, automações, notificações e integração com Financeiro.

### Financeiro, Fiscal e RH

- Financeiro cobre bancos/caixa, movimentações/transferências, contas a pagar/receber, recorrência/parcelamento/atraso/juros/multas/descontos/anexos, conciliação manual/automatizada, fluxo de caixa previsto/realizado, DRE, orçamento, centros de custo/rateio, aprovações e visões por Empresa/Organização.
- Open Finance/conexão bancária depende de configuração.
- Fiscal é horizontal e opcional; pode servir Vendas, Loja, Financeiro etc. para NF-e, NFS-e e documentos/integradores fiscais conforme contrato e regras configuradas.
- RH cobre vínculos, cargos, departamentos, ponto, férias, ausências, atestados, solicitações, folha, benefícios, documentos, admissão/desligamento, onboarding, recrutamento e avaliação.
- Folha pode preparar dados para contador ou calcular integrada se as regras estiverem configuradas. Controle de Acesso pode alimentar ponto conforme regras da Empresa.
- Treinamentos obrigatórios e certificações de funcionários estão fora do escopo atual do módulo RH; reavaliar apenas com demanda recorrente.

### Estoque, Compras e Fornecedores

- Estoque separa quantidade física, reservada e disponível; movimentações, custo, localização, lote, validade, limites mínimo/ideal e alertas.
- Eventos como `estoque.baixo` podem disparar automações/análises.
- Compras tem fluxo próprio: necessidade → cotação → seleção de fornecedor → pedido → aprovação → recebimento → divergências.
- Fornecedores aceita cadastro privado e rede/catálogo VoidCube. Cotações podem chegar pelo site, notificações ou canais configurados; empresas comparam fornecedores e fornecedores podem publicar catálogo, preços, condições, região, prazo e disponibilidade.
- Marketplace B2B é parte da visão de produto.

### Eventos e Controle de Acesso

- Eventos cobre presença/online/híbrido, cadastro, datas, local, capacidade, inscrições, participantes, QR Code, presença, notificações, programação e relatórios.
- Evento online pode redirecionar para plataforma externa; não exige streaming próprio.
- Venda de ingressos não é embutida obrigatoriamente: Eventos consome Vendas quando ingressos forem comercializados.
- Controle de Acesso cobre pessoas, credenciais, áreas, horários, bloqueios, visitantes, validade e histórico; tecnologias possíveis: QR, RFID, NFC, cartão, PIN, biometria e reconhecimento facial.
- Reconhecimento facial pode vir do equipamento ou de add-on. O módulo também atende academia, empresa, escola e clube, não apenas eventos.
- Edge local mantém dados suficientes para decisão crítica de acesso e registra eventos durante falta de internet; sincroniza alterações/eventos quando conectividade volta.

### Agenda, Projetos, Documentos e Comunicação Interna

- Agenda configura duração, disponibilidade, profissional, local, sala/recurso, recorrência, confirmação, lembrete e cancelamento; pode servir equipe interna ou clientes. Pode consumir Vendas/Pagamentos para reserva cobrada.
- Projetos/Tarefas cobre dependências, responsáveis, prioridade, status, anexos, comentários, checklists, recorrência e horas; workflows de aprovação podem criar tarefas.
- Documentos combina pastas tradicionais e vínculo contextual; inclui contratos, validade/renovação, versionamento quando contratado e assinaturas simples, aceites eletrônicos ou digitais/certificadas como capacidades diferentes; pode haver múltiplos signatários.
- Comunicação Interna tem mensagens diretas/canais/equipes/menções/arquivos; conversas podem vincular a pedidos, projetos, clientes, fornecedores, compras e outros objetos.

### Atendimento, Suporte, Afiliados e Parceiros

- Atendimento centraliza conversas, filas, responsáveis, SLA, tags, prioridade e vínculo com cliente/Empresa; canais são conectores independentes (WhatsApp, Instagram, e-mail, chat e VoIP).
- Cada canal pode operar manualmente, por bot determinístico ou IA. Canal, automação e IA são conceitos distintos.
- WhatsApp pode ser manual/redirecionado, bot determinístico ou IA; conexão não oficial deve expor risco de bloqueio/indisponibilidade, oficial inclui custos externos/manutenção/margem e híbrida depende de arquitetura definida.
- Suporte/Chamados pode ser produto para os clientes atenderem seus consumidores e separado do suporte da própria VoidCube ao cliente.
- Afiliados/Parceiros abrange parceiros, afiliados, revendedores, agências e indicadores, com links/códigos, origem, comissão, regras, cupons, campanhas, saldo, pagamentos, relatórios e portal.

### Patrimônio, Manutenção, Ordem de Serviço, Logística e Localização

- Patrimônio controla equipamentos/bens, responsáveis, localização, histórico e manutenção.
- Manutenção preventiva/corretiva pode consumir Ordem de Serviço, Agenda, Estoque, Compras e Localização.
- Ordem de Serviço é horizontal para manutenção, assistência, instalação e trabalho de campo.
- Logística cobre entregas, motoristas, rotas e acompanhamento; pode integrar Vendas, Loja, Ordem de Serviço e Agenda.
- Localização é opcional para visitas, chegadas ou serviços de equipes externas.

### Formulários, BI e experiência operacional

- Formulários configura campos, validações e destino dos dados, podendo ser reutilizado por CRM, Sites, RH, Eventos, Agenda, Suporte etc.
- BI pode oferecer painéis prontos por módulo e painéis customizados multímódulo; filtros respeitam Organização, Empresa, filial, equipe, período, centro de custo e permissões.
- IA pode explicar variações e riscos dos dados se o usuário puder acessar a informação.

## Templates e dependências exemplificativas

Templates alteram campos, dashboards, automações e menus sem criar novos módulos/forks. Composições ilustrativas:

| Cenário | Composição possível |
| --- | --- |
| Comércio | Loja + Vendas + Pagamentos + Estoque + Financeiro + Fiscal opcional. |
| Oficina | CRM + Agenda + Ordem de Serviço + Estoque + Compras + Financeiro + Pagamentos. |
| Clínica | CRM + Agenda + Pagamentos + Financeiro + Documentos + Formulários. |
| Agência | CRM + Projetos/Tarefas + Vendas + Pagamentos + Documentos + BI. |
| Empresa de software | CRM + Projetos/Tarefas + Documentos + Automações + Suporte + Financeiro. |
| Eventos | Eventos + Vendas + Pagamentos + Controle de Acesso + Atendimento. |

São exemplos de composição e não definem oferta, preço ou dependências obrigatórias definitivas.
