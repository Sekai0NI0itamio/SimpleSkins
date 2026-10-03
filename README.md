# SimpleSkins

Wear any premium skin on an offline-mode **Forge 1.20.1** server.
The same jar goes on **server and clients**.

## Commands

- `/skin <premium-name>` — e.g. `/skin rekrap2`
- `/skin clear` — back to default
- `/skin set <player> <name>` — ops only, works for offline players too

The server fetches the skin from Mojang once, stores it in
`<world>/serverconfig/simpleskins.json`, and pushes it to every client,
which applies it to the tab-list entry and re-runs vanilla texture loading.
No access transformers, mixins, or reflection.

## Building

CI-only: every push runs `gradle build` (Java 17, unit tests) on GitHub
Actions and uploads the jar. Tags `v*` publish a release.
