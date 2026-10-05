# EZRecipe

**EZRecipe** adds configurable crafting recipes to a Paper server, provides a recipe-viewer GUI, and can remove matching vanilla recipes when a replacement is enabled.

| | |
|---|---|
| **Current version** | `1.0.6` |
| **Minecraft target** | Paper `1.21.4` (Java 21) |
| **Build system** | Maven (`pom.xml`) |
| **Plugin author** | voidles02 |

## Features

- Configurable shaped crafting recipes: toggle them, choose the output and amount, set a display name, and define the crafting pattern and ingredients.
- Eight included recipes, with configurable recipes loaded and validated at startup.
- Recipe browser and crafting-pattern viewer at `/ez-show`.
- Vanilla recipe replacement for the Netherite Upgrade Template and Netherite Scrap recipes. The plugin removes matching `minecraft` recipes when `replace-vanilla: true` is enabled.
- `/setup` reapplies missing or invalid defaults and registers configured recipes; `/status` reports whether each enabled recipe is registered.
- Automatic defaults and repairs for the Golden Carrot, Netherite recipes, and Bottle o' Enchanting configuration.
- XSeries material matching for more resilient material-name handling.

## Included recipes

In the patterns below, each letter represents the ingredient shown in the legend; a space is an empty crafting slot.

| Recipe | Pattern | Ingredients | Output |
|---|---|---|---|
| Golden Apple | `NGN` / `GAG` / `NGN` | `N` Gold Nugget, `G` Gold Ingot, `A` Apple | 1 Golden Apple |
| Enchanted Golden Apple | `GGG` / `GAG` / `GGG` | `G` Gold Block, `A` Golden Apple | 1 Enchanted Golden Apple |
| Totem of Undying | `GEG` / `ENE` / `GEG` | `G` Gold Block, `E` Emerald Block, `N` Nether Star | 1 Totem of Undying |
| Cobweb | `SSS` / `SSS` / `SSS` | `S` String | 4 Cobwebs |
| Netherite Upgrade Template | `NNN` / `NDN` / `NNN` | `N` Netherrack, `D` Diamond | 1 Netherite Upgrade Template |
| Netherite Scrap | `SS ` / `GG ` / `   ` | `S` Saddle, `G` Gold Ingot | 1 Netherite Scrap |
| Golden Carrot | ` N ` / `NCN` / ` N ` | `N` Gold Nugget, `C` Carrot | 1 Golden Carrot |
| Bottle o' Enchanting | `GDG` / `GAG` / ` G ` | `G` Glass, `D` Glowstone Dust, `A` Amethyst Shard | 1 Bottle o' Enchanting |

The two Netherite recipes are marked to replace matching vanilla recipes by default. Other recipes leave vanilla recipes alone unless their configuration explicitly enables `replace-vanilla`.

## Commands and permissions

| Command | Alias | Permission | Description |
|---|---|---|---|
| `/setup` | `/ez-setup` | `ezrecipe.admin` (operator by default) | Applies recipe defaults and registers configured recipes. |
| `/status` | `/ez-status` | `ezrecipe.admin` (operator by default) | Shows the registration status of configured recipes. |
| `/ez-show` | — | None | Opens the recipe browser; click a recipe to inspect its pattern and use the back button to return. |

## Installation and first run

1. Stop the server and place `ez-recipe-1.0.6.jar` in its `plugins` folder.
2. Start the server. EZRecipe creates `plugins/EZRecipe/config.yml` from the bundled defaults and registers enabled recipes.
3. Adjust the generated configuration if desired, then restart the server or run `/setup` to re-register recipes.
4. Players can open the recipe browser with `/ez-show`.

Back up `config.yml` before upgrading. Missing recipe defaults are added automatically; valid custom values are retained. Invalid required recipe data may be repaired to the bundled defaults so that the recipe can be registered.

## Configuration

Recipes are stored under `recipes` in `plugins/EZRecipe/config.yml`. Each recipe can use these settings:

| Setting | Purpose |
|---|---|
| `enabled` | Enables or disables the recipe. Defaults to `true` when omitted. |
| `display-name` | Name displayed in the recipe browser. Legacy color codes such as `&6` are supported. |
| `result` | Output material name. |
| `amount` | Number of output items; must be within the result's stack limit. |
| `shape` | One to three equally sized rows, each one to three characters wide. A space marks an empty slot. |
| `ingredients` | Maps each non-space pattern character to a material name. |
| `replace-vanilla` | When `true`, removes matching vanilla (`minecraft`) recipes with the same output material before registration. Defaults to `false` for user-defined recipes. |

Example:

```yaml
recipes:
  golden-carrot:
    enabled: true
    display-name: '&6Golden Carrot'
    result: GOLDEN_CARROT
    amount: 1
    shape:
      - ' N '
      - NCN
      - ' N '
    ingredients:
      N: GOLD_NUGGET
      C: CARROT
```

Recipe IDs must contain only lowercase letters, numbers, `.`, `_`, or `-`. Every non-space character in a shape must have a matching one-character key under `ingredients`. Invalid recipes are skipped and reported in the server log.

## Troubleshooting

- **A recipe is missing:** Check that its `enabled` value is `true`, verify its shape and ingredient keys, then inspect the server log for a validation warning.
- **A configuration recipe needs to be registered again:** Run `/setup` as an operator or restart the server.
- **The vanilla version still appears:** Confirm `replace-vanilla: true` for that recipe and that the configured result material matches the vanilla recipe's output.
- **A command is denied:** `/setup` and `/status` require `ezrecipe.admin`; this permission is granted to operators by default.

## Project notes

The project uses Maven only; `pom.xml` is the build definition. The Paper API is provided by the server, and XSeries is included in the plugin JAR.