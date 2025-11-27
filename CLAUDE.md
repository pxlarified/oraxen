# Oraxen Block System Implementation

## Overview
Implemented a comprehensive block system for Oraxen based on Craft-Engine's architecture. This system provides:

- **Server-side custom block registration** with unique IDs
- **Block state management** with configurable properties (boolean, integer, direction, enum)
- **Visual block state mapping** to vanilla Minecraft blocks for client display
- **Block settings** (hardness, resistance, luminance, burnable, replaceable, etc.)
- **O(1) access** to block states via BlockRegistryMirror
- **ID allocation system** with both fixed and auto-allocation modes
- **Configuration-driven** block definition from YAML

## Core Classes

### Block System Core (`io.th0rgal.oraxen.block`)

1. **CustomBlock** (interface)
   - Defines the contract for custom blocks
   - Methods for state access, property management, and settings

2. **AbstractCustomBlock** (abstract class)
   - Base implementation of CustomBlock
   - Handles state generation and property management
   - Automatically generates all state combinations from properties

3. **SimpleCustomBlock** (class)
   - Concrete implementation used by BlockParser
   - Created from YAML configuration

4. **ImmutableBlockState** (class)
   - Represents a specific block state with all properties resolved
   - Stores internal ID (server-side) and visual ID (client-side)
   - Provides `with()` and `withUnchecked()` methods for state transitions

5. **BlockSettings** (class)
   - Holds block properties: hardness, resistance, friction, luminance, etc.
   - Can be loaded from configuration

6. **Property** (abstract class with subclasses)
   - Represents block properties like powered, lit, facing, etc.
   - Subclasses:
     - `BooleanProperty` - true/false values
     - `IntProperty` - integer ranges
     - `DirectionProperty` - NORTH, SOUTH, EAST, WEST, UP, DOWN
     - `EnumProperty<E>` - generic enum values
   - `StringEnumProperty` (internal) - for string-based enums from config

### ID Management

7. **IdAllocator** (class)
   - Manages internal block state ID allocation
   - Supports fixed ID assignment and automatic allocation
   - Caches allocated IDs
   - Processes pending allocations in batch

8. **VisualBlockStateAllocator** (class)
   - Manages mapping of custom blocks to visual vanilla blocks
   - Supports fixed state assignment and auto-allocation
   - Groups states by type (solid, open, liquid)

### Registry & Access

9. **BlockRegistry** (class)
   - Central registry for all custom blocks
   - Prevents duplicate registration
   - Provides freeze/unfreeze for thread-safety
   - Tracks all registered blocks and states

10. **BlockRegistryMirror** (class)
    - Global static access to block states by ID
    - O(1) lookup: `BlockRegistryMirror.getById(id)`
    - Initialized after BlockManager setup

11. **BlockManager** (class)
    - Coordinates block registration and initialization
    - Manages state ID allocation
    - Initializes BlockRegistryMirror
    - Provides centralized access to blocks and states

### Configuration & Loading

12. **BlockParser** (class)
    - Parses YAML configuration into CustomBlock instances
    - Supports property types: boolean, integer, direction, enum
    - Parses BlockSettings from config
    - `StringEnumProperty` for string-based enum values

13. **BlocksManager** (class)
    - High-level manager for block system
    - Loads blocks from `blocks.yml`
    - Handles reload functionality
    - Accessible from `OraxenPlugin.getBlocksManager()`

## Integration

### Plugin Integration
- Added `BlocksManager` field to `OraxenPlugin`
- Initialized in `onEnable()` after NMS setup
- Loads blocks from `blocks.yml` in plugin data folder
- Getter method: `OraxenPlugin.get().getBlocksManager()`

### Configuration File
- Default `blocks.yml` provided in resources
- Supports YAML-based block definitions with:
  - Custom properties
  - Block settings (hardness, luminance, etc.)
  - Multiple appearance states

## Build Commands

```bash
# Full build
./gradlew build

# Compile only
./gradlew compileJava

# Quick compilation (incremental)
./gradlew compileJava --continuous

# Check for warnings/errors
./gradlew build --warning-mode fail
```

## Architecture Highlights

### State Generation
- Automatically generates all possible state combinations from properties
- Example: Boolean property (2 values) + Direction property (6 values) = 12 states

### ID Mapping
```
Internal ID (server) ← → Visual ID (client)
                ↓
    blockStateMappings[]
                ↓
    Vanilla block state (displayed to players)
```

### Registration Flow
1. Parse YAML configuration
2. Create CustomBlock instances with properties
3. Generate all state combinations
4. Allocate internal IDs
5. Allocate visual IDs (map to vanilla blocks)
6. Store mappings in blockStateMappings array
7. Initialize BlockRegistryMirror for O(1) access

## API Usage

### Access Block Manager
```java
BlocksManager blocksManager = OraxenPlugin.get().getBlocksManager();
CustomBlock block = blocksManager.getBlock("custom_stone");
ImmutableBlockState state = blocksManager.getBlockState(stateId);
```

### Access Specific State
```java
ImmutableBlockState state = BlockRegistryMirror.getById(stateId);
BlockSettings settings = state.getSettings();
```

### Get All Blocks
```java
Collection<CustomBlock> allBlocks = blocksManager.getBlockManager().getAllBlocks();
```

## Example Configuration (blocks.yml)

```yaml
blocks:
  custom_stone:
    settings:
      hardness: 1.5
      resistance: 6.0
      luminance: 0
      burnable: false
    properties:
      powered:
        type: boolean
      lit:
        type: boolean

  custom_redstone:
    settings:
      hardness: 3.0
      luminance: 9
      random_ticks: true
    properties:
      lit:
        type: boolean
```

## Performance Characteristics

- **O(1)** block state lookup via BlockRegistryMirror
- **O(1)** block lookup by ID via BlockRegistry
- **O(n)** state generation where n = ∏(property.getPossibleValues().length)
- Minimal memory overhead: single array of states

## Future Enhancements

- [ ] NMS integration for block placement/breaking
- [ ] Block event system (place, break, interact)
- [ ] Loot tables
- [ ] Drop system
- [ ] Collision shapes
- [ ] Light-emitting blocks integration
- [ ] Block entities support
- [ ] Ore generation
