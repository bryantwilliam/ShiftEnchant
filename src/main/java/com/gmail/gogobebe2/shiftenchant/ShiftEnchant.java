
package com.gmail.gogobebe2.shiftenchant;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ShiftEnchant extends JavaPlugin {
    @Override
    public void onEnable() {
        getLogger().info("Starting up <name>. If you need me to update this plugin, email at gogobebe2@gmail.com");
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling <name>. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        return false;
    }

    private void initializeEnchantments() {
        for (Enchantment enchantment : Enchantment.values()) {
            for (int level = enchantment.getStartLevel(); level < enchantment.getMaxLevel(); level++) {
                getConfig().addDefault("enchantments." + enchantment.getName() + ".level." + level + ".gold", 5);
            }
        }
    }

    private void initializeEnchantables() {
        for (Material material : Material.values()) {
            List<Enchantment> enchantments = new ArrayList<>();
            ItemStack item = new ItemStack(material);
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.canEnchantItem(item)) {
                    enchantments.add(enchantment);
                }
            }
        }
    }
}
