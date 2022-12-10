package me.mohsumzadah.stackitems.Listeners;

import com.google.common.base.CharMatcher;
import me.mohsumzadah.stackitems.StackItems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ItemsListener implements Listener {
    HashMap<Item, Integer> item_amount = new HashMap<>();

    @EventHandler
    public void playerDeath(PlayerDeathEvent event){

        HashMap<ItemStack, Integer> player_items_amount = new HashMap<>();
        HashMap<ItemStack, Location> player_items_loc = new HashMap<>();

        Location loc = new Location(event.getEntity().getWorld(),
                event.getEntity().getLocation().getX(),
                event.getEntity().getLocation().getY(),
                event.getEntity().getLocation().getZ());

        for (ItemStack item : event.getEntity().getInventory().getStorageContents()) {
            if (item == null) continue;
            int amount = item.clone().getAmount();
            item.setAmount(1);
            if (player_items_amount.containsKey(item)) {
                player_items_amount.put(item, player_items_amount.get(item) + amount);
            } else {
                int rand_loc = new Random().nextInt(-2, 2);
                player_items_amount.put(item, amount);
                player_items_loc.put(item, loc.clone().add(rand_loc, 0, rand_loc));

            }
        }
//        double pos = 0.0;
        for (ItemStack player_item : player_items_amount.keySet()){

            Item dropped_item = player_items_loc.get(player_item).getWorld()
                    .dropItem(player_items_loc.get(player_item),player_item);

            createHologramPlayerItems(player_items_loc.get(player_item)
                    , dropped_item
                    , player_items_amount.get(player_item));

//            pos += 0.0;
        }
        List<ItemStack> dropped_items = new ArrayList<>();
        for (ItemStack item : event.getDrops()){
            dropped_items.add(dropped_items.size(), item);
        }
        event.getDrops().removeAll(dropped_items);
    }


    @EventHandler
    public void itemDrop(PlayerDropItemEvent event){

        Item item = event.getItemDrop();
        int amount = item.getItemStack().clone().getAmount();
        item.getItemStack().setAmount(1);
        item.getItemStack().getItemMeta().setLore(Collections.singletonList(item.getUniqueId().toString()));

        boolean item_is_ground = false;
        for (Entity entity : event.getItemDrop().getNearbyEntities(5,5,5)){
            if (entity instanceof Item){
                Item entity_item = (Item) entity;

                if (entity_item.getItemStack().getType().equals(item.getItemStack().getType())){
                    item_amount.put(entity_item, item_amount.get(entity_item) + amount);
                    createHologram(false, entity_item);
                    item.remove();
                    item_is_ground = true;
                    break;
                }
            }
        }
        if (!item_is_ground){
            item_amount.put(item, amount);
            createHologram(true, item);
        }
    }

    @EventHandler
    public void itemTake(EntityPickupItemEvent event){
        if (event.getEntity() instanceof Player){
            Player player = (Player) event.getEntity();
            Item item = event.getItem();
            for (Entity entity : item.getNearbyEntities(0, 3, 0)){
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible()){
                    String name = ChatColor.stripColor(stand.getCustomName());
                    String separator = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-SEPARATOR")));

                    if (name.contains(item.getName())){
                        int amount = Integer.parseInt(CharMatcher.inRange('0', '9')
                                .retainFrom(Arrays.toString(name.split(" ")).split(separator)[1]));
                        int can_take_amount = hasAvaliableAmount(player, item);
                        if (can_take_amount!=0){
                            if (amount <= can_take_amount) {
                                item.getItemStack().setAmount(amount-1);
                                player.getInventory().addItem(item.getItemStack());
                                StackItems.plugin.getCoolDown().removeItemTimer(item);
                                StackItems.plugin.getItemNameCorrector().item_stand.remove(item);
                                stand.remove();
                                item.remove();

                                player.getWorld().playSound(player.getLocation(),
                                        Sound.ENTITY_ITEM_PICKUP, 1,amount);
                                break;
                            } else {
                                item.getItemStack().setAmount(1);
                                item_amount.put(item, amount - can_take_amount);
                                createHologram(false,item);
                                event.setCancelled(true);
                                item.getItemStack().setAmount(amount-can_take_amount-1);
                                player.getInventory().addItem(item.getItemStack());

                                player.getWorld().playSound(player.getLocation(),
                                        Sound.ENTITY_ITEM_PICKUP, 1,amount);
                                break;
                            }


                        }

                    }
                }
            }
        }
        else if (event.getEntity() instanceof Piglin) {
            Piglin piglin = (Piglin) event.getEntity();
            Item item = event.getItem();
            int amount = 0;
            for (Entity entity : item.getNearbyEntities(0, 3, 0)){
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible()){

                    String separator = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-SEPARATOR")));
                    String name = ChatColor.stripColor(stand.getCustomName());
                    if (name.contains(item.getName())){
                        amount = Integer.parseInt(CharMatcher.inRange('0', '9')
                                .retainFrom(name.split(separator)[1]));

                        item.getItemStack().setAmount(1);

                        StackItems.plugin.getCoolDown().removeItemTimer(item);
                        StackItems.plugin.getItemNameCorrector().item_stand.remove(item);
                        stand.remove();
                        break;
                    }
                }
            }
            if (amount-1 != 0){
                Item new_item = item.getWorld().dropItem(item.getLocation(),item.getItemStack());

                item_amount.put(new_item, amount-1);
                createHologram(true, new_item);
            }


        }
        else if (event.getEntity() instanceof Villager) {
            Villager villager  = (Villager) event.getEntity();
            Item item = event.getItem();
            for (Entity entity : item.getNearbyEntities(0, 3, 0)){
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible()){
                    String name = ChatColor.stripColor(stand.getCustomName());
                    String separator = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-SEPARATOR")));
                    if (name.contains(item.getName())){
                        int amount = Integer.parseInt(CharMatcher.inRange('0', '9')
                                .retainFrom(Arrays.toString(name.split(" ")).split(separator)[1]));

                        item.getItemStack().setAmount(amount-1);
                        villager.getInventory().addItem(item.getItemStack());
                        StackItems.plugin.getCoolDown().removeItemTimer(item);
                        StackItems.plugin.getItemNameCorrector().item_stand.remove(item);
                        stand.remove();
                        item.remove();
                        break;
                    }
                }
            }

        }


    }


    public void createHologram(Boolean is_new, Item item){
        if (is_new){

            Entity hologram_entity = item.getWorld()
                    .spawnEntity(item.getLocation().clone().add(0, -1.5 ,0),EntityType.ARMOR_STAND);

            ArmorStand hologram = (ArmorStand) hologram_entity;
            hologram.setVisible(false);
            hologram.setCustomNameVisible(StackItems.plugin.getItemNameCorrector().getHologramStatus());

            String item_name = ChatColor.translateAlternateColorCodes('&',
                    StackItems.plugin.config.getString("ITEM-NAME"))
                    .replaceAll("%name%", item.getName());

            String amount = ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-AMOUNT"))
                    .replaceAll("%amount%", String.valueOf(item_amount.get(item)));

            String time = ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-TIME"))
                    .replaceAll("%time%", String.valueOf(StackItems.plugin.config
                            .getInt("ITEM-REMOVE-SECOND")));

            String separator = ChatColor.translateAlternateColorCodes('&',
                    StackItems.plugin.config.getString("ITEM-SEPARATOR"));

            hologram.setCustomName(item_name+" " + separator + " " +
                    amount + " " + separator + " " + time);
            hologram.setGravity(false);

            StackItems.plugin.getItemNameCorrector().addItemList(item, hologram);
            StackItems.plugin.getCoolDown().addItem(item, item_name, amount, time, separator);



        }else {
            for (Entity entity : item.getNearbyEntities(5, 3, 5)) {
                if (!(entity instanceof ArmorStand)) continue;
                ArmorStand stand = (ArmorStand) entity;
                if (!stand.isVisible()){
                    String uncolor_separator = ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&',
                            StackItems.plugin.config.getString("ITEM-SEPARATOR")));
                    String name = ChatColor.stripColor(stand.getCustomName()).split(uncolor_separator)[0];
                    if (name.contains(item.getName())){

                        String item_name = ChatColor.translateAlternateColorCodes('&',
                                        StackItems.plugin.config.getString("ITEM-NAME"))
                                .replaceAll("%name%", item.getName());

                        String amount = ChatColor.translateAlternateColorCodes('&',
                                        StackItems.plugin.config.getString("ITEM-AMOUNT"))
                                .replaceAll("%amount%", String.valueOf(item_amount.get(item)));


                        String separator = ChatColor.translateAlternateColorCodes('&',
                                StackItems.plugin.config.getString("ITEM-SEPARATOR"));


                        if (ChatColor.stripColor(stand.getCustomName()).split(uncolor_separator).length == 2){
                            stand.setCustomName(item_name + " " + separator + " "+amount);
                        }else{
                            String time =StackItems.plugin.getCoolDown().returnItemTime(item);
                            stand.setCustomName(item_name + " " + separator + " " + amount +
                                    " " + separator + " " + time);
                            StackItems.plugin.getCoolDown().addItem(item, "", amount, "", "");

                        }


                        break;
                    }
                }
            }
        }


    }

    public void createHologramPlayerItems(Location loc, Item drop, Integer amount_int){
        Entity hologram_entity = loc.getWorld().spawnEntity(loc.add(0, -1.5, 0)
                , EntityType.ARMOR_STAND);

        ArmorStand hologram = (ArmorStand) hologram_entity;
        hologram.setVisible(false);
        hologram.setGravity(false);

        hologram.setCustomNameVisible(StackItems.plugin.getItemNameCorrector().getHologramStatus());

        String item_name = ChatColor.translateAlternateColorCodes('&',
                        StackItems.plugin.config.getString("ITEM-NAME"))
                .replaceAll("%name%", drop.getName());

        String amount = ChatColor.translateAlternateColorCodes('&',
                        StackItems.plugin.config.getString("ITEM-AMOUNT"))
                .replaceAll("%amount%", String.valueOf(amount_int));

        String separator = ChatColor.translateAlternateColorCodes('&',
                StackItems.plugin.config.getString("ITEM-SEPARATOR"));

        hologram.setCustomName(item_name+" " + separator + " " +
                amount);
        item_amount.put(drop, amount_int);



        StackItems.plugin.getItemNameCorrector().addItemList(drop, hologram);
//        StackItems.plugin.getItemNameCorrector().addItemList(drop);
    }

    public int hasAvaliableAmount(Player player, Item item_type){
        int free_amount = 0;
        Inventory inv = player.getInventory();
        for (ItemStack item: inv.getStorageContents()) {
            if(item == null) {
                free_amount += item_type.getItemStack().getMaxStackSize();
            } else if (item.getType().equals(item_type.getItemStack().getType())) {

                free_amount += item_type.getItemStack().getMaxStackSize() - item.getAmount();
            }
        }
        return free_amount;
    }
}
