package com.mengcraft.playerSQL;

import com.comphenix.protocol.utility.StreamSerializer;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import com.google.common.collect.ImmutableMap;
import com.google.gson.*;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class PlayerData {
    private final Player player;
    private final String name;

    private int task = -1;

    public PlayerData(Player player) {
        String getVersion = player.getServer().getBukkitVersion().split("-")[0].substring(0, 3);
        double version = Double.parseDouble(getVersion);
        if (version > 1.6) this.name = player.getUniqueId().toString();
        else this.name = player.getName();
        this.player = player;
    }

    public String getName() {
        return name;
    }

    public void load() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String sql = "SELECT DATA FROM PlayerSQL " +
                            "WHERE NAME = ? FOR UPDATE;";
                    PreparedStatement statement = PlayerSQL.connection.prepareStatement(sql);
                    statement.setString(1, name);
                    ResultSet result = statement.executeQuery();
                    Boolean next = result.next();
                    if (next) {
                        final String data = result.getString(1);
                        JsonArray json = new JsonParser().parse(data).getAsJsonArray();
                        setPlayer(json);
                    } else setup();
                    statement.close();
                    result.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(PlayerSQL.plugin, 5);
    }

    public String getData() {
        return getJsonString();
    }

    public void save() {
        try {
            String sql = "UPDATE PlayerSQL " +
                    "SET DATA = ? " +
                    "WHERE NAME = ?;";
            PreparedStatement statement = PlayerSQL.connection.prepareStatement(sql);
            String data = getJsonString();
            statement.setString(1, data);
            statement.setString(2, name);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String getJsonString() {
        Gson gson = new Gson();
        JsonArray array = new JsonArray();
        double health = player.getHealth();
        int food = player.getFoodLevel();
        int exp = SetExpFix.getTotalExperience(player);
        ItemStack[] inventory = player.getInventory().getContents();
        ItemStack[] armor = player.getInventory().getArmorContents();
        ItemStack[] chest = player.getEnderChest().getContents();
        JsonElement[] elements = {
                gson.toJsonTree(health),
                gson.toJsonTree(food),
                gson.toJsonTree(exp),
                getStacksData(inventory),
                getStacksData(armor),
                getStacksData(chest),
                getPotions()
        };
        for (JsonElement element : elements) array.add(element);
        return gson.toJson(array);
    }

    public void startDaily() {
        if (task < 0) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    save();
                }
            }.runTaskTimer(PlayerSQL.plugin, 6000, 6000).getTaskId();
        }
    }

    public void stopDaily() {
        if (task > 0) {
            PlayerSQL.plugin.getServer().getScheduler().cancelTask(task);
            task = -1;
        }
    }

    private void setPlayer(JsonArray json) {
        setHealth(json);
        setFood(json);
        setExp(json);
        setInventory(json);
        setChest(json);
        setPotion(json);
    }

    private JsonArray getPotions() {
        Gson gson = new Gson();
        JsonArray array = new JsonArray();
        Collection<PotionEffect> active = player.getActivePotionEffects();
        if (active.size() > 0) {
            for (PotionEffect effect : active) {
                Map<String, Object> map = effect.serialize();
                JsonElement element = gson.toJsonTree(map);
                array.add(element);
            }
        }
        return array;
    }

    private void setPotion(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.potion", true);
        if (sync) {
            Collection<PotionEffect> active = player.getActivePotionEffects();
            for (PotionEffect effect : active) {
                PotionEffectType type = effect.getType();
                player.removePotionEffect(type);
            }
            JsonArray potion = array.get(6).getAsJsonArray();
            if (potion.size() > 0) {
                for (JsonElement element : potion) {
                    JsonObject object = element.getAsJsonObject();
                    Map<String, Object> map = ImmutableMap.<String, Object>of(
                            "effect", object.get("effect").getAsInt(),
                            "duration", object.get("duration").getAsInt(),
                            "amplifier", object.get("amplifier").getAsInt(),
                            "ambient", object.get("ambient").getAsBoolean()
                    );
                    player.addPotionEffect(new PotionEffect(map), true);
                }
            }
        }
    }

    private void setChest(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.enderChest", true);
        if (sync) {
            JsonArray chest = array.get(5).getAsJsonArray();
            ItemStack[] stacks = getStacks(chest);
            player.getEnderChest().setContents(stacks);
        }
    }

    private void setInventory(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.inventory", true);
        if (sync) {
            JsonArray inventory = array.get(3).getAsJsonArray();
            JsonArray armor = array.get(4).getAsJsonArray();
            ItemStack[] inventoryStacks = getStacks(inventory);
            ItemStack[] armorStacks = getStacks(armor);
            player.getInventory().setContents(inventoryStacks);
            player.getInventory().setArmorContents(armorStacks);
        }
    }


    private void setExp(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.exp", true);
        if (sync) {
            int exp = array.get(2).getAsInt();
            SetExpFix.setTotalExperience(player, exp);
        }
    }

    private void setFood(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.food", true);
        if (sync) {
            int food = array.get(1).getAsInt();
            player.setFoodLevel(food);
        }
    }

    private void setHealth(JsonArray array) {
        boolean sync = PlayerSQL.plugin.getConfig().getBoolean("sync.health", true);
        if (sync) {
            double health = array.get(0).getAsDouble();
            try {
                player.setHealth(health);
            } catch (IllegalArgumentException e) {
                health = player.getMaxHealth();
                player.setHealth(health);
            }
        }
    }

    private void setup() {
        try {
            String sql = "INSERT INTO PlayerSQL(NAME) " +
                    "VALUES(?);";
            PreparedStatement statement = PlayerSQL.connection.prepareStatement(sql);
            statement.setString(1, name);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ItemStack[] getStacks(JsonArray inventory) {
        StreamSerializer serializer = StreamSerializer.getDefault();
        List<ItemStack> stackList = new ArrayList<>();
        try {
            for (JsonElement element : inventory) {
                boolean isJsonNull = element.isJsonNull();
                if (isJsonNull) {
                    ItemStack stack = new ItemStack(Material.AIR);
                    stackList.add(stack);
                } else {
                    String base64 = element.getAsString();
                    ItemStack stack = serializer.deserializeItemStack(base64);
                    stackList.add(stack);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        int size = stackList.size();
        return stackList.toArray(new ItemStack[size]);
    }

    private JsonArray getStacksData(ItemStack[] stacks) {
        JsonArray array = new JsonArray();
        Gson gson = new Gson();
        StreamSerializer serializer = StreamSerializer.getDefault();
        try {
            for (ItemStack stack : stacks) {
                if (stack != null) {
                    String base64 = serializer.serializeItemStack(stack);
                    JsonElement json = gson.toJsonTree(base64);
                    array.add(json);
                } else {
                    JsonElement json = gson.toJsonTree(null);
                    array.add(json);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return array;
    }
}
