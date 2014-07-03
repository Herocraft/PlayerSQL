package com.mengcraft.playerSQL;

import com.comphenix.protocol.utility.StreamSerializer;
import com.earth2me.essentials.craftbukkit.SetExpFix;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.libs.com.google.gson.*;
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

public class PlayerData {
    private final Player player;
    private final String name;
    private int task = -1;
    private String data = null;

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

    public String getData() {
        return data;
    }

    public void update() {
        ConfigurationSection getConfig = PlayerSQL.plugin.getConfig().getConfigurationSection("sync");
        double health = getConfig.getBoolean("health", true) ? player.getHealth() : -1;
        int food = getConfig.getBoolean("food", true) ? player.getFoodLevel() : -1;
        int exp = getConfig.getBoolean("exp", true) ? player.getTotalExperience() : -1;
        String inventory = getConfig.getBoolean("inventory", true) ? getStacksData(player.getInventory().getContents()) : null;
        String armor = getConfig.getBoolean("inventory", true) ? getStacksData(player.getInventory().getArmorContents()) : null;
        String enderChest = getConfig.getBoolean("enderChest", true) ? getStacksData(player.getEnderChest().getContents()) : null;
        String potion = getConfig.getBoolean("potion", true) ? getPotions() : null;
        data = health + ";" + food + ";" + exp + ";" + inventory + ";" + armor + ";" + enderChest + ";" + potion;
    }

    public void load() {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    String sql = "SELECT DATA FROM PlayerSQL " +
                            "WHERE NAME = ? FOR UPDATE;";
                    PreparedStatement statement = PlayerSQL.database.prepareStatement(sql);
                    statement.setString(1, name);
                    ResultSet result = statement.executeQuery();
                    Boolean next = result.next();
                    if (next) {
                        final String data = result.getString(1);
                        setData(data);
                    } else setup();
                    statement.close();
                    result.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskLaterAsynchronously(PlayerSQL.plugin, 5);
    }

    public void save() {
        try {
            String sql = "UPDATE PlayerSQL " +
                    "SET DATA = ? " +
                    "WHERE NAME = ?;";
            PreparedStatement statement = PlayerSQL.database.prepareStatement(sql);
            statement.setString(1, data);
            statement.setString(2, name);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void startDaily() {
        if (task < 0) {
            this.task = new BukkitRunnable() {
                @Override
                public void run() {
                    update();
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

    private void setup() {
        try {
            String sql = "INSERT INTO PlayerSQL(NAME) " +
                    "VALUES(?);";
            PreparedStatement statement = PlayerSQL.database.prepareStatement(sql);
            statement.setString(1, name);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setData(String data) {
        String[] values = data.split(";");
        double health = Double.parseDouble(values[0]);
        int food = Integer.parseInt(values[1]);
        int exp = Integer.parseInt(values[2]);
        if (health != -1) player.setHealth(health);
        if (food != -1) player.setFoodLevel(food);
        if (exp != -1) SetExpFix.setTotalExperience(player, exp);
        if (values[3].length() > 4) {
            ItemStack[] stacks = getStacks(values[3]);
            player.getInventory().setContents(stacks);
        }
        if (values[4].length() > 4) {
            ItemStack[] stacks = getStacks(values[4]);
            player.getInventory().setArmorContents(stacks);
        }
        if (values[5].length() > 4) {
            ItemStack[] stacks = getStacks(values[5]);
            player.getEnderChest().setContents(stacks);
        }
        if (values[6].length() > 4) setPotions(values[6]);
    }

    private void setPotions(String data) {
        JsonArray array = new Gson().fromJson(data, JsonElement.class).getAsJsonArray();
        Collection<PotionEffect> activeEffects = player.getActivePotionEffects();
        for (PotionEffect activeEffect : activeEffects) {
            PotionEffectType getType = activeEffect.getType();
            player.removePotionEffect(getType);
        }
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            String type = object.get("type").getAsString();
            int durability = object.get("durability").getAsInt();
            int amplifier = object.get("amplifier").getAsInt();
            player.addPotionEffect(new PotionEffect(PotionEffectType.getByName(type), durability, amplifier), true);
        }
    }

    private String getPotions() {
        JsonArray array = new JsonArray();
        Gson gson = new Gson();
        Collection<PotionEffect> effects = player.getActivePotionEffects();
        for (PotionEffect effect : effects) {
            JsonObject object = new JsonObject();
            String type = effect.getType().getName();
            int durability = effect.getDuration();
            int amplifier = effect.getAmplifier();
            object.add("type", gson.toJsonTree(type));
            object.add("durability", gson.toJsonTree(durability));
            object.add("amplifier", gson.toJsonTree(amplifier));
            array.add(object);
        }
        return array.toString();
    }

    private ItemStack[] getStacks(String data) {
        List<ItemStack> stackList = new ArrayList<>();
        StreamSerializer serializer = StreamSerializer.getDefault();
        JsonArray array = new Gson().fromJson(data, JsonElement.class).getAsJsonArray();
        try {
            for (JsonElement element : array) {
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
        ItemStack[] stacks = {};
        return stackList.toArray(stacks);
    }

    private String getStacksData(ItemStack[] stacks) {
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
        return array.toString();
    }
}
