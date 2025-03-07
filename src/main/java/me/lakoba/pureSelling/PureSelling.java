package me.lakoba.pureSelling;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class PureSelling extends JavaPlugin implements Listener {

    private final Map<Location, UUID> placedBins = new HashMap<>();
    private final Map<Location, Inventory> binInventories = new HashMap<>();
    private final Map<Location, Hologram> placedHolograms = new HashMap<>();
    private File configFile;
    private FileConfiguration dataConfig;

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
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (itemInHand != null && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasCustomModelData()) {
            int itemCustomModelData = itemInHand.getItemMeta().getCustomModelData();

            if (itemCustomModelData == customModelData) {
                Location loc = block.getLocation();
                Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
                placedBins.put(loc, player.getUniqueId());
                binInventories.put(loc, Bukkit.createInventory(null, 27, "Selling Bin"));
                Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
                DHAPI.addHologramLine(hologram, "Čas do prodeje: %pureselling_time%");
                placedHolograms.put(holoLocation, hologram);
                player.sendMessage("Umístil jsi Selling Bin!");
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Location loc = block.getLocation();

        if (placedBins.containsKey(loc)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.sendMessage("Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(placedBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (!player.isSneaking()) {
                    event.setCancelled(true);
                    player.openInventory(binInventories.get(loc));
                }
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
            event.getPlayer().sendMessage("Selling bin byl zničen!");
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
                                player.sendMessage("Položky byly prodány! A odměnu máš v Selling Binu");
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

        if (dataConfig.getConfigurationSection("bins") == null) return;

        for (String key : dataConfig.getConfigurationSection("bins").getKeys(false)) {
            Location loc = stringToLocation(key);
            UUID owner = UUID.fromString(dataConfig.getString("bins." + key + ".owner"));
            placedBins.put(loc, owner);
            Location holoLocation = loc.clone().add(0.5, 1.5, 0.5);
            Hologram hologram = DHAPI.createHologram(UUID.randomUUID().toString(), holoLocation);
            DHAPI.addHologramLine(hologram, "Čas do prodeje: %pureselling_time%");
            placedHolograms.put(holoLocation, hologram);
            binInventories.put(loc, Bukkit.createInventory(null, 27, "Selling Bin"));
        }
    }

    private void saveData() {
        dataConfig.set("bins", null);
        for (Map.Entry<Location, UUID> entry : placedBins.entrySet()) {
            String key = locationToString(entry.getKey());
            dataConfig.set("bins." + key + ".owner", entry.getValue().toString());
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
