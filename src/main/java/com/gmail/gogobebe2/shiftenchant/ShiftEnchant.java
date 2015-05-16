
package com.gmail.gogobebe2.shiftenchant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public class ShiftEnchant extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftEnchant. If you need me to update this plugin, email at gogobebe2@gmail.com");
        initializeDefaultEnchantments();
        this.getConfig().options().copyDefaults(true);
        saveConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ShiftEnchant. If you need me to update this plugin, email at gogobebe2@gmail.com");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (label.equalsIgnoreCase("ench") || label.equalsIgnoreCase("enchant")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Error! You have to be a player to use this command!");
                return true;
            }
            Player player = (Player) sender;
            ItemStack item = player.getItemInHand();
            EnchantmentTarget enchantmentTarget = getEnchantmentTarget(item);
            if (enchantmentTarget == null) {
                player.sendMessage(ChatColor.RED + "Error! That item is not enchantable!");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "Opening enchantment shop");
            openGui(player, enchantmentTarget);
            return true;
        }
        return false;
    }

    private void openGui(Player player, EnchantmentTarget enchantmentTarget) {
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                + "Enchantment Shop");
        int slot = 0;
        for (String enchantmentName : getConfig().getConfigurationSection("enchantments").getKeys(false)) {
            Enchantment enchantment = Enchantment.getByName(enchantmentName);
            if (enchantment != null && enchantment.getItemTarget() != null && enchantment.getItemTarget().equals(enchantmentTarget)) {
                for (String level : getConfig().getConfigurationSection("enchantments." + enchantmentName + ".level").getKeys(false)) {
                    ItemStack book = new ItemStack(Material.ENCHANTED_BOOK, 1);
                    ItemMeta bookMeta = book.getItemMeta();

                    bookMeta.setDisplayName(ChatColor.AQUA + enchantment.getName());
                    List<String> bookLore = new ArrayList<>();
                    bookLore.add(ChatColor.AQUA + "Level: " + ChatColor.BLUE + ChatColor.BOLD + level);
                    bookLore.add(ChatColor.GOLD + "" + ChatColor.BOLD + getConfig().getInt("enchantments." + enchantment.getName() + ".level." + level + ".gold"));
                    bookMeta.setLore(bookLore);
                    book.setItemMeta(bookMeta);

                    inventory.setItem(slot++, book);
                }

            }
        }
        player.openInventory(inventory);

    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!inventory.getName().equals(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                + "Enchantment Shop")) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null || item.getType().equals(Material.AIR) || !item.hasItemMeta()
                || !item.getType().equals(Material.ENCHANTED_BOOK)) {
            return;
        }
        String bookName;
        List<String> bookLore;
        int level;
        int cost;
        int goldCarried = inventory.all(Material.GOLD_INGOT).size();
        try {
            bookName = ChatColor.stripColor(item.getItemMeta().getDisplayName());
            bookLore = item.getItemMeta().getLore();
            //noinspection ConstantConditions
            level = Integer.parseInt(ChatColor.stripColor(bookLore.get(0)).replace("Level: ", " "));
            //noinspection ConstantConditions
            cost = Integer.parseInt(ChatColor.stripColor(bookLore.get(1)));
        } catch (NullPointerException | NumberFormatException exc) {
            player.sendMessage(ChatColor.RED + "An error occurred! That item isn't supposed to be in the gui!");
            return;
        }

        if (goldCarried < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough gold ingots to buy " + ChatColor.BLUE + bookName);
            return;
        }

        player.closeInventory();

        player.getItemInHand().addEnchantment(Enchantment.getByName(bookName), level);
        player.sendMessage(ChatColor.AQUA + player.getItemInHand().getItemMeta().getDisplayName() + " enchanted with "
                + ChatColor.BLUE + bookName + " for " + ChatColor.GOLD + cost);
    }

    private EnchantmentTarget getEnchantmentTarget(ItemStack item) {
        for (EnchantmentTarget enchantmentTarget : EnchantmentTarget.values()) {
            if (!(enchantmentTarget.equals(EnchantmentTarget.ALL) || enchantmentTarget.equals(EnchantmentTarget.ARMOR))) {
                if (enchantmentTarget.includes(item)) {
                    return enchantmentTarget;
                }
            }
        }
        return null;
    }

    private void initializeDefaultEnchantments() {
        for (Enchantment enchantment : Enchantment.values()) {
            for (int level = enchantment.getStartLevel(); level < enchantment.getMaxLevel(); level++) {
                getConfig().addDefault("enchantments." + enchantment.getName() + ".level." + level + ".gold", 5);
            }
        }
    }
}
