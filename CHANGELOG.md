## Drink Beer Refill 1.4.0-beta.3 (Minecraft 1.21.11)

### Changed

- Updated the native Fabric and NeoForge builds to Minecraft 1.21.11, Fabric API 0.141.6+1.21.11, NeoForge 21.11.45, NeoForm `1.21.11-20251209.172050`, Parchment `2025.12.20`, and JEI 27.22.0.65.
- Migrated identifiers, interaction results, consumable components, value-based block-entity serialization, GUI blits, and block-entity render-state submission without changing registry or payload IDs.
- Updated item/model and recipe resources to the 1.21.11 formats.
- Use Fabric Loom 1.17.19 with Gradle 9.5.1: JEI 27.22.0.65 identifies itself as built by Loom 1.17.12, so the originally planned Loom 1.14.10 configuration rejects the dependency before compilation.

### Verification

- Added `verifyUnit`, `verifyServer`, `verifyCompatibility`, `verifyClient`, and `verifyRelease` unattended gates with dependency locks, verification metadata, reproducible archives, artifact inspection, log auditing, and SHA-256 output.
- Expanded the shared server suite to 10 DrinkBeer scenarios on each loader, plus Minecraft's built-in test, covering brewing automation, bartending and mixed-beer persistence, payload validation, configuration limits, and optional JEI startup.
- Added real Fabric and NeoForge graphical-client probes. Each creates or opens an isolated same-version world, renders for at least 200 frames and 15 seconds, verifies OpenGL, opens the keg GUI and all 9 JEI brewing recipes, records screenshots/JSON, saves, and exits normally.
- Added a PID-scoped Windows watchdog that verifies the actual Minecraft window and captures external world, F3, GUI, JEI, and save states without touching other Minecraft processes.
- Verified a world created by the 1.21.11 Fabric probe can be loaded and saved by the 1.21.11 NeoForge probe. No cross-Minecraft-version world compatibility is claimed or tested.

## Drink Beer Refill 1.4.0-beta.2

This release migrates the project to a native Fabric and NeoForge multi-loader layout for Minecraft 1.21.1. It does not depend on Architectury Loom, Architectury Gradle Plugin, or Architectury API.

### Added

- Added a Fabric 0.19.3 build with Fabric API 0.116.12+1.21.1 and Fabric Loom 1.14.10.
- Added Fabric implementations for registration, extended menus, C2S networking, Transfer API inventory automation, client registration, and per-world JSON configuration.
- Added shared registry-ID snapshots, loader-specific configuration tests, and five shared Holiday GameTests with thin Fabric and NeoForge discovery adapters.
- Added CI and publishing tasks that build, test, and publish separate Fabric and NeoForge artifacts.

### Changed

- Split the project into `common`, `fabric`, and `neoforge` modules. Each loader compiles the shared sources in its own toolchain; no common JAR is embedded or published.
- Moved shared gameplay to vanilla APIs behind small platform and client hooks. NeoForge retains deferred registration and capabilities; Fabric uses direct registration and Transfer API storage.
- Replaced loader-specific inventory and menu primitives in shared code with vanilla containers and slots while preserving all automation directions and restrictions.
- Kept Minecraft 1.21.1, Java 21, NeoForge 21.1.244, ModDevGradle 2.0.143, NeoForm `1.21.1-20240808.144430`, Parchment `2024.11.17`, and JEI 19.21.0.246.
- Fabric now stores server settings in `<world>/serverconfig/drinkbeer-server.json`; NeoForge continues to use `drinkbeer-server.toml` with matching fields, defaults, and ranges.

### Fixed

- Fixed transparent planes on the holiday stars and colored lights rendering as black on Fabric by registering their cutout render layer.

### Compatibility

- Preserved registry IDs, resource paths, NBT and codec formats, including the existing `drinkbeer:refreash_tradebox` payload ID.
- Verified both dedicated-server GameTest suites and loading NeoForge-created test worlds on Fabric and Fabric-created test worlds on NeoForge.
