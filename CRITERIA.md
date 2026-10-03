# Criteria: SimpleSkins (Forge 1.20.1 /skin mod, server + client jar)

- [ ] `gradle build` green on GitHub Actions (Java 17); nothing compiled locally
- [ ] CI uploads `simpleskins-1.0.0.jar`; unit tests pass (Mojang JSON parsing, model detection, name validation)
- [ ] `/skin <premium-name>` copies that account's skin (Mojang API fetch off-thread), persists per UUID, broadcasts to all clients
- [ ] Joining clients receive every stored skin; the joiner's skin is broadcast to everyone
- [ ] Client applies skins by swapping the tab-entry textures property and re-running vanilla `registerTextures()` — no AT, no mixins, no reflection
- [ ] `/skin clear` restores default look; op `/skin set <player> <name>` works for offline players too
- [ ] Live-verified on the Seedloaf server (needs real Mojang fetch + rendering)
