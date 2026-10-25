# Automações, IA e integrações

Este arquivo reúne o modelo de eventos, ações, regras e IA descrito nos PDFs. Trate os exemplos como exemplos; a lista final de eventos e ações deve pertencer aos módulos que os publicam.

Fontes principais: **Arquitetura de Produtos**, pp. 8–9, 17–19, 25–27; **Registro Mestre**, pp. 7–9.

## Modos de operação

O cliente pode operar uma tarefa de três formas:

1. **Manual:** uma pessoa inicia e executa a ação.
2. **Regra determinística:** um evento e condições configuradas acionam uma sequência previsível.
3. **IA com autonomia configurada:** a IA executa somente dentro das permissões, políticas, limites e aprovações definidas.

O modo de automação deve ser explícito por fluxo ou ação. IA não recebe permissão implícita por estar habilitada.

## Eventos e ações

Módulos publicam eventos de domínio e declaram as ações que oferecem. Exemplos ilustrativos:

| Evento | Exemplos de ações possíveis |
| --- | --- |
| Pedido pago | Criar tarefa, enviar confirmação, atualizar relatório |
| Estoque baixo | Solicitar aprovação, criar pedido de compra |
| Tarefa concluída | Notificar responsável, iniciar próxima etapa |
| Funcionário admitido | Criar checklist, gerar documentos, atribuir acessos |
| Documento próximo do vencimento | Notificar, abrir tarefa de renovação |
| Acesso negado | Registrar evento, avisar responsável, iniciar análise |

Nomes como pedido.pago ou estoque.baixo são exemplos, não contratos já implementados. Cada módulo deve documentar esquema, versão, contexto de Empresa e semântica de entrega do evento.

## Regras e construtor visual

- Regras podem ser criadas por formulário simples, linguagem natural validada ou editor visual de nós.
- Fluxos avançados podem combinar evento, condições, chamadas, aprovações e ações.
- Aprovação humana pode existir em qualquer etapa do fluxo.
- O cliente deve simular ou validar a regra antes de ativá-la, quando a execução puder afetar dados ou gerar cobrança.
- Receitas e templates são configurações reutilizáveis; não devem duplicar o módulo que implementa a regra.
- O módulo Automations coordena regras, mas o domínio dono continua responsável pela regra de negócio e pela implementação da ação.

## Permissões e trilha de execução

- A automação só pode executar ações disponíveis ao cliente e ao escopo do evento.
- Uma ação de IA herda o menor conjunto de permissões necessário para a tarefa; o acesso do usuário solicitante também deve ser respeitado.
- Registre quem iniciou e executou cada etapa: HUMAN, AI, AUTOMATION, SYSTEM ou INTEGRATION.
- Para IA, registre política aplicada, aprovador quando houver, limites, valor/custo estimado ou realizado e dados de entrada usados conforme política de retenção.
- A trilha deve permitir explicar a decisão e identificar erros sem conceder acesso amplo a conteúdo sensível.

## IA e consumo

- O acesso de IA a dados deve ser configurável pela Organização e sujeito às permissões do usuário que fez a solicitação.
- IA pode responder consultas e acionar ferramentas permitidas; ações com impacto podem exigir aprovação humana ou limites de valor.
- Cobrança de IA é por uso. O Dashboard deve exibir consumo, limite e projeção.
- Modelos, fornecedores, preços unitários, política de retenção de prompts e limites ainda dependem de decisões técnicas/comerciais específicas.

## APIs e webhooks

- Eventos e ações do produto são a fonte comum para automações internas, API e webhooks.
- API e webhook não devem criar uma segunda implementação de regra de domínio.
- Respeite escopo e autorização da Organização, Empresa, módulo e ação em toda chamada.
- Autenticação, idempotência, versionamento, retry, assinaturas, limites e catálogo de eventos precisam ser decididos por integração.

## Playground

O Playground deve permitir explorar módulos, dependências, integrações, receitas e fluxos em contexto de demonstração. A composição de um produto ou template deve resolver os módulos necessários e tornar explícitas suas dependências e custos. Templates são configurações para um setor ou processo, não forks de código.

