## graphify

This project has a knowledge graph at graphify-out/ with god nodes, community structure, and cross-file relationships.

Graphify MCP is the primary interface for querying the knowledge graph. For codebase analysis, architecture questions, dependency tracing, symbol relationships, and cross-file reasoning, use the Graphify MCP tools before browsing the source code manually.

When the user types `/graphify`, use the Graphify MCP tools before doing anything else.

Rules:
- For codebase questions, query the Graphify MCP first when `graphify-out/graph.json` exists.
- Use the appropriate Graphify MCP tools for graph queries, relationships, paths, concepts, nodes, communities, and architecture instead of invoking `graphify` through the shell.
- Do not treat the absence of `graphify` in the shell PATH as Graphify being unavailable. If the Graphify MCP tools are available, use them directly.
- Dirty `graphify-out/` files are expected after hooks or incremental updates and are not a reason to skip Graphify.
- Only skip Graphify if the task concerns stale or incorrect graph output, the MCP is actually unavailable, or the user explicitly says not to use it.
- If `graphify-out/wiki/index.md` exists, use it for broad navigation when necessary.
- Read `graphify-out/GRAPH_REPORT.md` only for broad architecture review or when the Graphify MCP does not provide enough context.
- Prefer scoped Graphify MCP queries over raw grep, broad source browsing, or reading the entire graph report.
- After modifying code, keep the knowledge graph current. Prefer a Graphify MCP update/rebuild tool if one is available. If the MCP does not expose an update operation, run `C:\Users\User\.local\bin\graphify.exe update .` explicitly instead of relying on `graphify` being available in PATH.
- If Graphify MCP is unavailable, fall back to `graphify-out/` artifacts and source inspection, but explicitly distinguish this fallback from using Graphify MCP.