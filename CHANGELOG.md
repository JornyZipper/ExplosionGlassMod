# ExplosionGlass Changelog

## [1.9.2] - 2025-12-06

### Added
- Custom configuration GUI to replace Forge's auto-generated GuiConfig
- Blacklist/Whitelist editor with add/remove functionality
- Line of Sight (LoS) system with proper visibility checking
- Multi-language support (English, Russian, German, Ukrainian)
- Glass drops with configurable drop chance
- LoS ignore distance parameter for customizable visibility checks

### Fixed
- Fixed GUI config crashes caused by NumberFormatException
- Fixed LoS algorithm to properly check visibility through transparent materials
- Fixed whitelist/blacklist editor functionality
- Fixed button text positioning and alignment in config GUI

### Changed
- Improved LoS checking to use path-tracing algorithm instead of ray-casting
- Changed default Glass Drops to OFF
- Optimized explosion detection to check all blocks in radius efficiently
- Updated config default values for better gameplay balance

### Technical Details
- Line of Sight now checks 8 corners of target block
- Transparent materials (glass, water, leaves) don't block line of sight
- Solid materials (stone, dirt, wood) properly block line of sight
- LoS ignore distance controls the zone where visibility is not checked

## [1.9] - Previous Release
- Initial mod release with basic glass breaking functionality

---

## Configuration Options

### General Settings
- **Enable** - Toggle mod on/off (Default: ON)
- **Break Radius** - Radius where glass always breaks without LoS check (Default: 20 blocks)
- **LoS Radius** - Radius where glass breaks if visible (Default: 10 blocks)
- **Use LoS Check** - Enable/disable line of sight checking (Default: ON)
- **Drop Items** - Enable glass drops (Default: OFF)
- **Drop Chance** - Probability of glass dropping (0.0-1.0, Default: 1.0)
- **LoS Ignore Distance** - Blocks where LoS is ignored (Default: 10 blocks)
- **Blacklist** - Blocks that should NOT break
- **Whitelist** - Blocks that ALWAYS break regardless of radius/LoS
