package com.KDI.ezrecipe;

import com.cryptomorin.xseries.XMaterial;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

public final class EzRecipeCommand implements CommandExecutor, Listener {
    private static final String MAIN_TITLE = "§2EZRecipes §8» §fRecipes";
    private static final int[] RECIPE_SLOTS = {10, 11, 12, 13, 14, 15};
    private static final int[] GRID_SLOTS = {10, 11, 12, 19, 20, 21, 28, 29, 30};

    private final RecipeManager recipeManager;

    public EzRecipeCommand(RecipeManager recipeManager) {
        this.recipeManager = recipeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (command.getName().toLowerCase()) {
            case "ez-setup" -> {
                recipeManager.setup();
                sender.sendMessage("§aEZRecipe has been set up and its recipes were registered.");
            }
            case "ez-status" -> recipeManager.sendStatus(sender);
            case "ez-show" -> {
                if (sender instanceof Player player) {
                    openRecipeMenu(player);
                } else {
                    sender.sendMessage("§cOnly players can open the recipe menu.");
                }
            }
            default -> sender.sendMessage("§cUnknown EZRecipe command.");
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory topInventory = event.getView().getTopInventory();
        if (!(topInventory.getHolder() instanceof RecipeMenuHolder holder)) {
            return;
        }

        event.setCancelled(true);
        if (event.getRawSlot() < 0 || event.getRawSlot() >= topInventory.getSize()) {
            return;
        }

        if (holder.recipeId == null) {
            List<String> ids = holder.recipeIds;
            if (event.getRawSlot() == 18 && holder.page > 0) {
                openRecipeMenu(player, holder.page - 1);
                return;
            }
            if (event.getRawSlot() == 26 && (holder.page + 1) * RECIPE_SLOTS.length < ids.size()) {
                openRecipeMenu(player, holder.page + 1);
                return;
            }
            for (int index = 0; index < RECIPE_SLOTS.length && index < ids.size(); index++) {
                if (event.getRawSlot() == RECIPE_SLOTS[index]) {
                    int recipeIndex = holder.page * RECIPE_SLOTS.length + index;
                    if (recipeIndex < ids.size()) {
                        openRecipe(player, ids.get(recipeIndex), holder.page);
                    }
                    return;
                }
            }
        } else if (event.getRawSlot() == 40) {
            openRecipeMenu(player, holder.page);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof RecipeMenuHolder)) {
            return;
        }

        int topSize = event.getView().getTopInventory().getSize();
        for (int slot : event.getRawSlots()) {
            if (slot < topSize) {
                event.setCancelled(true);
                return;
            }
        }
    }

    private void openRecipeMenu(Player player) {
        openRecipeMenu(player, 0);
    }

    private void openRecipeMenu(Player player, int requestedPage) {
        List<String> ids = getDisplayableRecipeIds();
        int pageCount = Math.max(1, (ids.size() + RECIPE_SLOTS.length - 1) / RECIPE_SLOTS.length);
        int page = Math.max(0, Math.min(requestedPage, pageCount - 1));
        RecipeMenuHolder holder = new RecipeMenuHolder(null, ids, page);
        Inventory inventory = Bukkit.createInventory(holder, 27, MAIN_TITLE);
        holder.inventory = inventory;
        fill(inventory, "BLACK_STAINED_GLASS_PANE");

        int firstRecipe = page * RECIPE_SLOTS.length;
        for (int index = 0; index < RECIPE_SLOTS.length && firstRecipe + index < ids.size(); index++) {
            String id = ids.get(firstRecipe + index);
            ConfigurationSection section = recipeManager.getRecipeSection(id);
            if (section == null) {
                continue;
            }

            Material result = recipeManager.parseMaterial(section.getString("result"));
            if (result == null) {
                continue;
            }

            ItemStack item = new ItemStack(result, Math.max(1, section.getInt("amount", 1)));
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(color(section.getString("display-name", id)));
                meta.setLore(List.of("§7Click to view this recipe."));
                item.setItemMeta(meta);
            }
            inventory.setItem(RECIPE_SLOTS[index], item);
        }

        if (page > 0) {
            inventory.setItem(18, item("ARROW", "§ePrevious page", List.of("§7Page " + page + " of " + pageCount)));
        }
        inventory.setItem(22, item("PAPER", "§fPage " + (page + 1) + " of " + pageCount, List.of()));
        if (page + 1 < pageCount) {
            inventory.setItem(26, item("ARROW", "§eNext page", List.of("§7Page " + (page + 2) + " of " + pageCount)));
        }

        player.openInventory(inventory);
    }

    private void openRecipe(Player player, String id, int page) {
        ConfigurationSection section = recipeManager.getRecipeSection(id);
        if (section == null) {
            return;
        }

        String displayName = color(section.getString("display-name", id));
        RecipeMenuHolder holder = new RecipeMenuHolder(id, List.of(), page);
        Inventory inventory = Bukkit.createInventory(holder, 45,
                "§2Recipe §8» " + displayName);
        holder.inventory = inventory;
        fill(inventory, "BLACK_STAINED_GLASS_PANE");

        List<String> shape = section.getStringList("shape");
        ConfigurationSection ingredients = section.getConfigurationSection("ingredients");
        for (int index = 0; index < GRID_SLOTS.length; index++) {
            int row = index / 3;
            int column = index % 3;
            String patternRow = row < shape.size() ? shape.get(row) : "";
            if (column >= patternRow.length() || ingredients == null) {
                continue;
            }

            String symbol = String.valueOf(patternRow.charAt(column));
            Material material = recipeManager.parseMaterial(ingredients.getString(symbol));
            if (material != null) {
                inventory.setItem(GRID_SLOTS[index], new ItemStack(material));
            }
        }

        inventory.setItem(13, item("ARROW", "§bCrafting result", List.of("§7Ingredients produce the item on the right.")));

        Material result = recipeManager.parseMaterial(section.getString("result"));
        if (result != null) {
            ItemStack output = new ItemStack(result, Math.max(1, section.getInt("amount", 1)));
            ItemMeta meta = output.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(displayName);
                meta.setLore(List.of("§7Result: §f" + section.getInt("amount", 1) + "x"));
                output.setItemMeta(meta);
            }
            inventory.setItem(16, output);
        }

        ItemStack back = item("ARROW", "§cBack to recipes", List.of("§7Click to return to page " + (page + 1) + "."));
        if (back != null) {
            inventory.setItem(40, back);
        }
        player.openInventory(inventory);
    }

    private List<String> getDisplayableRecipeIds() {
        return recipeManager.getEnabledRecipeIds().stream()
                .filter(recipeManager::isRecipeValid)
                .toList();
    }

    private void fill(Inventory inventory, String materialName) {
        ItemStack filler = item(materialName, "§7", List.of());
        if (filler == null) {
            return;
        }
        for (int slot = 0; slot < inventory.getSize(); slot++) {
            inventory.setItem(slot, filler.clone());
        }
    }

    private ItemStack item(String materialName, String name, List<String> lore) {
        ItemStack item = XMaterial.matchXMaterial(materialName).map(XMaterial::parseItem).orElse(null);
        if (item == null) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String color(String text) {
        return text.replace('&', '§');
    }

    private static final class RecipeMenuHolder implements InventoryHolder {
        private final String recipeId;
        private final List<String> recipeIds;
        private final int page;
        private Inventory inventory;

        private RecipeMenuHolder(String recipeId, List<String> recipeIds, int page) {
            this.recipeId = recipeId;
            this.recipeIds = recipeIds;
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return inventory;
        }
    }
}