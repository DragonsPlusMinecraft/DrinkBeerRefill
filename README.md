# DrinkBeer Refill

DrinkBeer Refill is a community revival of **Drink Beer**, bringing placeable drinks, brewing, bartending, spices, flavor combinations, and bartering to modern Fabric and NeoForge.

The current revival release targets:

- Minecraft 1.21.1
- Fabric Loader 0.19.3 with Fabric API 0.116.12+1.21.1, or NeoForge 21.1.244 or newer
- Java 21

The `1.4.0-beta.2` line adds first-class Fabric support while preserving the authorized upstream 4.0 holiday content, repaired 1.21.1 gameplay, mixed-beer migration, safe automation, and save compatibility.

## Using the core blocks

### Keg

Place the recipe's four ingredients and the required empty mugs in the keg. Inputs remain visible and locked while brewing; they are consumed only when the timer completes. The result can then be taken from the output slot.

Automation faces:

- Top: ingredient input
- Horizontal sides: empty-mug input
- Bottom: finished-beer and returned-bucket output

### Bartending table

- Use a beer on the table to place it.
- Open the drawer, then use up to three spices anywhere on the table to mix them into the beer.
- Use an empty hand on the table surface to take the beer back.
- Use an empty hand on the drawer face, or sneak-use anywhere on the table, to open or close the drawer.

Automation faces:

- Top: beer input
- Horizontal sides: spice input
- Bottom: mixed-beer output

### Placeable beer

Placed beer can be recovered either by breaking it or by using an empty hand. Mixed-beer base and spice data are preserved in both cases, including items migrated from the legacy `BlockEntityTag/MixedBeer` format.

## Server configuration

Server configuration is stored per world:

- Fabric: `<world>/serverconfig/drinkbeer-server.json`
- NeoForge: `<world>/serverconfig/drinkbeer-server.toml`

Both files expose the same fields, defaults, and ranges. Fabric creates a missing JSON file with defaults; an invalid file is left untouched and defaults are used for that session.

- `enableWorldChangingFlavorEffects` controls whether Stormy and Drying flavors may alter blocks.
- `maxWorldChangesPerDrink` limits changes from a single drink (default: 4096). Flavor effects never force-load chunks.
- `beerSaturationModifier` controls beer saturation (default: `0.1`; use `0.0` for exact upstream 4.0 behavior).

## Optional integrations

JEI integration displays brewing recipes and required brewing time. The mod itself can run without JEI.

## Development

Build and run the regression suite with Java 21:

```text
./gradlew build
```

The project is split into `common`, `fabric`, and `neoforge` modules. Shared JUnit tests run once in each loader, and both dedicated-server GameTest suites are part of the root `check`/`build`. CI publishes only the two loader-specific JARs.

## Authors and license

- [Lekavar](https://github.com/Lekavar) is the original creator of Drink Beer and the author of the upstream 4.0 content.
- [MarbleGateKeeper](https://github.com/MarbleGateKeeper) is the current maintainer and author of the modern multi-loader revival.

DrinkBeer Refill is distributed under **All Rights Reserved**. Redistribution, modification, relicensing, or publication of the project or derivative versions requires express written permission from the rights holders. Third-party dependencies and tools remain under their respective licenses.

## Historical contributors

Thanks to everyone whose work made this revival possible:

- [Lekavar](https://github.com/Lekavar) and [MarbleGateKeeper](https://github.com/MarbleGateKeeper) created the 1.17.1 and earlier official versions.
- [Naetheline](https://github.com/Naetheline) created the 1.18.1 version and ported bartending and bartering.
- [yanang007](https://github.com/yanang007) created the 1.18.2 version and Jade integration.
- [MarbleGateKeeper](https://github.com/MarbleGateKeeper) created the JEI integration.
