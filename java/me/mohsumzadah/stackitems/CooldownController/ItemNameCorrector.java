package me.mohsumzadah.stackitems.CooldownController;

import me.mohsumzadah.stackitems.StackItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class ItemNameCorrector {

    public List<ArmorStand> holograms = new ArrayList<>();
    public final HashMap<Item, ArmorStand> item_stand = new HashMap<>();
    public boolean hologram_visibility = StackItems.plugin.config.getBoolean("ITEM-HOLO-SHOW");

    public ItemNameCorrector(StackItems plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (item_stand.size() > 0) {
                    for (Item item : item_stand.keySet()) {
                        if (!item.getLocation().add(0, -1.5, 0)
                                .equals(item_stand.get(item).getLocation())) {
                            ArmorStand stand = item_stand.get(item);
                            stand.teleport(item.getLocation().add(0, -1.5, 0));
                        }
                    }
                }
                if (holograms.size() > 0){
                    for (ArmorStand stand : holograms){
                        if (!isItemUnder(stand)){
                            holograms.remove(stand);
                            stand.remove();
                            break;
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    private boolean isItemUnder(ArmorStand stand){
        for (Entity entity : stand.getNearbyEntities(0,3,0)){
            if (entity instanceof Item){
                Item item = (Item) entity;
                if (stand.getCustomName().contains(item.getName())){
                    return true;
                }
            }
        }
        return false;
    }

    public void addItemList(Item item, ArmorStand stand){
        holograms.add(holograms.size(), stand);
        item_stand.put(item,stand);
    }

    public void setItemVisibility(Boolean visibility_holo){
        hologram_visibility = visibility_holo;
        StackItems.plugin.config.set("ITEM-HOLO-SHOW", hologram_visibility);
        StackItems.plugin.saveConfigF();
        if (holograms.size() > 0) {
            for (ArmorStand stand : holograms){
                stand.setCustomNameVisible(hologram_visibility);
            }
        }
    }

    public boolean getHologramStatus(){
        return hologram_visibility;
    }


}