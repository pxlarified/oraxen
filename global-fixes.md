> [!TIP]
> Fixes critical logic bug in texture merge that silently discards merged item textures.
>
> **Changes**
> - Fixed `DuplicationHandler.getItemTextures()` returning `null` instead of constructed object
> - Changed line 98 from `return null;` to `return newTextures;`
> - This ensures merged textures from duplicate item models are preserved in resource pack generation
> - Previously caused silent data loss when merging multiple item model variants
>
> **Impact**
> - Resource packs no longer lose texture definitions when duplicate items are detected
> - Fixes broken texture rendering for custom items with model predicates
> - No breaking changes to public API
>
> **Test Coverage**
> - Unit test: Verify `getItemTextures()` returns non-null JsonObject with merged data
> - Integration test: Load items with duplicate texture definitions and verify pack generation

> [!TIP]
> Improves exception logging in ResourcePack generation to use structured logging system.
>
> **Changes**
> - Replaced `e.printStackTrace()` with `Logs.logError()` in two exception handlers
> - Added conditional debug logging: `if (Settings.DEBUG.toBool()) e.printStackTrace();`
> - Line 83: Pack file deletion failure now logs meaningful error message
> - Line 148: File gathering failure now logs meaningful error message
> - Ensures all exceptions flow through the configured logging system
>
> **Impact**
> - Administrators can now control exception verbosity via debug setting
> - Log files now capture resource pack generation errors properly
> - Improves debugging when packs fail to generate due to I/O issues
> - No functional behavior change, only logging improvement

> [!TIP]
> Adds structured logging to ZipUtils and fixes missing import.
>
> **Changes**
> - Added import: `import io.th0rgal.oraxen.utils.logs.Logs;`
> - Replaced `ex.printStackTrace()` with `Logs.logError()` and conditional debug printing
> - Line 36: ZIP compression failures now logged with context message
> - Ensures compression errors are captured in plugin logs instead of stderr
>
> **Impact**
> - Resource pack compression failures are now visible in admin logs
> - Administrators can diagnose pack generation issues without accessing server console stderr
> - Follows project logging conventions consistently

> [!TIP]
> Prevents NullPointerException in SelfHost SHA1 calculation.
>
> **Changes**
> - Added null check in `SelfHost.getSHA1()` method
> - Lines 144-147: Returns empty byte array if SHA1 was not calculated
> - Logs error message when SHA1 calculation fails: "SHA1 hash not calculated for resource pack"
> - Prevents crash when pack upload fails during SHA1 computation
>
> **Impact**
> - Fixes potential crash when resource pack hosting fails
> - Graceful fallback prevents cascading failures in upload system
> - Player pack download no longer crashes server if SHA1 calculation error occurs
> - Upstream callers can detect failure via empty byte array
>
> **Risk Mitigation**
> - Without this fix: NPE could crash the entire plugin during server startup
> - With this fix: Upload fails gracefully with logged error message

> [!TIP]
> Improves error reporting in self-hosted resource pack uploads.
>
> **Changes**
> - Updated exception message to include underlying error: `e.getMessage()`
> - Enhanced logging in `uploadPack()` catch block
> - Administrators now see the actual cause (network error, disk full, permissions, etc.)
> - More helpful diagnostics when self-hosting fails
>
> **Impact**
> - Easier troubleshooting of resource pack hosting failures
> - Administrators can quickly identify network vs. filesystem issues
> - Better support for community when issues are reported with full error messages

> [!TIP]
> Replaces unsafe assertion with proper null check in item configuration loading.
>
> **Changes**
> - Replaced `assert packSection != null;` with explicit null check in `ItemParser` constructor
> - Lines 86-91: Now wraps model data parsing in null-safe block
> - Prevents NullPointerException when Pack configuration section is missing
> - Logging added to track incomplete item configurations
>
> **Impact**
> - Fixes silent crash in production (assertions disabled by default in Java production JVMs)
> - Items with malformed Pack sections now handled gracefully
> - Server no longer crashes when loading items with incomplete custom model data
> - Custom model data is silently skipped if Pack section is null
>
> **Why This Matters**
> - Production Java typically runs with assertions disabled (`-ea` flag not set)
> - Previous code assumed assertion would prevent null dereference, but it doesn't
> - This is a latent crash bug that only manifests in production environments

> [!TIP]
> Replaces unsafe assertions with proper null checks in furniture entity spawning.
>
> **Changes**
> - Replaced duplicate `assert location.getWorld() != null;` checks in two locations
> - Lines 366-369: Added explicit null check with early return before entity spawn
> - Lines 754-758: Added World variable capture and null check before nearby entity search
> - Both paths now log warnings when world becomes unexpectedly null
>
> **Impact**
> - Fixes critical NullPointerException in furniture mechanics (lines 366, 755)
> - Prevents furniture entities from spawning if world is unloaded
> - Server no longer crashes when furniture is placed during world unload
> - Graceful degradation: placement silently fails with logged warning instead of crash
>
> **Risk Mitigation**
> - Without fix: Player interacting with furniture during world unload crashes server
> - With fix: Furniture placement returns null, player sees no visual change
> - Administrators see warning in logs, can investigate world management issues
>
> **Why Assertions Fail in Production**
> - Java assertions require `-ea` JVM flag to enable (rarely used in production)
> - Code depended on assertions to ensure world safety, but they don't execute
> - This created latent crash bugs visible only in production environments
