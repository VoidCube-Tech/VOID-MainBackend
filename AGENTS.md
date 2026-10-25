Backend Libraries
As dependências do backend não devem ser usadas só porque estão instaladas. Use Spring Web para APIs HTTP, Validation para validar entrada, Spring Data JPA para persistência comum de domínio, PostgreSQL como banco principal e Flyway para qualquer alteração de schema. Para consultas analíticas, dashboards ou SQL complexo, prefira jOOQ em vez de forçar JPA.

Spring Security deve centralizar autenticação e autorização, e OAuth2 Resource Server deve ser usado para JWT/Bearer Token em vez de validação manual. Actuator, Prometheus e OpenTelemetry servem para observabilidade: Actuator para health/metrics, Prometheus para coleta de métricas e OpenTelemetry para tracing quando o fluxo passar por múltiplos componentes.

Spring Modulith deve preservar as fronteiras do monólito modular. Evite acesso direto a internals de outros módulos; prefira contratos explícitos e eventos. Testcontainers deve ser usado em testes de integração com PostgreSQL, Redis ou RabbitMQ, sem substituir testes unitários simples.

Redis só deve entrar para cache, rate limit, locks, idempotência ou dados temporários. Use Spring Data Redis (Access+Driver) no backend MVC; não use Reactive Redis sem uma decisão explícita de adoção de WebFlux. Spring Session Redis só faz sentido para sessão HTTP server-side, não para uma API stateless com JWT.

WebSocket deve ser usado apenas quando houver necessidade real de atualização em tempo real. Quartz é indicado para jobs persistentes ou agendamentos dinâmicos; para tarefas simples e fixas, prefira @Scheduled. RabbitMQ só deve ser introduzido quando houver necessidade concreta de fila durável, retry independente ou processamento por workers; dentro do mesmo monólito, prefira primeiro chamadas diretas ou eventos do Spring Modulith.

MapStruct pode ser usado para mapeamentos repetitivos entre DTOs, domínio e respostas, evitando reflection e código manual repetido. Springdoc/OpenAPI deve documentar e facilitar o teste das APIs HTTP.

Sempre que perder contexto sobre arquitetura, responsabilidade de módulos, dependências entre módulos, fluxo de dados ou escolha de biblioteca, consulte as skills Caveman e Graphfy antes de implementar. Não invente uma nova arquitetura com contexto incompleto.

Princípio geral: primeiro identifique o problema, depois a regra de negócio e a arquitetura, e só então escolha a biblioteca. Evite infraestrutura antecipada, acoplamento desnecessário e uso de dependências sem caso de uso concreto.