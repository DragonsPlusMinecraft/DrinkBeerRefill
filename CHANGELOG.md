## Drink Beer Refill 1.4.1

This version is preparation for mod cross-mod compat.

### Added
- Added tank-only source and flowing fluids for all nine base beers, with one mug equal to 250 mB.
- Added standard Fabric Transfer API and NeoForge fluid-item capabilities for draining and filling beer mugs.
- Added per-beer fluid tags, the aggregate `#drinkbeer:beers` tag, localized fluid names, and cross-loader regression coverage.
- Added an optional NeoForge Create milk-sprite atlas pack that contains metadata only and activates only when both external Create textures exist.

### Fixed
- Fixed Create 6.0.x JEI Spout pages showing 1000 mB for beer mugs that actually accept 250 mB, without adding duplicate filling recipes.

### Compatibility

- Beer fluids are non-placeable and have no buckets. Create is optional; NeoForge creamy beers reference its milk textures only when both files are available, while Fabric and all fallback paths use vanilla water textures.
- The Create/JEI display patch is optional, client-only, limited to DrinkBeer fluids, and fails closed if Create changes the targeted method.
- Mixed beer remains item-only so its base-beer and spice components are never discarded by fluid conversion.
- Beer barrels keep their existing mug-based production and are not converted into fluid tanks.
