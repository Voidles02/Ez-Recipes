package com.KDI.ezrecipe;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.StringJoiner;

public final class RecipeManager implements Listener {
    private final EzRecipePlugin plugin;
    private Set<Material> replacedVanillaResults = Set.of();

    public RecipeManager(EzRecipePlugin plugin) {
        this.plugin = plugin;
    }

    public void ensureLatestDefaults() {
        ConfigurationSection recipes = plugin.getConfig().getConfigurationSection("recipes");
        if (recipes == null) {
            recipes = plugin.getConfig().createSection("recipes");
        }

        boolean changed = repairGoldenCarrot(recipes);
        changed |= migrateLegacyNetheriteScrapRecipe(recipes);
        changed |= ensureDefaultRecipe(recipes, "netherite-template", "&cUpgrade Template", "NETHERITE_UPGRADE_SMITHING_TEMPLATE", List.of("NNN", "NDN", "NNN"), new String[][]{{"N", "NETHERRACK"}, {"D", "DIAMOND"}});
        changed |= migrateNetheriteTemplateDisplayName(recipes);
        changed |= ensureDefaultRecipe(recipes, "netherite-ingot", "&8Netherite Ingot", "NETHERITE_INGOT", List.of("SS", "GG"), new String[][]{{"S", "NETHERITE_SCRAP"}, {"G", "GOLD_INGOT"}});
        changed |= migrateOldNetheriteIngotPattern(recipes);
        ConfigurationSection section = recipes.getConfigurationSection("exp-bottle");
        if (section == null) {
            section = recipes.createSection("exp-bottle");
        }

        if (!section.contains("enabled")) {
            section.set("enabled", true);
            changed = true;
        }
        if (!section.contains("display-name")) {
            section.set("display-name", "&bBottle o' Enchanting");
            changed = true;
        }
        if (!section.contains("result")) {
            section.set("result", "EXPERIENCE_BOTTLE");
            changed = true;
        }
        if (!section.contains("amount")) {
            section.set("amount", 1);
            changed = true;
        }
        if (!section.contains("shape")) {
            section.set("shape", List.of("GDG", "GAG", " G "));
            changed = true;
        }

        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) {
            ingredients = section.createSection("ingredients");
        }
        if (!ingredients.contains("G")) {
            ingredients.set("G", "GLASS");
            changed = true;
        }
        if (!ingredients.contains("D")) {
            ingredients.set("D", "GLOWSTONE_DUST");
            changed = true;
        }
        if (!ingredients.contains("A")) {
            ingredients.set("A", "AMETHYST_SHARD");
            changed = true;
        }

        if (!validateRecipe("exp-bottle", section, false)) {
            section.set("result", "EXPERIENCE_BOTTLE");
            section.set("amount", 1);
            section.set("shape", List.of("GDG", "GAG", " G "));
            section.set("ingredients.G", "GLASS");
            section.set("ingredients.D", "GLOWSTONE_DUST");
            section.set("ingredients.A", "AMETHYST_SHARD");
            changed = true;
        }

        if (changed) {
            plugin.saveConfig();
            plugin.getLogger().info("Added or repaired the exp-bottle recipe in config.yml.");
        }
    }

    private boolean migrateLegacyNetheriteScrapRecipe(ConfigurationSection recipes) {
        ConfigurationSection legacy = recipes.getConfigurationSection("netherite-scrap");
        if (legacy == null) {
            return false;
        }

        boolean enabled = legacy.getBoolean("enabled", true);
        recipes.set("netherite-scrap", null);
        ConfigurationSection replacement = recipes.getConfigurationSection("netherite-ingot");
        if (replacement == null) {
            replacement = recipes.createSection("netherite-ingot");
            replacement.set("enabled", enabled);
            replacement.set("display-name", "&8Netherite Ingot");
            replacement.set("result", "NETHERITE_INGOT");
            replacement.set("amount", 1);
            replacement.set("replace-vanilla", true);
            replacement.set("shape", List.of("SS", "GG"));
            ConfigurationSection ingredients = replacement.createSection("ingredients");
            ingredients.set("S", "NETHERITE_SCRAP");
            ingredients.set("G", "GOLD_INGOT");
        }
        return true;
    }

    private boolean migrateOldNetheriteIngotPattern(ConfigurationSection recipes) {
        ConfigurationSection ingot = recipes.getConfigurationSection("netherite-ingot");
        if (ingot == null) {
            return false;
        }
        List<String> shape = ingot.getStringList("shape");
        if (!List.of("SG", "GS").equals(shape) && !List.of("SGS", "G G", "SGS").equals(shape)) {
            return false;
        }
        ingot.set("shape", List.of("SS", "GG"));
        return true;
    }

    private boolean migrateNetheriteTemplateDisplayName(ConfigurationSection recipes) {
        ConfigurationSection template = recipes.getConfigurationSection("netherite-template");
        if (template == null || !"&cNetherite Upgrade Template".equals(template.getString("display-name"))) {
            return false;
        }
        template.set("display-name", "&cUpgrade Template");
        return true;
    }

    private boolean ensureDefaultRecipe(ConfigurationSection recipes, String id, String displayName, String result, List<String> shape, String[][] ingredientDefaults) {
        boolean changed = false;
        ConfigurationSection section = recipes.getConfigurationSection(id);
        if (section == null) {
            section = recipes.createSection(id);
            changed = true;
        }
        if (!section.contains("enabled")) {
            section.set("enabled", true);
            changed = true;
        }
        if (!section.contains("display-name")) {
            section.set("display-name", displayName);
            changed = true;
        }
        if (!section.contains("result")) {
            section.set("result", result);
            changed = true;
        }
        if (!section.contains("amount")) {
            section.set("amount", 1);
            changed = true;
        }
        if (!section.contains("shape")) {
            section.set("shape", shape);
            changed = true;
        }
        if (!section.contains("replace-vanilla")) {
            section.set("replace-vanilla", true);
            changed = true;
        }
        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        if (ingredients == null) {
            ingredients = section.createSection("ingredients");
            changed = true;
        }
        for (String[] ingredient : ingredientDefaults) {
            if (!ingredients.contains(ingredient[0])) {
                ingredients.set(ingredient[0], ingredient[1]);
                changed = true;
            }
        }
        if (!validateRecipe(id, section, false)) {
            section.set("result", result);
            section.set("amount", 1);
            section.set("shape", shape);
            section.set("ingredients", null);
            ingredients = section.createSection("ingredients");
            for (String[] ingredient : ingredientDefaults) {
                ingredients.set(ingredient[0], ingredient[1]);
            }
            section.set("replace-vanilla", true);
            changed = true;
        }
        return changed;
    }

    private boolean repairGoldenCarrot(ConfigurationSection recipes) {
        ConfigurationSection section = recipes.getConfigurationSection("golden-carrot");
        if (section != null && validateRecipe("golden-carrot", section, false)) {
            return false;
        }
        if (section == null) {
            section = recipes.createSection("golden-carrot");
        }

        section.set("enabled", true);
        section.set("display-name", "&6Golden Carrot");
        section.set("result", "GOLDEN_CARROT");
        section.set("amount", 1);
        section.set("shape", List.of(" N ", "NCN", " N "));
        section.set("ingredients.N", "GOLD_NUGGET");
        section.set("ingredients.C", "CARROT");
        section.set("ingredients.G", null);
        return true;
    }

    public void setup() {
        ensureLatestDefaults();
        if (!plugin.getConfig().getBoolean("setup-complete", false)) {
            plugin.getConfig().set("setup-complete", true);
            plugin.saveConfig();
        }
        registerConfiguredRecipes();
    }

    public void registerConfiguredRecipes() {
        ConfigurationSection recipes = plugin.getConfig().getConfigurationSection("recipes");
        if (recipes == null) {
            plugin.getLogger().warning("No recipes section was found in config.yml.");
            return;
        }

        replacedVanillaResults = collectReplacedVanillaResults(recipes);
        removeExistingPluginRecipes();

        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null || !section.getBoolean("enabled", true)) {
                continue;
            }
            registerRecipe(id, section);
        }
    }

    public void unregisterConfiguredRecipes() {
        removeExistingPluginRecipes();
    }

    private Set<Material> collectReplacedVanillaResults(ConfigurationSection recipes) {
        Set<Material> replacedResults = new HashSet<>();
        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null || !section.getBoolean("enabled", true) || !section.getBoolean("replace-vanilla", false)
                    || !validateRecipe(id, section, false)) {
                continue;
            }
            Material result = material(section.getString("result"));
            if (result != null) {
                replacedResults.add(result);
            }
        }
        return replacedResults;
    }

    private void removeExistingPluginRecipes() {
        List<NamespacedKey> keys = new ArrayList<>();
        Iterator<Recipe> iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            Recipe recipe = iterator.next();
            if (!(recipe instanceof Keyed keyed)) {
                continue;
            }

            NamespacedKey key = keyed.getKey();
            String namespace = key.getNamespace();
            boolean pluginRecipe = namespace.equals(plugin.getName().toLowerCase(Locale.ROOT)) || namespace.equals("ezrecipes");
            if (pluginRecipe) {
                keys.add(key);
            }
        }
        for (NamespacedKey key : keys) {
            Bukkit.removeRecipe(key);
        }
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        Recipe recipe = event.getRecipe();
        if (isReplacedVanillaRecipe(recipe)) {
            event.getInventory().setResult(null);
        }
    }

    @EventHandler
    public void onPrepareSmithing(PrepareSmithingEvent event) {
        if (isReplacedVanillaRecipe(event.getInventory().getRecipe())) {
            event.setResult(null);
        }
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        if (isReplacedVanillaRecipe(event.getRecipe())) {
            event.setCancelled(true);
        }
    }

    private boolean isReplacedVanillaRecipe(Recipe recipe) {
        if (!(recipe instanceof Keyed keyed)) {
            return false;
        }
        NamespacedKey key = keyed.getKey();
        return key.getNamespace().equals("minecraft")
                && replacedVanillaResults.contains(recipe.getResult().getType());
    }

    public void sendStatus(CommandSender sender) {
        ConfigurationSection recipes = plugin.getConfig().getConfigurationSection("recipes");
        sender.sendMessage("§6§lEZRecipe Status");
        if (recipes == null) {
            sender.sendMessage("§cNo recipe configuration was found.");
            return;
        }

        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null || !section.getBoolean("enabled", true)) {
                sender.sendMessage("§7- §f" + id + ": §eDisabled");
                continue;
            }

            NamespacedKey key = recipeKey(id);
            if (key == null) {
                sender.sendMessage("§7- §f" + id + ": §cInvalid recipe ID");
                continue;
            }
            Recipe recipe = Bukkit.getRecipe(key);
            if (recipe == null) {
                sender.sendMessage("§7- §f" + id + ": §cNot registered");
            } else {
                sender.sendMessage("§7- §f" + id + ": §aWorking");
            }
        }
    }

    public void sendRecipes(CommandSender sender) {
        ConfigurationSection recipes = plugin.getConfig().getConfigurationSection("recipes");
        sender.sendMessage("§6§lEZRecipe Recipes");
        if (recipes == null) {
            sender.sendMessage("§cNo recipes are configured.");
            return;
        }

        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section == null || !section.getBoolean("enabled", true)) {
                continue;
            }

            String displayName = section.getString("display-name", id);
            String result = section.getString("result", "UNKNOWN");
            int amount = section.getInt("amount", 1);
            List<String> shape = section.getStringList("shape");
            ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
            StringJoiner ingredientText = new StringJoiner("§7, §f");
            if (ingredients != null) {
                for (String symbol : ingredients.getKeys(false)) {
                    ingredientText.add(symbol + " = " + ingredients.getString(symbol, "UNKNOWN"));
                }
            }

            sender.sendMessage("§e" + color(displayName) + " §7-> §f" + amount + "x " + result);
            sender.sendMessage("§7  Pattern: §f" + String.join(" / ", shape));
            sender.sendMessage("§7  Ingredients: §f" + ingredientText);
        }
    }

    public List<String> getEnabledRecipeIds() {
        ConfigurationSection recipes = plugin.getConfig().getConfigurationSection("recipes");
        List<String> ids = new ArrayList<>();
        if (recipes == null) {
            return ids;
        }

        for (String id : recipes.getKeys(false)) {
            ConfigurationSection section = recipes.getConfigurationSection(id);
            if (section != null && section.getBoolean("enabled", true)) {
                ids.add(id);
            }
        }
        return ids;
    }

    public ConfigurationSection getRecipeSection(String id) {
        return plugin.getConfig().getConfigurationSection("recipes." + id);
    }

    public Material parseMaterial(String name) {
        return material(name);
    }

    public boolean isRecipeValid(String id) {
        ConfigurationSection section = getRecipeSection(id);
        return section != null && validateRecipe(id, section, false);
    }

    private void registerRecipe(String id, ConfigurationSection section) {
        if (!validateRecipe(id, section, true)) {
            return;
        }

        Material result = material(section.getString("result"));
        List<String> shape = section.getStringList("shape");
        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        NamespacedKey key = recipeKey(id);
        try {
            ItemStack output = new ItemStack(result, section.getInt("amount", 1));
            ShapedRecipe recipe = new ShapedRecipe(key, output);
            recipe.shape(shape.toArray(new String[0]));

            for (String symbol : ingredients.getKeys(false)) {
                Material ingredient = material(ingredients.getString(symbol));
                if (ingredient != null) {
                    recipe.setIngredient(symbol.charAt(0), ingredient);
                }
            }

            if (!Bukkit.addRecipe(recipe)) {
                plugin.getLogger().warning("Could not register recipe: " + id);
            }
        } catch (IllegalArgumentException exception) {
            plugin.getLogger().warning("Skipping recipe " + id + ": " + exception.getMessage());
        }
    }

    private boolean validateRecipe(String id, ConfigurationSection section, boolean log) {
        NamespacedKey key = recipeKey(id);
        Material result = material(section.getString("result"));
        List<String> shape = section.getStringList("shape");
        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        int amount = section.getInt("amount", 1);

        if (key == null) {
            return invalid(id, "recipe IDs may only contain lowercase letters, numbers, '.', '_' and '-'.", log);
        }
        if (result == null) {
            return invalid(id, "unknown result material.", log);
        }
        if (amount < 1 || amount > result.getMaxStackSize()) {
            return invalid(id, "amount must be between 1 and " + result.getMaxStackSize() + ".", log);
        }
        if (shape.isEmpty() || shape.size() > 3) {
            return invalid(id, "shape must contain between 1 and 3 rows.", log);
        }
        int width = shape.get(0).length();
        if (width < 1 || width > 3) {
            return invalid(id, "shape rows must contain between 1 and 3 characters.", log);
        }
        for (String row : shape) {
            if (row.length() != width || row.length() > 3) {
                return invalid(id, "all shape rows must have the same length between 1 and 3 characters.", log);
            }
        }
        if (ingredients == null) {
            return invalid(id, "ingredients are missing.", log);
        }
        for (String symbol : ingredients.getKeys(false)) {
            if (symbol.length() != 1 || symbol.charAt(0) == ' ') {
                return invalid(id, "ingredient keys must be one non-space character.", log);
            }
            if (material(ingredients.getString(symbol)) == null) {
                return invalid(id, "unknown ingredient " + symbol + ".", log);
            }
        }
        for (String row : shape) {
            for (int column = 0; column < row.length(); column++) {
                char symbol = row.charAt(column);
                if (symbol != ' ' && !ingredients.contains(String.valueOf(symbol))) {
                    return invalid(id, "shape symbol " + symbol + " has no ingredient.", log);
                }
            }
        }
        return true;
    }

    private boolean invalid(String id, String reason, boolean log) {
        if (log) {
            plugin.getLogger().warning("Skipping recipe " + id + ": " + reason);
        }
        return false;
    }

    private NamespacedKey recipeKey(String id) {
        if (id == null || !id.toLowerCase(Locale.ROOT).matches("[a-z0-9._-]+")) {
            return null;
        }
        return new NamespacedKey(plugin, "recipe_" + id.toLowerCase(Locale.ROOT));
    }

    private Material material(String name) {
        if (name == null) {
            return null;
        }
        return XMaterial.matchXMaterial(name).map(XMaterial::parseMaterial).orElse(null);
    }

    private String color(String text) {
        return text.replace('&', '§');
    }
}