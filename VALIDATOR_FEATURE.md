# Resource Pack Validator Feature

## Overview

The Resource Pack Validator is a comprehensive built-in validation system that helps detect and prevent common resource pack issues before they break the plugin or cause issues for players.

## Features

### 1. **File Integrity Checks** ✓
- Detects missing textures or models referenced in configs
- Warns if a JSON model points to a non-existent texture
- Spots duplicate namespaces or conflicting item IDs
- Validates texture references from models

### 2. **Syntax & Format Validation** ✓
- Verifies JSON files are valid (no syntax errors)
- Ensures YAML configs follow correct indentation and format
- Catches MiniMessage formatting errors in item lore/messages (including color codes, gradients)
- Validates bracket/tag matching in text

### 3. **Texture & Model Consistency** ✓
- Checks that textures are power-of-two dimensions
- Flags oversized textures that exceed 512x512 pixels
- Detects non-power-of-two textures that may cause issues
- Validates model file naming conventions

### 4. **Resourcepack Assembly Verification** ✓
- Confirms that the pack compiles properly before sending to clients
- Detects path naming issues (invalid characters)
- Validates model hierarchy (parent model references)
- Provides detailed reporting of issues found

## Usage

### Automatic Validation During Pack Generation

The validator runs automatically during pack generation when enabled in settings:

```yaml
Pack:
  generation:
    validate_pack: true              # Enable/disable automatic validation
    validator_fail_on_error: false   # Abort generation if errors found
```

### Manual Validation

Use the `/oraxen validate` command to validate the generated resource pack at any time:

```
/oraxen validate
```

This command:
- Reads the packed resource pack from `pack/pack.zip`
- Runs all validation checks
- Displays a detailed report with all issues
- Shows statistics on errors, warnings, and info messages

## Output Examples

### Successful Validation

```
──────────────────────────────────────────────────────
Resource Pack Validation Report
──────────────────────────────────────────────────────
✓ No issues found in resource pack!

Validation completed in 234ms
──────────────────────────────────────────────────────
```

### Validation with Issues

```
──────────────────────────────────────────────────────
Resource Pack Validation Report
──────────────────────────────────────────────────────
✗ 2 error(s) found
⚠ 5 warning(s) found
ℹ 3 info message(s)

Issues:
  ✗ ERROR: Texture resolution exceeds 512x512: 1024x1024 (assets/oraxen/textures/item/sword.png)
  ✗ ERROR: Invalid JSON syntax: Unexpected character at line 5 (assets/oraxen/models/item/sword.json)
  ⚠ WARNING: Missing texture reference: my_texture (assets/oraxen/models/item/armor.json)
  ⚠ WARNING: Texture dimensions are not power of 2: 128x100 (may cause issues)
  ...

Validation completed in 456ms
──────────────────────────────────────────────────────
```

## Validation Checks Explained

### JSON Syntax Validation
Scans all `.json` files in the resource pack and validates their JSON syntax. Reports syntax errors with line/character information.

### YAML Syntax Validation
Validates all `.yml` and `.yaml` files for proper YAML syntax and indentation.

### Texture Resolution Checks
- **Maximum Size**: Textures cannot exceed 512x512 pixels (Minecraft limit)
- **Power of Two**: Recommends dimensions that are powers of 2 (16, 32, 64, 128, 256, 512) to avoid rendering issues

### Texture Reference Validation
Scans model JSON files for texture references and verifies:
- All referenced textures exist
- Texture paths follow the correct format
- Model hierarchy (parent models) are available

### Path Naming Validation
Ensures all file paths contain only valid characters: `[a-z0-9/._-]` (lowercase, numbers, slashes, dots, underscores, hyphens only)

### MiniMessage Formatting Validation
Checks for common MiniMessage issues:
- Mismatched `<` and `>` brackets
- Mismatched `{` and `}` braces

### Model Hierarchy Validation
Verifies that parent model references in model files point to existing models.

### Namespace Validation
Checks namespace names follow Minecraft conventions and contain only valid characters.

## Configuration

Add these settings to your `settings.yml`:

```yaml
Pack:
  generation:
    # Enable comprehensive resource pack validation
    validate_pack: true
    
    # If true, pack generation will abort if validation finds errors
    # If false, generation continues but issues are reported
    validator_fail_on_error: false
```

## Integration Points

The validator integrates at two levels:

1. **Pack Generation** (`ResourcePack.java:149-158`)
   - Runs after all pack files are assembled
   - Before atlas generation and other post-processing
   - Can optionally abort generation on critical errors

2. **Manual Commands** (`/oraxen validate`)
   - Admins can validate the generated pack at any time
   - Useful for troubleshooting specific issues
   - Provides detailed feedback on problems

## Performance Considerations

- Validation runs synchronously during pack generation
- Typically completes in 200-500ms for standard packs
- Scales with pack size and number of files
- Can be disabled if performance is critical

## Command Permissions

```
oraxen.command.validate  # Required to use /oraxen validate command
```

## Troubleshooting

### "Resource pack file not found" error
- Make sure the pack has been generated at least once
- Regenerate the pack using `/oraxen pack generate` or `/oraxen reload`

### Validation finds many warnings but pack works fine
- Warnings indicate potential issues but don't prevent functionality
- Review specific warnings to improve pack quality
- Some warnings are informational and safe to ignore

### Generation aborted due to validation errors
- Check the validation report for specific errors
- Fix the reported issues in your configuration files
- If `validator_fail_on_error` is true, consider setting it to false temporarily to debug

## API Usage

Developers can use the validator programmatically:

```java
import io.th0rgal.oraxen.pack.generation.validator.ResourcePackValidator;
import io.th0rgal.oraxen.pack.generation.validator.ValidatorReport;

// Create validator with your VirtualFile list
ResourcePackValidator validator = new ResourcePackValidator(files);

// Run validation
ValidatorReport report = validator.validate();

// Get results
if (report.hasErrors()) {
    // Handle errors
}

report.printReport();  // Print to console
```

## Implementation Details

### Classes

- **`ResourcePackValidator`**: Main validator class that runs all checks
- **`ValidatorReport`**: Stores validation results and provides reporting
- **`ValidatorCommand`**: Command implementation for `/oraxen validate`

### File Paths

```
core/src/main/java/io/th0rgal/oraxen/pack/generation/validator/
├── ResourcePackValidator.java
└── ValidatorReport.java

core/src/main/java/io/th0rgal/oraxen/commands/
└── ValidatorCommand.java
```

### Settings

- `VALIDATE_PACK`: Enable/disable automatic validation
- `VALIDATOR_FAIL_ON_ERROR`: Control behavior on validation errors

## Future Enhancements

Possible improvements for future versions:

- Auto-fix capabilities for common issues
- Texture optimization suggestions
- Model complexity warnings
- Performance impact analysis
- Integration with IDEs/editors
- Batch processing of multiple packs
- Custom validation rule support

## Support

For issues or feature requests related to the validator:
1. Generate a validation report using `/oraxen validate`
2. Check the Oraxen documentation
3. Report detailed findings to the Oraxen team
