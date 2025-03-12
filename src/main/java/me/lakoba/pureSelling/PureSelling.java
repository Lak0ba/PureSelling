package me.lakoba.pureSelling;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class PureSelling extends JavaPlugin implements Listener {

    private final Map<Location, UUID> placedBins = new HashMap<>();
    private final Map<Location, UUID> ironBins = new HashMap<>();
    private final Map<Location, UUID> diamondBins = new HashMap<>();
    private final Map<Location, UUID> redstoneBins = new HashMap<>();
    private final Map<Location, UUID> netheriteBins = new HashMap<>();
    private final Map<Location, Inventory> binInventories = new HashMap<>();
    private final Map<Location, Inventory> ironBinInventories = new HashMap<>();
    private final Map<Location, Inventory> diamondBinInventories = new HashMap<>();
    private final Map<Location, Inventory> redstoneBinInventories = new HashMap<>();
    private final Map<Location, Inventory> netheriteBinInventories = new HashMap<>();
    private final Map<Location, Hologram> placedHolograms = new HashMap<>();
    private File configFile;
    private FileConfiguration dataConfig;

    private String prefix = ChatColor.translateAlternateColorCodes('&', "&7[&bPureSelling&7] &f");

    public Integer time = getConfig().getInt("selling_time", 15) * 60;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);
        startSellingTask();
        new PlaceHolderApiHandler(this).register();
    }

    @Override
    public void onDisable() {
        saveData();
        for (Hologram hologram : placedHolograms.values()) {
            hologram.delete();
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced();
        int customModelData = getConfig().getInt("custom_model_data", 0);
        getLogger().warning("Custom model data: " + customModelData);
        int ironCustomModelData = getConfig().getInt("iron_custom_model_data", 0);
        getLogger().warning("Iron custom model data: " + ironCustomModelData);
        int diamondCustomModelData = getConfig().getInt("diamond_custom_model_data", 0);
        getLogger().warning("Diamond custom model data: " + diamondCustomModelData);
        int redstoneCustomModelData = getConfig().getInt("redstone_custom_model_data", 0);
        getLogger().warning("Redstone custom model data: " + redstoneCustomModelData);
        int netheriteCustomModelData = getConfig().getInt("netherite_custom_model_data", 0);
        getLogger().warning("Netherite custom model data: " + netheriteCustomModelData);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasCustomModelData()) {
            int itemCustomModelData = itemInHand.getItemMeta().getCustomModelData();

            getLogger().warning("Item custom model data: " + itemCustomModelData);

            if (itemCustomModelData == customModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                placedBins.put(loc, player.getUniqueId());
                binInventories.put(loc, Bukkit.createInventory(null, 9 * 1, "Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, ChatColor.of("#905300") + "Wood level | " + getConfig().getDouble("sell_multiplier", 0) + "x prodej");
                DHAPI.addHologramLine(hologram, ChatColor.of("#905300") + "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage(prefix + "Umístil jsi " + ChatColor.of("#905300") + "Wood" + ChatColor.WHITE + " Selling Bin!");
            } else if (itemCustomModelData == ironCustomModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                ironBins.put(loc, player.getUniqueId());
                ironBinInventories.put(loc, Bukkit.createInventory(null, 9 * 2, "Iron Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, ChatColor.of("#cdcdcd") + "Iron level | " + getConfig().getDouble("iron_sell_multiplier", 0) + "x prodej");
                DHAPI.addHologramLine(hologram, ChatColor.of("#cdcdcd") + "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage(prefix + "Umístil jsi " + ChatColor.of("#cdcdcd") + "Iron" + ChatColor.WHITE + " Selling Bin!");
            } else if (itemCustomModelData == diamondCustomModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                diamondBins.put(loc, player.getUniqueId());
                diamondBinInventories.put(loc, Bukkit.createInventory(null, 9 * 4, "Diamond Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, ChatColor.of("#00ffcd") + "Diamond level | " + getConfig().getDouble("diamond_sell_multiplier", 0) + "x prodej");
                DHAPI.addHologramLine(hologram, ChatColor.of("#00ffcd") + "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage(prefix + "Umístil jsi " + ChatColor.of("#00ffcd") + "Diamond" + ChatColor.WHITE + " Selling Bin!");
            } else if (itemCustomModelData == redstoneCustomModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                redstoneBins.put(loc, player.getUniqueId());
                redstoneBinInventories.put(loc, Bukkit.createInventory(null, 9 * 3, "Redstone Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, ChatColor.of("#d10000") + "Redstone level | " + getConfig().getDouble("redstone_sell_multiplier", 0) + "x prodej");
                DHAPI.addHologramLine(hologram, ChatColor.of("#d10000") + "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage(prefix + "Umístil jsi " + ChatColor.of("#d10000") + "Redstone" + ChatColor.WHITE + " Selling Bin!");
            } else if (itemCustomModelData == netheriteCustomModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                netheriteBins.put(loc, player.getUniqueId());
                netheriteBinInventories.put(loc, Bukkit.createInventory(null, 9 * 5, "Netherite Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, ChatColor.of("#484848") + "Netherite level | " + getConfig().getDouble("netherite_sell_multiplier", 0) + "x prodej");
                DHAPI.addHologramLine(hologram, ChatColor.of("#484848") + "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage(prefix + "Umístil jsi " + ChatColor.of("#484848") + "Netherite" + ChatColor.WHITE + " Selling Bin!");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location loc = block.getLocation();

        // Zkontroluje, zda je bin umístěn a upraví chování pro pravý/levý klik
        if (placedBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(prefix + "Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(placedBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(binInventories.get(loc));
            }
        } else if (ironBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(prefix + "Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(ironBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(ironBinInventories.get(loc));
            }
        } else if (diamondBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(prefix + "Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(diamondBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(diamondBinInventories.get(loc));
            }
        } else if (redstoneBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(prefix + "Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(redstoneBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(redstoneBinInventories.get(loc));
            }
        } else if (netheriteBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage(prefix + "Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(netheriteBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK && !player.isSneaking()) {
                event.setCancelled(true);
                player.openInventory(netheriteBinInventories.get(loc));
            }
        }
    }


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        Location loc = block.getLocation();
        Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);

        if (placedBins.containsKey(loc)) {
            Inventory bin = binInventories.get(loc);
            for (ItemStack item : bin.getContents()) {
                if (item != null && item.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            binInventories.remove(loc);
            placedBins.remove(loc);
            placedHolograms.get(holoLocation).delete();
            placedHolograms.remove(holoLocation);
            event.getPlayer().sendMessage(prefix + "Selling bin byl zničen!");
        } else if (ironBins.containsKey(loc)) {
            Inventory bin = ironBinInventories.get(loc);
            for (ItemStack item : bin.getContents()) {
                if (item != null && item.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            ironBinInventories.remove(loc);
            ironBins.remove(loc);
            placedBins.remove(loc);
            placedHolograms.get(holoLocation).delete();
            placedHolograms.remove(holoLocation);
            event.getPlayer().sendMessage(prefix + "Selling bin byl zničen!");
        } else if (diamondBins.containsKey(loc)) {
            Inventory bin = diamondBinInventories.get(loc);
            for (ItemStack item : bin.getContents()) {
                if (item != null && item.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            diamondBinInventories.remove(loc);
            diamondBins.remove(loc);
            placedHolograms.get(holoLocation).delete();
            placedHolograms.remove(holoLocation);
            event.getPlayer().sendMessage(prefix + "Selling bin byl zničen!");
        } else if (redstoneBins.containsKey(loc)) {
            Inventory bin = redstoneBinInventories.get(loc);
            for (ItemStack item : bin.getContents()) {
                if (item != null && item.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            redstoneBinInventories.remove(loc);
            redstoneBins.remove(loc);
            placedHolograms.get(holoLocation).delete();
            placedHolograms.remove(holoLocation);
            event.getPlayer().sendMessage(prefix + "Selling bin byl zničen!");
        } else if (netheriteBins.containsKey(loc)) {
            Inventory bin = netheriteBinInventories.get(loc);
            for (ItemStack item : bin.getContents()) {
                if (item != null && item.getAmount() > 0) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
            }
            netheriteBinInventories.remove(loc);
            netheriteBins.remove(loc);
            placedHolograms.get(holoLocation).delete();
            placedHolograms.remove(holoLocation);
            event.getPlayer().sendMessage(prefix + "Selling bin byl zničen!");
        }
    }

    private void startSellingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (time > 0) {
                    time--;
                } else {
                    for (Map.Entry<Location, UUID> entry : placedBins.entrySet()) {
                        Location loc = entry.getKey();
                        UUID owner = entry.getValue();
                        Inventory bin = binInventories.get(loc);
                        List<ItemStack> rewards = new ArrayList<>();
                        boolean itemsSold = false;

                        if (bin == null) continue;

                        ItemStack[] binContents = bin.getContents().clone();

                        for (ItemStack item : binContents) {
                            if (item != null && item.getAmount() > 0) {
                                String itemKey = item.getType().toString();

                                if (!getConfig().contains("item_prices." + itemKey)) {
                                    continue;
                                }

                                int requiredAmount = getConfig().getInt("item_prices." + itemKey + ".amount", 0);
                                int rewardAmount = getConfig().getInt("item_prices." + itemKey + ".reward.amount", 0);

                                if (item.getAmount() < requiredAmount) {
                                    continue;
                                }

                                while (item.getAmount() >= requiredAmount) {
                                    item.setAmount(item.getAmount() - requiredAmount);
                                    ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("item_prices." + itemKey + ".reward.item", "AIR")), rewardAmount);
                                    rewards.add(reward);
                                    itemsSold = true;
                                }
                            }
                        }

                        if (itemsSold) {
                            bin.clear();
                            for (ItemStack reward : rewards) {
                                bin.addItem(reward);
                            }

                            for (ItemStack item : binContents) {
                                if (item != null && item.getAmount() > 0) {
                                    bin.addItem(item);
                                }
                            }

                            Player player = Bukkit.getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(prefix + "Položky byly prodány! A odměnu máš v Selling Binu");
                            }
                        }

                    }
                    for (Map.Entry<Location, UUID> entry : ironBins.entrySet()) {
                        Location loc = entry.getKey();
                        UUID owner = entry.getValue();
                        Inventory bin = ironBinInventories.get(loc);
                        List<ItemStack> rewards = new ArrayList<>();
                        boolean itemsSold = false;

                        if (bin == null) continue;

                        ItemStack[] binContents = bin.getContents().clone();

                        for (ItemStack item : binContents) {
                            if (item != null && item.getAmount() > 0) {
                                String itemKey = item.getType().toString();

                                if (!getConfig().contains("item_prices." + itemKey)) {
                                    continue;
                                }

                                int requiredAmount = getConfig().getInt("item_prices." + itemKey + ".amount", 0);
                                int rewardAmount = getConfig().getInt("item_prices." + itemKey + ".reward.amount", 0);

                                if (item.getAmount() < requiredAmount) {
                                    continue;
                                }

                                while (item.getAmount() >= requiredAmount) {
                                    item.setAmount(item.getAmount() - requiredAmount);
                                    ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("item_prices." + itemKey + ".reward.item", "AIR")), (int) (rewardAmount * getConfig().getDouble("iron_sell_multiplier", 0)));
                                    rewards.add(reward);
                                    itemsSold = true;
                                }
                            }
                        }

                        if (itemsSold) {
                            bin.clear();
                            for (ItemStack reward : rewards) {
                                bin.addItem(reward);
                            }

                            for (ItemStack item : binContents) {
                                if (item != null && item.getAmount() > 0) {
                                    bin.addItem(item);
                                }
                            }

                            Player player = Bukkit.getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(prefix + "Položky byly prodány! A odměnu máš v Selling Binu");
                            }
                        }

                    }
                    for (Map.Entry<Location, UUID> entry : diamondBins.entrySet()) {
                        Location loc = entry.getKey();
                        UUID owner = entry.getValue();
                        Inventory bin = diamondBinInventories.get(loc);
                        List<ItemStack> rewards = new ArrayList<>();
                        boolean itemsSold = false;

                        if (bin == null) continue;

                        ItemStack[] binContents = bin.getContents().clone();

                        for (ItemStack item : binContents) {
                            if (item != null && item.getAmount() > 0) {
                                String itemKey = item.getType().toString();

                                if (!getConfig().contains("item_prices." + itemKey)) {
                                    continue;
                                }

                                int requiredAmount = getConfig().getInt("item_prices." + itemKey + ".amount", 0);
                                int rewardAmount = getConfig().getInt("item_prices." + itemKey + ".reward.amount", 0);

                                if (item.getAmount() < requiredAmount) {
                                    continue;
                                }

                                while (item.getAmount() >= requiredAmount) {
                                    item.setAmount(item.getAmount() - requiredAmount);
                                    ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("item_prices." + itemKey + ".reward.item", "AIR")), (int) (rewardAmount * getConfig().getDouble("diamond_sell_multiplier", 0)));
                                    rewards.add(reward);
                                    itemsSold = true;
                                }
                            }
                        }

                        if (itemsSold) {
                            bin.clear();
                            for (ItemStack reward : rewards) {
                                bin.addItem(reward);
                            }

                            for (ItemStack item : binContents) {
                                if (item != null && item.getAmount() > 0) {
                                    bin.addItem(item);
                                }
                            }

                            Player player = Bukkit.getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(prefix + "Položky byly prodány! A odměnu máš v Selling Binu");
                            }
                        }

                    }
                    for (Map.Entry<Location, UUID> entry : redstoneBins.entrySet()) {
                        Location loc = entry.getKey();
                        UUID owner = entry.getValue();
                        Inventory bin = redstoneBinInventories.get(loc);
                        List<ItemStack> rewards = new ArrayList<>();
                        boolean itemsSold = false;

                        if (bin == null) continue;

                        ItemStack[] binContents = bin.getContents().clone();

                        for (ItemStack item : binContents) {
                            if (item != null && item.getAmount() > 0) {
                                String itemKey = item.getType().toString();

                                if (!getConfig().contains("item_prices." + itemKey)) {
                                    continue;
                                }

                                int requiredAmount = getConfig().getInt("item_prices." + itemKey + ".amount", 0);
                                int rewardAmount = getConfig().getInt("item_prices." + itemKey + ".reward.amount", 0);

                                if (item.getAmount() < requiredAmount) {
                                    continue;
                                }

                                while (item.getAmount() >= requiredAmount) {
                                    item.setAmount(item.getAmount() - requiredAmount);
                                    ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("item_prices." + itemKey + ".reward.item", "AIR")), (int) (rewardAmount * getConfig().getDouble("redstone_sell_multiplier", 0)));
                                    rewards.add(reward);
                                    itemsSold = true;
                                }
                            }
                        }

                        if (itemsSold) {
                            bin.clear();
                            for (ItemStack reward : rewards) {
                                bin.addItem(reward);
                            }

                            for (ItemStack item : binContents) {
                                if (item != null && item.getAmount() > 0) {
                                    bin.addItem(item);
                                }
                            }

                            Player player = Bukkit.getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(prefix + "Položky byly prodány! A odměnu máš v Selling Binu");
                            }
                        }

                    }
                    for (Map.Entry<Location, UUID> entry : netheriteBins.entrySet()) {
                        Location loc = entry.getKey();
                        UUID owner = entry.getValue();
                        Inventory bin = netheriteBinInventories.get(loc);
                        List<ItemStack> rewards = new ArrayList<>();
                        boolean itemsSold = false;

                        if (bin == null) continue;

                        ItemStack[] binContents = bin.getContents().clone();

                        for (ItemStack item : binContents) {
                            if (item != null && item.getAmount() > 0) {
                                String itemKey = item.getType().toString();

                                if (!getConfig().contains("item_prices." + itemKey)) {
                                    continue;
                                }

                                int requiredAmount = getConfig().getInt("item_prices." + itemKey + ".amount", 0);
                                int rewardAmount = getConfig().getInt("item_prices." + itemKey + ".reward.amount", 0);

                                if (item.getAmount() < requiredAmount) {
                                    continue;
                                }

                                while (item.getAmount() >= requiredAmount) {
                                    item.setAmount(item.getAmount() - requiredAmount);
                                    ItemStack reward = new ItemStack(Material.valueOf(getConfig().getString("item_prices." + itemKey + ".reward.item", "AIR")), (int) (rewardAmount * getConfig().getDouble("netherite_sell_multiplier", 0)));
                                    rewards.add(reward);
                                    itemsSold = true;
                                }
                            }
                        }

                        if (itemsSold) {
                            bin.clear();
                            for (ItemStack reward : rewards) {
                                bin.addItem(reward);
                            }

                            for (ItemStack item : binContents) {
                                if (item != null && item.getAmount() > 0) {
                                    bin.addItem(item);
                                }
                            }

                            Player player = Bukkit.getPlayer(owner);
                            if (player != null) {
                                player.sendMessage(prefix + "Položky byly prodány! A odměnu máš v Selling Binu");
                            }
                        }

                    }
                    time = getConfig().getInt("selling_time", 15) * 60;
                }
            }
        }.runTaskTimer(this, 0, 20);
    }

    private void loadData() {
        configFile = new File(getDataFolder(), "data.yml");
        if (!configFile.exists()) {
            saveResource("data.yml", false);
        }
        dataConfig = YamlConfiguration.loadConfiguration(configFile);

        if (    dataConfig.getConfigurationSection("bins") == null &&
                dataConfig.getConfigurationSection("iron_bins") == null &&
                dataConfig.getConfigurationSection("redstone_bins") == null &&
                dataConfig.getConfigurationSection("diamond_bins") == null &&
                dataConfig.getConfigurationSection("netherite_bins") == null
            ) return;

        for (String key : dataConfig.getConfigurationSection("bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("bins." + key + ".owner"));
            placedBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, ChatColor.of("#905300") + "Wood level | " + getConfig().getDouble("sell_multiplier", 0) + "x prodej");
            DHAPI.addHologramLine(hologram, ChatColor.of("#905300") + "Čas do prodeje: %pureselling_time%");
            placedHolograms.put(holoLocation, hologram);
            binInventories.put(loc, Bukkit.createInventory(null, 9 * 1, "Selling Bin"));
        }

        for (String key : dataConfig.getConfigurationSection("iron_bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("iron_bins." + key + ".owner"));
            ironBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, ChatColor.of("#cdcdcd") + "Iron level | " + getConfig().getDouble("iron_sell_multiplier", 0) + "x prodej");
            DHAPI.addHologramLine(hologram, ChatColor.of("#cdcdcd") + "Čas do prodeje: %pureselling_time%");
            placedHolograms.put(holoLocation, hologram);
            ironBinInventories.put(loc, Bukkit.createInventory(null, 9 * 2, "Iron Selling Bin"));
        }

        for (String key : dataConfig.getConfigurationSection("redstone_bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("redstone_bins." + key + ".owner"));
            redstoneBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, ChatColor.of("#d10000") + "Redstone level | " + getConfig().getDouble("redstone_sell_multiplier", 0) + "x prodej");
            DHAPI.addHologramLine(hologram, ChatColor.of("#d10000") + "Čas do prodeje: %pureselling_time%");
            redstoneBinInventories.put(loc, Bukkit.createInventory(null, 9 * 3, "Redstone Selling Bin"));
        }

        for (String key : dataConfig.getConfigurationSection("diamond_bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("diamond_bins." + key + ".owner"));
            diamondBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, ChatColor.of("#00ffcd") + "Diamond level | " + getConfig().getDouble("diamond_sell_multiplier", 0) + "x prodej");
            DHAPI.addHologramLine(hologram, ChatColor.of("#00ffcd") + "Čas do prodeje: %pureselling_time%");
            placedHolograms.put(holoLocation, hologram);
            diamondBinInventories.put(loc, Bukkit.createInventory(null, 9 * 4, "Diamond Selling Bin"));
        }

        for (String key : dataConfig.getConfigurationSection("netherite_bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("netherite_bins." + key + ".owner"));
            netheriteBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, ChatColor.of("#484848") + "Netherite level | " + getConfig().getDouble("netherite_sell_multiplier", 0) + "x prodej");
            DHAPI.addHologramLine(hologram, ChatColor.of("#484848") + "Čas do prodeje: %pureselling_time%");
            placedHolograms.put(holoLocation, hologram);
            netheriteBinInventories.put(loc, Bukkit.createInventory(null, 9 * 5, "Netherite Selling Bin"));
        }
    }

    private void saveData() {
        dataConfig.set("bins", null);
        for (Map.Entry<Location, UUID> entry : placedBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("bins." + key + ".owner", entry.getValue().toString());
        }
        dataConfig.set("iron_bins", null);
        for (Map.Entry<Location, UUID> entry : ironBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("iron_bins." + key + ".owner", entry.getValue().toString());
        }
        dataConfig.set("redstone_bins", null);
        for (Map.Entry<Location, UUID> entry : redstoneBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("redstone_bins." + key + ".owner", entry.getValue().toString());
        }
        dataConfig.set("diamond_bins", null);
        for (Map.Entry<Location, UUID> entry : diamondBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("diamond_bins." + key + ".owner", entry.getValue().toString());
        }
        dataConfig.set("netherite_bins", null);
        for (Map.Entry<Location, UUID> entry : netheriteBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("netherite_bins." + key + ".owner", entry.getValue().toString());
        }
        try {
            dataConfig.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String locationToString(Location loc) {
        return loc.getWorld().getName() + "," + loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ();
    }

    private Location stringToLocation(String str) {
        String[] parts = str.split(",");
        return new Location(Bukkit.getWorld(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
    }
}
