# Operação, capacidade e segurança

Este arquivo descreve a operação esperada da plataforma VoidCube conforme os PDFs. É contexto de produto e alvo arquitetural, não prova de que os serviços já existam no backend atual.

Fontes principais: **Arquitetura de Produtos**, pp. 19–28; **Registro Mestre**, pp. 6–10.

## Recursos e capacidade

- A alocação pode ocorrer em níveis hierárquicos: Organização, Empresa, módulo, projeto ou serviço.
- A capacidade não utilizada pode ficar em um pool e ser redistribuída temporariamente, respeitando reservas e prioridades definidas pelo cliente.
- Capacidade deve ser descrita em unidades normalizadas de processamento, memória, armazenamento e GPU, além de limites por serviço.
- O cliente pode aumentar capacidade quando necessário. A redução não pode deixar o volume abaixo do uso atual.
- Mudanças de capacidade devem ter prazo operacional visível; o documento prevê janelas de até três dias úteis, sujeitas a configuração.
- Auto scaling deve respeitar limites técnicos e um teto financeiro configurado. Não deve expandir consumo sem controle de custo.
- Coeficientes de capacidade, recursos incluídos e limites de autoscaling permanecem PENDENTES.

## Infraestrutura, hardware e Edge

- O módulo Infrastructure é voltado à operação da infraestrutura VoidCube: recursos, disponibilidade, monitoramento, backup e hardware conectado.
- Isso não representa um console universal para administrar provedores externos como AWS ou Cloudflare.
- Equipamentos podem ser vendidos ou alugados; equipamentos comprados passam a pertencer ao cliente. Manutenção e suporte podem ter cobrança recorrente.
- Hardware de terceiros deve passar por homologação. Catálogo, compatibilidade, preço, garantia e responsabilidades de manutenção estão pendentes.
- Edge é um nó local para continuidade operacional, sincronização e execução offline limitada. O estado offline deve sincronizar posteriormente e preservar consistência e auditoria.
- Controle de Acesso e Eventos são os casos citados para operação com Edge/offline; as regras de conflito, retenção local e segurança ainda precisam de definição técnica.

## Observabilidade e suporte

- Exponha saúde e disponibilidade, uso de CPU/RAM/armazenamento, histórico de consumo e custo, incidentes e alertas.
- Alertas operacionais podem disparar automações conforme política e permissões do cliente.
- Logs técnicos devem ter acesso controlado; não exponha conteúdo sensível por padrão.
- Central de incidentes pode acompanhar status, responsáveis, contatos e comunicações. Uma página de status é opcional.
- O suporte é vendido por módulo/add-on ou incluído na mensalidade conforme o contrato. Horários, SLA e severidades ainda não foram definidos.

## Backups e recuperação

- Frequência e retenção de backup podem variar por Empresa e módulo.
- A operação deve possibilitar exportação pelo cliente e restauração no escopo disponível.
- Restauração granular de módulo/área depende de suporte técnico e consistência dos dados.
- Recuperação point-in-time é uma possibilidade futura, não requisito aprovado.
- Exclusões solicitadas pelo cliente não devem desaparecer ao restaurar backup antigo: aplicar novamente o registro de exclusão após a restauração.
- RPO, RTO, frequência, retenção de backups e validação periódica de restauração estão PENDENTES.

## Segurança, sessão e auditoria

- Criptografia em trânsito e em repouso é o baseline de segurança registrado.
- Sessões podem ter duração e restrições configuráveis por dispositivo ou localização, conforme a política.
- MFA deve ser suportado; administradores são recomendados a usar MFA. A exigência por perfil ainda está PENDENTE.
- SSO é uma possibilidade futura, sem decisão de escopo ou protocolo.
- Registros de auditoria devem identificar o ator: pessoa, IA, automação, sistema ou integração. A cadeia também deve indicar a autorização e o contexto aplicáveis.
- Logs, dados de autenticação e identificadores de tenant devem respeitar escopo e permissão.

## White-label, publicação e propriedade dos dados

- O padrão comercial exibe “Powered by VoidCube”. A remoção é uma opção premium.
- O modelo pode suportar branding, domínios e configuração visual por cliente ou parceiro.
- Licenciamento para parceiros, revenda, comissionamento, migração entre parceiros e troca de branding precisam de regras explícitas.
- O cliente é proprietário dos dados da operação e deve poder exportá-los.
- O backend não é auto-hospedado pelo cliente no encerramento. A continuidade pós-cancelamento depende das regras de retenção em [COMERCIAL.md](COMERCIAL.md).

## APIs e integrações

- API e webhooks devem expor os mesmos eventos e ações disponíveis aos módulos, respeitando permissões e escopos de Organização, Empresa e módulo.
- Integrações externas podem ter cobrança por uso ou repasse de custos do fornecedor.
- Credenciais de integrações são segredo operacional e não devem ser armazenadas em código ou documentação pública.
- Requisitos de autenticação, limites de taxa, catálogo de conectores e política de retry precisam ser definidos por integração.

## Decisões que não devem ser inventadas

RPO/RTO, SLA, frequência e retenção de backup, coeficientes de capacidade, limites de autoscaling, obrigação de MFA por perfil, política SSO, catálogo de hardware, requisitos offline do Edge e obrigações locais legais/fiscais permanecem PENDENTES nos PDFs.

