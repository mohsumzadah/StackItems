package me.mohsumzadah.stackitems.CooldownController;

import com.google.common.base.CharMatcher;
import me.mohsumzadah.stackitems.StackItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CoolDown {

    // 0 = item_name, 1=amount, 2=time, 3=separator
    public final HashMap<Item, List<String>> item_set = new HashMap<>();
    List<Item> remove_items = new ArrayList<>();

    public CoolDown(StackItems plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (item_set.size() > 0){

                    for (Item item : item_set.keySet()){

                        String time = item_set.get(item).get(2);
                        int old_num = Integer.parseInt(CharMatcher.inRange('0', '9').retainFrom(time));

                        if (old_num > 0) {
                            for (Entity entity : item.getNearbyEntities(0, 20, 0)) {

                                if (!(entity instanceof ArmorStand)) continue;
                                ArmorStand stand = (ArmorStand) entity;
                                String name = ChatColor.stripColor(stand.getCustomName());

                                if (name.contains(item.getName())) {
                                    String separator = item_set.get(item).get(3);
                                    String item_name = item_set.get(item).get(0);
                                    String amount = item_set.get(item).get(1);

                                    stand.setCustomName(item_name + " " + separator + " " + amount + " "
                                            + separator + " " + time);

                                    int new_number = old_num - 1;
                                    time = time.replaceAll(String.valueOf(old_num), String.valueOf(new_number));
                                    addItem(item, item_name, amount, time, separator);
                                }
                            }
                        }else {
                            for (Entity entity : item.getNearbyEntities(0, 20, 0)) {
                                if (!(entity instanceof ArmorStand)) continue;
                                ArmorStand stand = (ArmorStand) entity;
                                if (stand.equals(StackItems.plugin.getItemNameCorrector().item_stand.get(item))){
                                    stand.remove();
                                    StackItems.plugin.getItemNameCorrector().holograms.remove(stand);
                                    break;
                                }
                            }
                            item_set.remove(item);
                            item.remove();
                            StackItems.plugin.getItemNameCorrector().item_stand.remove(item);
                        }
                    }

                }
            }
        }.runTaskTimer(plugin, 0, 20);
    }


    public void removeItemTimer(Item item){
        if (item_set.containsKey(item)){
            item_set.remove(item);
        }
    }

    public void addItem(Item item, String item_name, String amount, String time, String separator){
        // 0 = item_name, 1=amount, 2=time, 3=separator
        if (item_name == "") item_name = item_set.get(item).get(0);
        if (amount == "") amount = item_set.get(item).get(1);
        if (time == "") time = item_set.get(item).get(2);
        if (separator == "") separator = item_set.get(item).get(3);

        List<String> new_list = new ArrayList<>();
        new_list.add(0, item_name);
        new_list.add(1, amount);
        new_list.add(2, time);
        new_list.add(3, separator);
        item_set.put(item, new_list);

    }

    public String returnItemTime(Item item){
        if (item_set.containsKey(item)){
            return item_set.get(item).get(2);
        }
        return null;
    }

}