# Plataforma, Core e tenancy

**Status geral: ALVO.** Este modelo descreve o produto/plataforma desejado; não deve ser confundido com entidades já implementadas neste checkout.

Fontes principais: **Arquitetura de Produtos**, pp. 4, 6–9; **Registro Mestre**, pp. 3, 6–9.

## Modelo do ecossistema

VoidCube é concebida como sistema operacional empresarial composto por módulos reutilizáveis, add-ons, integrações, automações e capacidade computacional. O cliente trabalha com a visão da empresa e seus processos; a modularidade fica por baixo dessa experiência.

| Princípio | Registro |
| --- | --- |
| Reuso horizontal | Capacidades comuns, como Vendas, Pagamentos, Aprovações, Formulários, Documentos, Automações e Ordens de Serviço, são compartilhadas entre produtos. |
| Composição | Produtos, pacotes e templates compõem módulos e configurações; não criam implementações ou forks independentes. |
| Adoção incremental | O cliente começa pelas capacidades necessárias e adiciona módulos/add-ons conforme surgem novas necessidades. |
| Recursos compartilhados | Capacidade pode formar pools delegáveis entre Organização, Empresa e serviços. |
| Uso interno | A VoidCube deve usar seus próprios módulos sempre que aplicável. |

## Organização, empresas e grupos

- **Organização** é a raiz do isolamento lógico/tenant, faturamento, políticas e branding.
- Uma Organização pode conter grupos empresariais, empresas, filiais/unidades e estruturas funcionais. Uma Empresa pode participar de mais de um grupo dentro da Organização.
- **Empresa** é a entidade operacional que recebe licenças de módulos. A Organização pode comprar licenças em volume e distribuí-las às Empresas, conforme as regras comerciais em [COMERCIAL.md](COMERCIAL.md).
- Filiais/unidades podem ter CNPJ próprio. Uma unidade pode ser promovida a Empresa preservando o histórico, conforme evolução do modelo.
- Departamentos, equipes e centros de custo são dimensões organizacionais reutilizadas por RH, permissões, tarefas, Financeiro, Compras, Projetos e Patrimônio.

Relação conceitual:

```text
Organização (tenant)
├── Grupos empresariais
├── Empresas
│   ├── Filiais/unidades
│   ├── Departamentos
│   ├── Equipes
│   └── Centros de custo
├── Branding e políticas
└── Faturamento e licenças distribuídas
```

O desenho é conceitual, não exige que toda empresa use todos os níveis. A relação Empresa–Grupo pode ser muitos-para-muitos dentro da Organização.

## Identidade, vínculo e contexto

- A identidade da conta VoidCube é global e única; ela pode atuar em várias Organizações e Empresas.
- Os papéis nas Empresas são vínculos distintos, por exemplo funcionário, administrador, fornecedor, afiliado/parceiro ou cliente.
- Quando tem acesso a mais de uma Organização/Empresa, a pessoa seleciona um contexto ativo. O Dashboard continua sendo o mesmo produto, mas conteúdo e ações disponíveis variam com contexto, módulos e permissões.
- Administradores de grupos podem atuar em várias Empresas conforme permissões herdadas.
- O alvo de autorização pode combinar Usuário, Organização, Grupo, Empresa, Unidade, Departamento/Equipe, Módulo, Recurso, Registro e Ação.
- Exemplo de escopo: permitir acesso ao Financeiro de uma filial específica, limitado ao centro de custo Comercial.

Aplicações no backend devem validar autorização para o contexto e para o registro/ação. Receber um `organizationId` ou `companyId` na requisição não concede acesso por si só.

## VoidCube Core

O Core fornece a base organizacional, identidade e governança mesmo quando o cliente ainda não contratou módulos pagos. A IA não faz parte do Core gratuito.

Capacidades previstas:

| Capacidade | Papel |
| --- | --- |
| Organização/tenant | Raiz de isolamento, empresas, grupos, branding e políticas. |
| Empresas, unidades e estrutura | Empresas, filiais/unidades, departamentos, equipes e centros de custo. |
| Identidade e vínculos | Usuários e memberships para múltiplos contextos. |
| Cargos, perfis e permissões | Acesso reutilizável e granular por escopo. |
| Notificações | Caixa central de eventos e avisos da operação. |
| Auditoria | Ações humanas, do sistema, automações, integrações e IA. |
| Configurações e branding | Contexto operacional e identidade da Organização/cliente. |
| Playground | Montagem e simulação da operação e dos módulos antes da compra. |

## Dashboard

Todo cliente tem acesso ao Dashboard, inclusive quem compra somente uma Página de Vendas. Ele é o espaço de trabalho e leitura da saúde da empresa, não apenas uma tela de configuração.

- Apresenta alertas e tarefas operacionais, como recebíveis, pedidos aguardando envio, estoque baixo, pagamentos vencendo, atendimentos e próximos eventos.
- Organiza navegação por domínios reconhecíveis pelo empresário, por exemplo Início, Operação, Vendas, Pessoas, Financeiro, Análises e Automações.
- Não deve exibir como navegação permanente dezenas de módulos não contratados; recursos disponíveis para contratação ficam no Playground/área de adição.
- Pesquisa global e comandos rápidos respeitam permissões e contexto ativo.
- Deve mostrar recomendações ou ações relevantes quando aplicável, sem obrigar o cliente a conhecer termos internos de infraestrutura.

## Metadados e capacidades horizontais

### Tags

Tags globais podem ser reutilizadas entre Empresas, mas associações a registros permanecem privadas ao tenant/contexto. Tags privadas também são permitidas para vocabulário interno.

### Campos customizados

Módulos compatíveis podem aceitar campos configuráveis por Empresa/processo. Tipos previstos: texto, número, moeda, data/hora, booleano, seleção única/múltipla, usuário, empresa, arquivo, URL, telefone, e-mail e referência a outro registro.

### Policies / Rules Engine

Uma capacidade horizontal de regras deve atender segurança, permissões, aprovações, gastos, recursos, automações, IA, retenção, backup, cobrança e operação. Evite criar uma engine paralela dentro de cada módulo quando a regra puder ser compartilhada.

## Manifesto de módulo

Cada módulo deve declarar seu contrato de capacidades para alimentar catálogo, Playground, licenciamento, autorização, automações, documentação e provisionamento. O manifesto previsto contém:

- dependências obrigatórias e opcionais;
- integrações necessárias ou disponíveis;
- permissões;
- eventos emitidos e consumidos;
- ações expostas e automações disponíveis;
- campos customizáveis;
- limites, consumo e funcionalidades habilitadas;
- licença, mensalidade, entitlements e add-ons/extensões.

Se A exige B, B deve ser uma dependência explícita e contratada; não crie uma cópia técnica oculta de B como inclusão gratuita. Módulos principais ganham capacidades adicionais por add-ons/submódulos, em vez de níveis artificiais “Basic/Pro/Premium” como regra geral. Pacotes são composições comerciais identificáveis, não módulos duplicados.

## Identidade técnica e estado do repositório

O documento de produto define a direção de domínio acima. O checkout atual ainda é um esqueleto sem `Organization`, `Company`, `Membership`, autorização multi-tenant ou Dashboard. Consulte [`../ARCH.md`](../ARCH.md) antes de implementar; decisões de negócio não definem por si só tabelas, agregados ou uma migração concreta.
