
package com.gmail.gogobebe2.shiftenchant;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ShiftEnchant extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getLogger().info("Starting up ShiftEnchant. If you need me to update this plugin, email at gogobebe2@gmail.com");
        Bukkit.getPluginManager().registerEvents(this, this);
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
            if (item == null || item.getType().equals(Material.AIR)) {
                player.sendMessage(ChatColor.RED + "You aren't holding an item in your hand!");
                return true;
            }
            player.sendMessage(ChatColor.GREEN + "" + ChatColor.ITALIC + "Opening enchantment shop...");
            openGui(player, item);
            return true;
        }
        return false;
    }

    private void openGui(Player player, ItemStack item) {
        List<Enchantment> possibleEnchantments = getPossibleEnchantments(item.getType());
        Inventory inventory = Bukkit.createInventory(null, 54, ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD
                + "Enchantment Shop");
        int slot = 0;
        for (String enchantmentName : getConfig().getConfigurationSection("enchantments").getKeys(false)) {
            Enchantment enchantment = Enchantment.getByName(enchantmentName);
            if (enchantment != null && (possibleEnchantments.contains(enchantment))) {
                for (String level : getConfig().getConfigurationSection("enchantments." + enchantmentName + ".level").getKeys(false)) {
                    ItemStack book;
                    if (item.getEnchantments() != null && item.getEnchantments().keySet().contains(enchantment)) {
                        book = new ItemStack(Material.BARRIER, 1);
                    } else {
                        book = new ItemStack(Material.ENCHANTED_BOOK, 1);
                    }
                    EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) book.getItemMeta();
                    try {
                        bookMeta.addStoredEnchant(enchantment, Integer.parseInt(level), false);
                    } catch (NumberFormatException exc) {
                        player.sendMessage(ChatColor.RED + "Something went wrong in the config. " + level
                                + " is not a number");
                        return;
                    }

                    List<String> bookLore;
                    if (!bookMeta.hasLore()) {
                        bookLore = new ArrayList<>();
                    } else {
                        bookLore = bookMeta.getLore();
                    }
                    bookLore.add(ChatColor.GOLD + "Cost: " + ChatColor.BOLD + getConfig().getInt("enchantments." + enchantment.getName() + ".level." + level + ".gold") + ChatColor.GOLD + " gold.");
                    bookMeta.setLore(bookLore);
                    book.setItemMeta(bookMeta);

                    inventory.setItem(slot++, book);
                }
            }
        }
        player.openInventory(inventory);

    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (event.getDamager() == null || event.getEntity() == null || !(event.getDamager() instanceof Player)
                || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player damaged = (Player) event.getEntity();

        event.setCancelled(true);
        damaged.damage(event.getFinalDamage());

        double base;
        ItemStack axe = damager.getItemInHand();

        switch (axe.getType()) {
            case WOOD_AXE:
                base = 2;
                break;
            case STONE_AXE:
                base = 4;
                break;
            case GOLD_AXE:
                base = 4;
                break;
            case IRON_AXE:
                base = 7;
                break;
            case DIAMOND_AXE:
                base = 12;
                break;
            default:
                return;

        }
        int enchLevel = 0;
        if (axe.getEnchantments().size() > 0) {
            if (axe.getEnchantments().containsKey(Enchantment.DAMAGE_ALL)) {
                enchLevel = axe.getEnchantments().get(Enchantment.DAMAGE_ALL);
            }
        }
        base += ((enchLevel) * 1.5);
        ItemStack[] armourContents = damaged.getInventory().getArmorContents();
        if (armourContents == null || armourContents.length == 0) {
            return;
        }
        for (ItemStack armour : armourContents) {
            double finalDurability = armour.getDurability() + base;
            armour.setDurability((short) finalDurability);
        }
        damaged.getInventory().setArmorContents(armourContents);
        damaged.updateInventory();
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (!ChatColor.stripColor(inventory.getName()).equals("Enchantment Shop")) {
            return;
        }
        event.getPlayer().sendMessage(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Closing enchantment shop...");
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (!ChatColor.stripColor(inventory.getName()).equals("Enchantment Shop")) {
            return;
        }
        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();

        ItemStack item = event.getCurrentItem();
        if (item == null) {
            return;
        } else if (item.getType().equals(Material.AIR) || !item.hasItemMeta() || !item.getType().equals(Material.ENCHANTED_BOOK)) {
            if (item.getType().equals(Material.BARRIER)) {
                player.sendMessage(ChatColor.RED + "You already have this enchantment on silly!");
            }
            return;
        }

        EnchantmentStorageMeta bookMeta = (EnchantmentStorageMeta) item.getItemMeta();


        List<String> bookLore = bookMeta.getLore();

        List<Enchantment> enchantments = new ArrayList<>();
        for (Enchantment enchantment : bookMeta.getStoredEnchants().keySet()) {
            enchantments.add(enchantment);
        }

        Enchantment enchantment = enchantments.get(0);
        String enchantmentName = enchantment.getName();
        int level = bookMeta.getStoredEnchantLevel(enchantment);
        int cost = getConfig().getInt("enchantments." + enchantment.getName() + ".level." + level + ".gold");

        Inventory playerInventory = player.getInventory();
        Set<Integer> goldCarriedSlots = playerInventory.all(Material.GOLD_INGOT).keySet();
        int goldQuantity = 0;
        for (int slot : goldCarriedSlots) {
            ItemStack gold = playerInventory.getItem(slot);
            goldQuantity += gold.getAmount();
        }

        if (goldQuantity < cost) {
            player.sendMessage(ChatColor.RED + "You don't have enough gold ingots to buy " + ChatColor.BLUE + enchantmentName);
            player.sendMessage(ChatColor.BLUE + enchantmentName + ChatColor.YELLOW + " costs " + ChatColor.GOLD
                    + ChatColor.BOLD + cost + ChatColor.YELLOW + " gold and you only have " + ChatColor.GOLD
                    + ChatColor.BOLD + goldQuantity);
            return;
        }

        int goldToTake = cost;
        for (int slot : goldCarriedSlots) {
            if (goldToTake <= 0) {
                break;
            }
            ItemStack gold = playerInventory.getItem(slot);
            if (gold.getAmount() < goldToTake) {
                gold.setAmount(0);
                goldToTake -= gold.getAmount();
            } else {
                gold.setAmount(gold.getAmount() - goldToTake);
                goldToTake = 0;
            }
            playerInventory.setItem(slot, gold);
        }
        player.closeInventory();
        player.getItemInHand().addEnchantment(enchantment, level);
        player.updateInventory();
        player.sendMessage(ChatColor.DARK_PURPLE + player.getItemInHand().getType().name() + ChatColor.AQUA + " enchanted with "
                + ChatColor.BLUE + enchantmentName + ChatColor.AQUA + " for " + ChatColor.GOLD + ChatColor.BOLD + cost
                + ChatColor.AQUA + " gold");
    }

    private List<Enchantment> getPossibleEnchantments(Material material) {
        List<Enchantment> possibleEnchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.values()) {
            if (isBlacklistedEnchantment(enchantment)) continue;
            try {
                if (enchantment.getItemTarget().includes(material)) {
                    possibleEnchantments.add(enchantment);
                } else if (enchantment.equals(Enchantment.THORNS) && isArmour(material)) {
                    possibleEnchantments.add(enchantment);
                } else if (enchantment.equals(Enchantment.DAMAGE_ALL) && isAxe(material)) {
                    possibleEnchantments.add(enchantment);
                }
            } catch (NullPointerException exc) {
                possibleEnchantments.add(enchantment);
            }
        }
        return possibleEnchantments;
    }

    private void initializeDefaultEnchantments() {
        for (Enchantment enchantment : Enchantment.values()) {
            if (isBlacklistedEnchantment(enchantment)) continue;
            for (int level = enchantment.getStartLevel(); level <= enchantment.getMaxLevel(); level++) {
                getConfig().addDefault("enchantments." + enchantment.getName() + ".level." + level + ".gold", 5);
            }
        }
    }

    private boolean isAxe(Material material) {
        return material.equals(Material.WOOD_AXE) || material.equals(Material.STONE_AXE)
                || material.equals(Material.IRON_AXE) || material.equals(Material.GOLD_AXE)
                || material.equals(Material.DIAMOND_AXE);
    }

    private boolean isArmour(Material material) {
        return material.equals(Material.IRON_HELMET) || material.equals(Material.GOLD_HELMET)
                || material.equals(Material.LEATHER_HELMET) || material.equals(Material.CHAINMAIL_HELMET)
                || material.equals(Material.DIAMOND_HELMET)
                || material.equals(Material.IRON_CHESTPLATE) || material.equals(Material.GOLD_CHESTPLATE)
                || material.equals(Material.LEATHER_CHESTPLATE) || material.equals(Material.CHAINMAIL_CHESTPLATE)
                || material.equals(Material.DIAMOND_CHESTPLATE)
                || material.equals(Material.IRON_LEGGINGS) || material.equals(Material.GOLD_LEGGINGS)
                || material.equals(Material.LEATHER_LEGGINGS) || material.equals(Material.CHAINMAIL_LEGGINGS)
                || material.equals(Material.DIAMOND_LEGGINGS)
                || material.equals(Material.IRON_BOOTS) || material.equals(Material.GOLD_BOOTS)
                || material.equals(Material.LEATHER_BOOTS) || material.equals(Material.CHAINMAIL_BOOTS)
                || material.equals(Material.DIAMOND_BOOTS);
    }

    private boolean isBlacklistedEnchantment(Enchantment enchantment) {
        return enchantment.equals(Enchantment.DAMAGE_ARTHROPODS) || enchantment.equals(Enchantment.DAMAGE_UNDEAD)
                || enchantment.equals(Enchantment.PROTECTION_EXPLOSIONS) || enchantment.equals(Enchantment.LURE)
                || enchantment.equals(Enchantment.PROTECTION_FALL) || enchantment.equals(Enchantment.OXYGEN)
                || enchantment.equals(Enchantment.LOOT_BONUS_MOBS) || enchantment.equals(Enchantment.SILK_TOUCH)
                || enchantment.equals(Enchantment.WATER_WORKER) || enchantment.equals(Enchantment.DEPTH_STRIDER);
    }
}
