package me.mohsumzadah.stackitems;

import com.google.common.base.CharMatcher;
import me.mohsumzadah.stackitems.CooldownController.CoolDown;
import me.mohsumzadah.stackitems.CooldownController.ItemNameCorrector;
import me.mohsumzadah.stackitems.Listeners.ItemsListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class StackItems extends JavaPlugin {

    public static StackItems plugin;
    ItemNameCorrector itemNameCorrector;
    CoolDown coolDown;

    @Override
    public void onEnable() {
        plugin = this;

        createConfigF();

        itemNameCorrector = new ItemNameCorrector(plugin);
        coolDown = new CoolDown(plugin);

        getCommand("itemstack").setExecutor(plugin);
        getServer().getPluginManager().registerEvents(new ItemsListener(), this);

    }

    @Override
    public void onDisable() {

        removeAllData();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("itemstack")) {
            if (sender instanceof Player) {
                Player player = ((Player) sender).getPlayer();
                if (args[0].equalsIgnoreCase("reload")) {
                    if (player.hasPermission("itemstack.reload") ||
                            player.hasPermission("itemstack.*")) {
                        removeAllData();
                        plugin.getServer().getPluginManager().disablePlugin(plugin);
                        plugin.getServer().getPluginManager().enablePlugin(plugin);
                    }
                }
                else if (args[0].equalsIgnoreCase("holo")) {
                    if (player.hasPermission("itemstack.holo") ||
                            player.hasPermission("itemstack.*")) {
                        if (args[1].equalsIgnoreCase("on")) {
                            if (!getItemNameCorrector().hologram_visibility) {
                                getItemNameCorrector().setItemVisibility(true);
                            }
                        } else if (args[1].equalsIgnoreCase("off")) {
                            if (getItemNameCorrector().hologram_visibility) {
                                getItemNameCorrector().setItemVisibility(false);
                            }
                        }
                    }
                }
                else if (args[0].equalsIgnoreCase("clear")) {
                    if (player.hasPermission("itemstack.clear") ||
                            player.hasPermission("itemstack.*")){
                        for (Entity entity : player.getNearbyEntities(20,20,20)){
                            if (entity instanceof ArmorStand) {
                                ArmorStand stand = (ArmorStand) entity;
                                if (!stand.isVisible()) {
                                    StackItems.plugin.getItemNameCorrector().holograms.remove(stand);
                                    entity.remove();
                                }
                            } else if (entity instanceof Item) {
                                Item item = (Item) entity;
                                StackItems.plugin.getCoolDown().removeItemTimer(item);
                                StackItems.plugin.getItemNameCorrector().item_stand.remove(item);
                                item.remove();
                            }


                        }
                    }
                }
            }
        }
        return super.onCommand(sender, command, label, args);
    }

    public void removeAllData(){
        List<Item> delete_items = new ArrayList<>();

        List<ArmorStand> holograms = new ArrayList<>(getItemNameCorrector().holograms);

        for (ArmorStand stand : holograms) {
            for (Entity entity : stand.getNearbyEntities(1, 3, 1)){
                if (!(entity instanceof Item)) continue;
                Item item = (Item) entity;
                String uncolor_separator = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                        StackItems.plugin.config.getString("ITEM-SEPARATOR")));
                String name = ChatColor.stripColor(stand.getCustomName()).split(uncolor_separator)[0];

                if (name.toLowerCase(Locale.ROOT).contains(item.getName().toLowerCase(Locale.ROOT))){
                    String str_amount = ChatColor.stripColor(stand.getCustomName()).split(uncolor_separator)[1];
                    int amount = Integer.parseInt(CharMatcher.inRange('0', '9').retainFrom(str_amount));
                    ItemStack new_item = item.getItemStack();
                    new_item.setAmount(amount);
                    item.getWorld().dropItem(item.getLocation().clone().add(0,1,0),new_item);
                    delete_items.add(delete_items.size(), item);

                    break;
                }
            }
        }



        for (ArmorStand stand : holograms){
            stand.remove();
        }
        for (Item item : delete_items){
            item.remove();
        }




    }

    public ItemNameCorrector getItemNameCorrector(){
        return itemNameCorrector;
    }
    public CoolDown getCoolDown(){
        return coolDown;
    }


    private File configf;
    public FileConfiguration config;
    private void createConfigF(){
        configf = new File(getDataFolder(), "config.yml");
        if(!configf.exists()){
            configf.getParentFile().mkdirs();
            saveResource("config.yml",false);
        }
        config = new YamlConfiguration();
        try {
            config.load(configf);
        }catch (IOException | InvalidConfigurationException e){
            e.printStackTrace();
        }
    }
    public void saveConfigF(){
        try {
            config.save(configf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
