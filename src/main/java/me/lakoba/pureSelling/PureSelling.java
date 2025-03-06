package me.lakoba.pureSelling;

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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PureSelling extends JavaPlugin implements Listener {

    private final Map<Location, UUID> placedBins = new HashMap<>();
    private final Map<Location, Inventory> binInventories = new HashMap<>();
    private File configFile;
    private FileConfiguration dataConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadData();
        Bukkit.getPluginManager().registerEvents(this, this);
        startSellingTask();
    }

    @Override
    public void onDisable() {
        saveData();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlockPlaced(); // Změněno na block, který byl právě položen
        Material binMaterial = Material.valueOf(getConfig().getString("bin_block", "CHEST"));
        int customModelData = getConfig().getInt("custom_model_data", 0);
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        // Zkontrolujeme, jestli má položka v ruce CustomModelData
        if (itemInHand != null && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasCustomModelData()) {
            int itemCustomModelData = itemInHand.getItemMeta().getCustomModelData();

            // Pokud CustomModelData odpovídá, uložíme do mapy a provedeme další logiku
            if (itemCustomModelData == customModelData && block.getType() == binMaterial) {
                Location loc = block.getLocation();
                placedBins.put(loc, player.getUniqueId());
                binInventories.put(loc, Bukkit.createInventory(null, 27, "Selling Bin"));
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
        Material binMaterial = Material.valueOf(getConfig().getString("bin_block", "CHEST"));
        Integer customModelData = getConfig().getInt("custom_model_data", 0);

        if (block.getType() == binMaterial && placedBins.containsKey(loc)) {
            event.setCancelled(true);
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                player.sendMessage("Tento Selling Bin vlastní: " + Bukkit.getOfflinePlayer(placedBins.get(loc)).getName());
            } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                if (player.isSneaking()) {
                    block.setType(Material.AIR); // Zničí blok
                    player.sendMessage("Selling Bin byl zničen.");
                } else {
                    player.openInventory(binInventories.get(loc));
                }
            }
        }
    }


    private void startSellingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Map.Entry<Location, UUID> entry : placedBins.entrySet()) {
                    Location loc = entry.getKey();
                    UUID owner = entry.getValue();
                    Inventory bin = binInventories.get(loc);

                    if (bin == null) continue;
                    double totalValue = 0;

                    for (ItemStack item : bin.getContents()) {
                        if (item != null) {
                            String itemKey = item.getType().toString();
                            double price = getConfig().getDouble("item_prices." + itemKey, 0);
                            totalValue += price * item.getAmount();
                        }
                    }

                    bin.clear();
                    Player player = Bukkit.getPlayer(owner);
                    if (player != null) {
                        player.sendMessage("Prodáno za " + totalValue + "$!");
                    }
                }
            }
        }.runTaskTimer(this, 0, (getConfig().getLong("selling_time", 15) * 60) * 20);
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
