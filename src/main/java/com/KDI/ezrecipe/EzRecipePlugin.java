package com.KDI.ezrecipe;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class EzRecipePlugin extends JavaPlugin {
    private RecipeManager recipeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.recipeManager = new RecipeManager(this);
        recipeManager.ensureLatestDefaults();
        if (!getConfig().getBoolean("setup-complete", false)) {
            recipeManager.setup();
        } else {
            recipeManager.registerConfiguredRecipes();
        }

        EzRecipeCommand command = new EzRecipeCommand(recipeManager);
        getServer().getPluginManager().registerEvents(recipeManager, this);
        getServer().getPluginManager().registerEvents(command, this);
        registerCommand("ez-setup", command);
        registerCommand("ez-status", command);
        registerCommand("ez-show", command);
    }

    @Override
    public void onDisable() {
        if (recipeManager != null) {
            recipeManager.unregisterConfiguredRecipes();
        }
    }

    private void registerCommand(String name, EzRecipeCommand command) {
        PluginCommand pluginCommand = getCommand(name);
        if (pluginCommand != null) {
            pluginCommand.setExecutor(command);
        } else {
            getLogger().warning("Command /" + name + " is missing from plugin.yml.");
        }
    }
}