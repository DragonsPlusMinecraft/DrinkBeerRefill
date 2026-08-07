## Drink Beer Refill 1.4.0-beta.1

This release ports the holiday content and gameplay values from the authorized upstream Drink Beer 4.0.0-beta release to NeoForge 1.21.1 while retaining the revival's server-authoritative interaction, automation, migration, and security fixes.

### Added

- Added the four colored gifts and their 27-entry upstream reward pool.
- Added center and side colored lights, both Stars of Bethlehem, and all three two-block horse models.
- Added the upstream gift-opening, neigh, and bell sounds, models, textures, crafting recipes, loot tables, and translations.
- Added configurable beer saturation with an improved default of `0.1`; `0.0` restores the exact upstream no-saturation behavior.
- Added regression coverage for upstream beer values, gift rewards, holiday resources, and server-authoritative special drink behavior.

### Changed

- Restored the nine base beers' hunger, status-effect, moon-phase, and intoxication values from upstream 4.0.0-beta.
- Centralized beer definitions so ordinary and mixed beer use the same gameplay data and special actions.
- Ported holiday interactions to native NeoForge 1.21.1 APIs without Fabric client mutations or renderer mixins.
- Changed the project license from AGPL-3.0 to **All Rights Reserved** with permission from original author Lekavar.
- Declared the authors as `Lekavar, MarbleGateKeeper`, with the original author listed first.

### Preserved fixes

- Kept safe hopper capabilities, block drops, bartending interaction fixes, mixed-beer migration, world-change limits, and hardened trade-box networking from `1.3.0-beta.1`.

## Drink Beer Refill 1.3.0-beta.1

This is the first maintenance release after the project revival. It targets Minecraft 1.21.1 and requires NeoForge 21.1.244 or newer.

### Fixed

- Restored block drops on Minecraft 1.21.1 by moving all 41 loot tables to the correct `loot_table/blocks` data-pack directory.
- Fixed the first spice crashing the bartending table when a mixed-beer component contained fewer than three entries.
- Fixed bartending-table beer placement, spice insertion, drawer toggling, beer retrieval, and overlapping-model interaction problems.
- Fixed hopper insertion into the bartending table crashing because the advertised item handler exposed nonexistent inventory slots.
- Reworked keg state synchronization so brewing completes and the finished beer can be removed.
- Kegs now consume exactly one of each ingredient and the configured number of cups only when brewing completes; stacked ingredients are no longer deleted wholesale.
- Fixed automation side rules and input validation for kegs and bartending tables.
- Kegs and bartending tables now return their real stored contents when broken.
- Fixed mixed beer being reset when placed, picked up, or migrated from legacy `BlockEntityTag/MixedBeer` data.
- Fixed mixed-beer spice/flavor combination matching and integer persistence.
- Beer now restores saturation as well as hunger, and both values are shown in tooltips.
- Fixed trade-box cooldowns and offers only progressing while a menu was open.
- Hardened trade-box refresh packets against remote positions, unrelated menus, invalid block entities, and off-thread handling.
- Fixed the trade-box refresh button requiring the player to keep looking at the block while its screen was open.
- Prevented trade offer display slots from being shift-clicked or dropped by breaking the trade box.
- Hardened drunkenness handling against invalid amplifiers and moved drink-side inventory/effect mutations to the logical server.
- Fixed Drunk Frost Walker removing itself after its first effect tick.
- Fixed recipe-board-to-beer lookup comparing translated text with an internal id.

### Changed

- Empty-hand interaction with the bartending table retrieves the beer; use the drawer face or sneak-use to toggle the drawer.
- Beer is placed directly on the bartending table. Spices may be used anywhere on the table while the drawer is open.
- Bartending tables expose beer input on top, spice input on horizontal sides, and finished-beer output on the bottom.
- Kegs expose ingredients on top, cups on horizontal sides, and output/returned buckets on the bottom.
- World-changing Stormy and Drying flavor effects no longer load chunks and are capped at 4096 changed blocks per drink by default. Both behaviors are configurable in `drinkbeer-server.toml`.
- Updated NeoForge from 21.1.72 to 21.1.244, ModDevGradle to 2.0.143, Gradle to 9.2.1, Parchment mappings, and the publishing plugin.
- Declared exact Minecraft 1.21.1 compatibility and a minimum NeoForge version of 21.1.244.

### Development

- Added NeoForge-aware JUnit 5 coverage for spice components, brewing recipes, legacy mixed-beer migration, drunkenness bounds, JSON parsing, and resource layout.
- Added a GitHub Actions CI workflow that validates the Gradle wrapper, builds, tests, and uploads the JAR.
- Modernized the manual publishing workflow and enabled both client and dedicated-server metadata for CurseForge.

## Drink Beer Refill 1.2.0

### Changed

- Recipe Board Package now only gives one random Recipe Board.

### Fixed

- Fixed a server crash while the keg was working.
- Fixed keg inventory glitches when a brewing recipe matched.
