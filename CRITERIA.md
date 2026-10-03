# Criteria: SimpleSkins (Forge 1.20.1 /skin mod, server + client jar)

- [x] `gradle build` green on GitHub Actions (Java 17); nothing compiled locally
- [x] CI uploads `simpleskins-1.0.0.jar`; unit tests pass (Mojang JSON parsing, model detection, name validation)
- [x] `/skin <premium-name>` copies that account's skin (Mojang API fetch off-thread), persists per UUID, broadcasts to all clients
- [x] Joining clients receive every stored skin; the joiner's skin is broadcast to everyone
- [x] Client applies skins by swapping the tab-entry textures property and re-running vanilla `registerTextures()` — no AT, no mixins, no reflection
- [x] `/skin clear` restores default look; op `/skin set <player> <name>` works for offline players too
- [ ] Live-verified on the Seedloaf server (needs real Mojang fetch + rendering)
