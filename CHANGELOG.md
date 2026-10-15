# ✨ EZRecipe Changelog

Release notes for EZRecipe. Newest releases are listed first.

**Current version:** `1.0.16` · **Compatibility baseline:** Paper `1.21.4` · Java `21`

---

## `1.0.16` — Recipe viewer and stability fixes

### 🎨 Improved
- Repositioned the 3×3 crafting preview, result arrow, and output item into one aligned row group for easier reading.
- Clarified recipe-card details and pagination, and added a message when no enabled valid recipes are available.

### 🛡 Stability
- Legacy Netherite Scrap configuration migration now removes the retired setting without overwriting an existing customized Netherite Ingot recipe.

### 🛠 Fixed
- Empty cells and unused rows in crafting previews now render as empty slots instead of decorative filler panes.

### ℹ️ Compatibility
- Updated the Maven artifact and plugin metadata to `1.0.16`. The compatibility baseline remains Paper `1.21.4` with Java `21`.

---

## `1.0.15` — Recipe, menu, and stability improvements

### 🎨 Improved
- Shortened the built-in recipe name to **Upgrade Template**. Existing configurations using the old default name migrate automatically; custom names are preserved.
- Refreshed recipe menus with a dark background, updated title styling, and a clear crafting-result indicator.
- An invalid custom replacement recipe no longer suppresses its vanilla result when EZRecipe cannot register the replacement.
- EZRecipe unregisters its own recipes when disabled, preventing them from remaining active after a disable or reload.
- Updated the Netherite Ingot recipe to match the requested 2×2 layout: two Netherite Scraps across the top and two Gold Ingots across the bottom.
- Existing configurations with the earlier built-in Netherite Ingot patterns migrate to the updated layout.

### 🛡 Stability
- Added cleanup for EZRecipe-registered recipes during plugin shutdown.
- Replaced the built-in Netherite Ingot recipe with a 2-scrap, 2-gold recipe.
- Migrated configurations containing the previous built-in pattern.

### 🛠 Fixed
- Left vanilla recipes registered and handled replacement behavior without removing vanilla recipe definitions, avoiding the server's “Tried to load unrecognized recipe” warnings.
- Limited recipe suppression to matching vanilla recipes, so other plugins' recipes with the same result are not inadvertently blocked.

### ⚡ Performance
- Cached replacement checks to avoid repeating the same recipe validation work.

### 📚 Documentation
- Consolidated the recipe, menu, compatibility, performance, and stability notes into this release entry.

### ℹ️ Compatibility note

The documented compatibility baseline is Paper `1.21.4` with Java `21`. No support for older server versions is claimed by these updates.