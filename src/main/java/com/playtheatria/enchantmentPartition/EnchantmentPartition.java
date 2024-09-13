package com.playtheatria.enchantmentPartition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;

public final class EnchantmentPartition extends JavaPlugin implements CommandExecutor {

    private final Component NEED_ENCHANTED_BOOK_MESSAGE = Component.text("You must be holding a an enchanted book with more than one enchantment to use this command!").color(NamedTextColor.DARK_RED);
    private final Component NEED_MORE_THAN_ONE_ENCHANT_MESSAGE = Component.text("Your enchanted book needs more than one enchantment to use this command!").color(NamedTextColor.DARK_RED);


    @Override
    public void onEnable() {
        // Plugin startup logic
        Objects.requireNonNull(getCommand("partition")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            getLogger().warning("This command is meant for players holding a book with enchants!");
            return false;
        }

        if (player.getInventory().getItemInMainHand().getType() != Material.ENCHANTED_BOOK) {
            player.sendMessage(NEED_ENCHANTED_BOOK_MESSAGE);
            return true;
        }

        ItemStack enchantedBook = player.getInventory().getItemInMainHand();
        ItemMeta meta = enchantedBook.getItemMeta();

        if (meta == null) {
            player.sendMessage(Component.text("null metadata"));
            return true;
        }

        if (meta instanceof EnchantmentStorageMeta enchantmentStorageMeta) {

            if (enchantmentStorageMeta.getStoredEnchants().entrySet().size() <= 1) {
                player.sendMessage(NEED_MORE_THAN_ONE_ENCHANT_MESSAGE);
                return true;
            }
            player.getInventory().remove(enchantedBook);
            for (Map.Entry<Enchantment, Integer> enchantment : enchantmentStorageMeta.getStoredEnchants().entrySet()) {
                Repairable repairable = (Repairable) enchantmentStorageMeta;
                player.getWorld().dropItem(player.getLocation(), getEnchantedBook(enchantment.getKey(), enchantment.getValue(), repairable.getRepairCost()));
            }
            player.sendMessage("You are holding an enchanted book with more than one enchantment!");
            return true;
        }

        return false;
    }

    private ItemStack getEnchantedBook(Enchantment enchantment, Integer enchantmentLevel, Integer repairCost) {
        // initialize book
        ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
        ItemMeta meta = itemStack.getItemMeta();

        // convert to enchantment meta
        EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
        enchantmentStorageMeta.addStoredEnchant(enchantment, enchantmentLevel, false);

        // convert to repairable meta
        Repairable repairable = (Repairable) enchantmentStorageMeta;
        repairable.setRepairCost(repairCost);

        // save the enchantment meta
        itemStack.setItemMeta(repairable);

        return itemStack;
    }
}
