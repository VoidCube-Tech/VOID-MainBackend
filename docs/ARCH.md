# Arquitetura do backend VoidCube

Este documento orienta agentes e desenvolvedores sobre a estrutura do repositório, onde colocar código novo e quais fronteiras preservar. Ele separa o que já existe do desenho definido nos documentos de produto; uma decisão documentada como alvo não significa que já esteja implementada.

## Como interpretar as informações

- **Atual** descreve arquivos e comportamentos encontrados no código e na configuração do repositório.
- **Diretriz** descreve regras técnicas registradas no `AGENTS.md`.
- **Alvo** descreve decisões dos documentos de arquitetura e produto que ainda precisam ser implementadas.

Para comportamento implementado, confira o código. Para regras de negócio e decisões de produto, consulte os documentos em `docs/`. Quando uma decisão ainda estiver pendente, não a transforme em regra por suposição: registre a dúvida e proponha um ADR quando uma decisão técnica for necessária.

## Estado atual

**Atual:** o repositório é um esqueleto de aplicação Spring Boot em Kotlin, preparado para coexistir com código Java. A aplicação é um único processo; ainda não há módulos de negócio, controllers, entidades, serviços de domínio ou migrações Flyway implementadas.

| Item | Estado atual |
| --- | --- |
| Build | Gradle com Kotlin DSL (`build.gradle.kts`) |
| Linguagens | Kotlin 2.3.21; Java e Kotlin compilados com toolchain Java 25 |
| Framework | Spring Boot 4.1.2 |
| Nome Gradle e aplicação | `Backend` |
| Pacote da aplicação | `com.voidcube.Backend` |
| Configuração | `src/main/resources/application.properties`, hoje com apenas `spring.application.name=Backend` |
| Persistência/schema | Dependências de JPA, PostgreSQL e Flyway declaradas; sem configuração ou migrações próprias no momento |
| Testes | Um teste de contexto Spring e uma configuração Testcontainers compartilhada |

O pacote existente usa `Backend` com inicial maiúscula. Preserve declarações e caminhos existentes ao adicionar arquivos. Uma eventual normalização deve alterar os pacotes e caminhos de forma coordenada, como uma mudança explícita.

As dependências presentes no Gradle indicam ferramentas disponíveis, não funcionalidades já adotadas. Só conecte uma biblioteca a um fluxo quando houver caso de uso concreto, seguindo o `AGENTS.md`.

## Mapa do repositório

```text
.
├── AGENTS.md                         # Instruções para agentes e regras de dependências
├── build.gradle.kts                  # Plugins, versões, dependências e configuração Kotlin/JVM
├── settings.gradle.kts               # Nome do projeto Gradle
├── gradlew / gradlew.bat             # Gradle Wrapper para Unix e Windows
├── gradle/wrapper/                   # Configuração e JAR do Wrapper
├── docs/                             # Decisões, arquitetura de produto, marca e este documento
├── src/
│   ├── main/
│   │   ├── kotlin/com/voidcube/Backend/
│   │   │   └── BackendApplication.kt # Inicialização Spring Boot atual
│   │   ├── java/                     # Raiz Java padrão; ainda não criada
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/         # Migrações Flyway; diretório atualmente vazio
│   │       ├── static/               # Recursos estáticos; diretório atualmente vazio
│   │       └── templates/            # Templates server-side; diretório atualmente vazio
│   └── test/
│       ├── kotlin/com/voidcube/Backend/
│       │   ├── BackendApplicationTests.kt
│       │   ├── TestBackendApplication.kt
│       │   └── TestcontainersConfiguration.kt
│       └── java/                     # Raiz Java de testes padrão; ainda não criada
└── graphify-out/                     # Saída local gerada pelo Graphify, ignorada pelo Git
```

`src/main/resources/static/` e `templates/` só devem receber arquivos se o backend passar a servir esses recursos. Para a API MVC, controllers e contratos HTTP ficam no código do módulo responsável.

### Kotlin e Java

O projeto aceita Kotlin e Java no mesmo backend Gradle. Kotlin fica em `src/main/kotlin/` e Java em `src/main/java/`; testes seguem a mesma convenção em `src/test/kotlin/` e `src/test/java/`. As raízes Java ainda não existem no repositório; crie-as apenas ao adicionar código Java. As duas linguagens devem usar o mesmo pacote e módulo — não separe módulos por linguagem. Até uma decisão explícita de normalização, use o pacote exato `com.voidcube.Backend` também nos arquivos Java para manter os tipos no mesmo namespace. Prefira contratos públicos interoperáveis quando tipos Kotlin forem chamados por código Java.

## Organização do código de negócio

**Alvo:** a arquitetura de produto define um monólito modular, com módulos de negócio no mesmo backend e fronteiras explícitas. Não dividir por produto vertical com cópias de funcionalidades horizontais; configurações e composições de módulos devem permitir reutilização.

Quando um caso de uso concreto exigir um módulo, crie-o como pacote de primeiro nível dentro do pacote raiz atual. Exemplo ilustrativo — não crie todos esses diretórios antecipadamente:

```text
src/main/kotlin/com/voidcube/Backend/
├── BackendApplication.kt
├── config/                           # Configuração transversal do Spring
├── core/                             # Capacidades centrais compartilhadas, quando implementadas
│   ├── api/
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── web/
├── crm/
│   ├── api/
│   ├── application/
│   ├── domain/
│   ├── infrastructure/
│   └── web/
└── sales/                            # Mesmo padrão, se e quando esse módulo existir
```

Os mesmos pacotes podem conter classes Java em `src/main/java/com/voidcube/Backend/`; escolha a linguagem conforme o código e a colaboração da equipe, mantendo a responsabilidade do módulo. Não duplique a implementação de uma regra nas duas linguagens.

Use as pastas dentro de cada módulo conforme a necessidade:

| Pasta | Responsabilidade |
| --- | --- |
| `api/` | Contratos públicos do módulo para outros módulos: interfaces, comandos/consultas compartilhados e eventos publicados. Não é sinônimo de controller HTTP. |
| `application/` | Casos de uso e coordenação do fluxo; aplica regras chamando o domínio e portas/adaptadores necessários. |
| `domain/` | Regras de negócio, entidades e objetos de valor pertencentes ao módulo. Evite dependência de HTTP e detalhes de infraestrutura. |
| `infrastructure/` | Adaptadores técnicos do módulo: persistência, integrações externas e implementações de portas. |
| `web/` | Controllers Spring MVC, DTOs de entrada/saída e mapeamento do protocolo HTTP para casos de uso. |
| `config/` | Configuração transversal da aplicação. Configuração exclusiva de um módulo deve ficar junto ao módulo. |

Não crie camadas vazias por simetria. Para módulos pequenos, adicione apenas as pastas necessárias e mantenha cada responsabilidade no módulo dono da regra.

### Fronteiras entre módulos

- Spring Modulith deve representar e verificar as fronteiras do monólito modular.
- Um módulo não deve importar diretamente pacotes internos (`domain`, `infrastructure`, `web`) de outro módulo.
- Para colaboração síncrona, use um contrato público em `api/`. Para comunicação desacoplada, publique/consuma eventos de módulo quando fizer sentido.
- Declare dependências entre módulos de forma explícita e mantenha-as direcionadas; evite ciclos.
- Não use `common`, `shared` ou `utils` como depósito de regras de negócio. Compartilhe somente conceitos realmente transversais e com dono claro.
- A lista de produtos em `voidcube_arquitetura_produtos_operacao.pdf` é catálogo/visão de produto, não uma ordem para criar um pacote para cada item.

## Domínio de plataforma definido nos documentos

**Alvo, ainda não implementado neste código:** a raiz de tenancy é `Organization`. Ela pode conter grupos, empresas e unidades organizacionais. A empresa (`Company`) é proprietária das licenças dos módulos; a organização pode comprar e distribuir licenças. Uma identidade de usuário é global e se relaciona às empresas por associações (`Membership`), com papéis e escopos de acesso.

O módulo Core/plataforma deve concentrar os conceitos compartilhados de organização/tenant, empresas, memberships, papéis/permissões, notificações, auditoria, configurações e catálogo/ativação de módulos conforme os casos de uso forem definidos. Outros módulos não devem criar versões próprias desses conceitos nem acessar seus detalhes internos. A autorização deve validar o contexto organizacional e o escopo do registro/ação; não confie em um identificador de organização ou empresa enviado pelo cliente sem autorização.

Os documentos recomendam evolução em etapas e migração controlada caso existam dados/modelos legados. Este repositório, no estado atual, não contém esses modelos. As regras comerciais, retenção, Edge/offline, billing, pools de recursos e catálogo completo permanecem decisões de produto; implemente-as somente quando o escopo de domínio e as regras estiverem definidos.

## Onde colocar integrações e persistência

- **HTTP:** endpoints em `web/` do módulo dono; valide entrada com Bean Validation e encaminhe para um caso de uso. Não coloque regra de negócio no controller.
- **Persistência comum:** PostgreSQL como banco principal; Spring Data JPA para persistência de domínio comum. Repositórios e adaptadores pertencem ao `infrastructure/` do módulo dono.
- **Schema:** toda alteração de schema deve ter migração Flyway versionada em `src/main/resources/db/migration/`, seguindo o padrão `V<versão>__<descricao>.sql`. Migrações aplicadas são imutáveis.
- **Consultas complexas/analíticas:** use jOOQ quando SQL complexo, dashboards ou análise justificarem; não force JPA para esses casos. Ainda não há configuração de geração/código jOOQ específica neste projeto.
- **Configuração Spring:** valores não secretos e defaults em `application.properties`; configurações específicas podem usar perfis, por exemplo `application-local.properties` ou `application-prod.properties`.
- **Segredos locais:** `.env` e `.env.*` são ignorados pelo Git, enquanto `.env.example` pode ser versionado sem valores secretos. Spring Boot não carrega `.env` automaticamente: use variáveis de ambiente ou a ferramenta de execução que as injeta.
- **Autenticação:** centralize autorização/autenticação no Spring Security. Para JWT/Bearer, use OAuth2 Resource Server; não valide tokens manualmente em cada endpoint.
- **Eventos e filas:** dentro do monólito, prefira chamada direta ou evento Spring Modulith. Introduza RabbitMQ quando houver necessidade concreta de fila durável, retry independente ou processamento em workers.
- **Redis:** use somente para cache, rate limit, locks, idempotência ou dados temporários. O backend atual é MVC; Redis reativo exige decisão explícita de adoção de WebFlux. Spring Session Redis só se aplica se houver sessão HTTP server-side.
- **Tempo real e agendamento:** use WebSocket somente para atualização realmente em tempo real; prefira `@Scheduled` para tarefas fixas simples e Quartz para jobs persistentes/dinâmicos.
- **Observabilidade:** Actuator expõe health/metrics; Prometheus coleta métricas; OpenTelemetry adiciona tracing quando o fluxo atravessar componentes e isso for necessário.
- **Integrações externas:** adaptadores ficam no `infrastructure/` do módulo responsável; mantenha credenciais fora do código e da configuração versionada.

## Testes

**Atual:** `BackendApplicationTests` é um teste de contexto (`@SpringBootTest`) e importa `TestcontainersConfiguration`. Essa configuração declara containers para Grafana LGTM, PostgreSQL, RabbitMQ e Redis. `TestBackendApplication` oferece uma inicialização de teste com a mesma configuração.

Organize testes Kotlin em `src/test/kotlin/com/voidcube/Backend/`, espelhando os pacotes do módulo testado. Testes simples de domínio/casos de uso devem continuar unitários; use Testcontainers em testes de integração que realmente dependam de PostgreSQL, Redis ou RabbitMQ. Não use o teste de contexto como substituto para cobertura de regras de negócio.

## Documentos de referência

- [base-conhecimento/README.md](base-conhecimento/README.md): índice da base de conhecimento de produto organizada por assunto; consulte o registro pertinente e confirme ambiguidades no PDF de origem.

- `AGENTS.md`: regras técnicas e critérios de uso das bibliotecas backend.
- `docs/VoidCube_Decisoes_Consolidadas_2026-10-17.pdf`: decisões consolidadas de produto e recomendações de evolução.
- `docs/voidcube_arquitetura_produtos_operacao.pdf`: catálogo e arquitetura dos produtos e da operação.
- `docs/BRAND_GUIDELINES.md`: guia de marca; para nomes técnicos, mantenha nomes funcionais internos até que os nomes de mercado sejam definidos.

Os PDFs registram pontos ainda pendentes — por exemplo, políticas de MFA, RPO/RTO de backup, coeficientes de capacidade e nomes finais de mercado. Não invente esses valores. Quando uma implementação depender deles, deixe explícito o ponto pendente e proponha uma decisão/ADR.
