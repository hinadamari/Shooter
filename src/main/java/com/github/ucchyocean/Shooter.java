/*
 * Copyright ucchy 2013
 */
package com.github.ucchyocean;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author ucchy
 * シューター
 */
public class Shooter extends JavaPlugin implements Listener {

    private static final String NAME = "フックショット";
    private static final String DISPLAY_NAME =
            ChatColor.WHITE + NAME;

    private static final int MAX_LEVEL = 3;
    private static final int DEFAULT_LEVEL = 2;
    private static final int RANGE = 64;

    private ItemStack item;

    /**
     * プラグインが有効になったときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
     */
    public void onEnable(){

        getServer().getPluginManager().registerEvents(this, this);

        item = new ItemStack(Material.FISHING_ROD, 1);
        ItemMeta shooterMeta = item.getItemMeta();
        shooterMeta.setDisplayName(DISPLAY_NAME);
        item.setItemMeta(shooterMeta);

        // レシピ追加
        ShapedRecipe recipe1 = new ShapedRecipe(getShooter(1));
        recipe1.shape("  B", " BC", "B C");
        recipe1.setIngredient('B', Material.STICK);
        recipe1.setIngredient('C', Material.GOLD_INGOT);
        getServer().addRecipe(recipe1);

        ShapedRecipe recipe = new ShapedRecipe(getShooter(2));
        recipe.shape("  B", " BC", "B C");
        recipe.setIngredient('B', Material.STICK);
        recipe.setIngredient('C', Material.NETHER_BRICK_ITEM);
        getServer().addRecipe(recipe);

        ShapedRecipe recipe3 = new ShapedRecipe(getShooter(3));
        recipe3.shape("  B", " BC", "B C");
        recipe3.setIngredient('B', Material.STICK);
        recipe3.setIngredient('C', Material.IRON_INGOT);
        getServer().addRecipe(recipe3);
    }

    /**
     * コマンドが実行されたときに呼び出されるメソッド
     * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command,
            String label, String[] args) {

        if ( args.length <= 0 ) {
            return false;
        }

        if ( args[0].equalsIgnoreCase("get") ) {

            if ( !(sender instanceof Player) ) {
                sender.sendMessage(ChatColor.RED + "This command can only use in game.");
                return true;
            }

            Player player = (Player)sender;

            int level = DEFAULT_LEVEL;
            if ( args.length >= 2 && args[1].matches("^[0-9]+$") ) {
                level = Integer.parseInt(args[1]);
            }

            giveShooter(player, level);

            return true;

        } else if ( args.length >= 2 && args[0].equalsIgnoreCase("give") ) {

            Player player = getServer().getPlayerExact(args[1]);
            if ( player == null ) {
                sender.sendMessage(ChatColor.RED + "Player " + args[1] + " was not found.");
                return true;
            }

            int level = DEFAULT_LEVEL;
            if ( args.length >= 3 && args[2].matches("^[0-9]+$") ) {
                level = Integer.parseInt(args[2]);
            }

            giveShooter(player, level);

            return true;
        }

        return false;
    }

    /**
     * 指定したプレイヤーに、指定したレベルのShooterを与える
     * @param player プレイヤー
     * @param level レベル
     */
    private void giveShooter(Player player, int level) {

        if ( level < 1 ) {
            level = 1;
        } else if ( level > MAX_LEVEL ) {
            level = MAX_LEVEL;
        }

        player.getInventory().addItem(getShooter(level));
    }

    /**
     * Shooterの取得
     * @param level
     * @return
     */
    private ItemStack getShooter(int level) {

    	ItemStack shooter = this.item.clone();

    	ItemMeta shooterMeta = shooter.getItemMeta();
    	ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.BLUE + "Level: " + ChatColor.WHITE +  level);
        shooterMeta.setLore(lore);
        shooter.setItemMeta(shooterMeta);

        return shooter;

    }

    /**
     * クリックされたときのイベント処理
     * @param event
     */
    @EventHandler(priority= EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getItemInHand() == null ||
            player.getItemInHand().getType() == Material.AIR ||
            !player.getItemInHand().getItemMeta().hasDisplayName() ||
            !player.getItemInHand().getItemMeta().getDisplayName().equals(DISPLAY_NAME)) {
            return;
        }

        if (event.getAction() == Action.PHYSICAL) {
        	return;
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR ||
            event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
        	event.setCancelled(true);
            return;
        }

        if (player.getTargetBlock(null, RANGE).getType() == Material.AIR) {
            player.sendMessage(ChatColor.RED + "距離が遠すぎます!!");
            return;
        }

        event.setCancelled(true);

        // レベルを取得
        ItemStack shooter = player.getItemInHand();
        double level = 1;
        for (String lore : shooter.getItemMeta().getLore()) {
        	String[] lores = ChatColor.stripColor(lore).split(" ");
        	if (lores[0].equals("Level:")) level = Double.valueOf(lores[1]);
        }

        // 耐久値を消費
        if (player.getGameMode() != GameMode.CREATIVE) {
			if (player.getItemInHand().getDurability() + 1 >= 65) {
				player.setItemInHand(new ItemStack(Material.AIR, 1));
				player.playSound(player.getLocation(), Sound.ITEM_BREAK, 1, 1);
			} else {
				player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability() + 1));
			}
		}

        // 飛翔
        player.setVelocity(player.getLocation().getDirection().multiply(level + 1));
        player.setFallDistance(-1000F);
        player.playSound(player.getLocation(), Sound.GHAST_FIREBALL, 1, 1);

        event.setCancelled(true);
    }


}
