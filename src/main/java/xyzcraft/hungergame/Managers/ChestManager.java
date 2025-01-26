package xyzcraft.hungergame.Managers;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import xyzcraft.hungergame.Listener.CompassUsageListener;

import java.util.*;

public class ChestManager implements Listener {


    public static final Map<Location, Boolean> chestFilled = new HashMap<>();
    public static final Map<Location, Inventory> chestContents = new HashMap<>();


    public static List<ItemWithProbability> normal_items = new ArrayList<>();
    public static List<ItemWithProbability> pro_items = new ArrayList<>();


    @EventHandler
    public void onChestOpen(InventoryOpenEvent event) {


        if (event.getInventory().getLocation() != null) {
            Location chestLocation = event.getInventory().getLocation();
            Material blockType = chestLocation.getBlock().getType();


            if (blockType == Material.CHEST || blockType == Material.ENDER_CHEST) {


                Inventory inv;
                event.setCancelled(true);

                if (blockType == Material.CHEST) {
                    inv = Bukkit.createInventory(null, 27, "Normal Chest");
                    event.getPlayer().openInventory(inv);

                } else {
                    inv = Bukkit.createInventory(null, 27, "Pro Chest");
                    event.getPlayer().openInventory(inv);
                }


                if (chestFilled.getOrDefault(chestLocation, false)) {


                    Inventory storedInventory = chestContents.get(chestLocation);
                    if (storedInventory != null) {
                        for (int i = 0; i < storedInventory.getSize(); i++) {
                            inv.setItem(i, storedInventory.getItem(i));
                        }
                    }
                } else {


                    fillChestWithLoot(chestLocation, inv, blockType != Material.CHEST);
                }


                chestFilled.put(chestLocation, true);


                chestContents.put(chestLocation, inv);
            }
        }
    }


    private void fillChestWithLoot(Location location, Inventory inventory, boolean is_pro_chest) {


        for (int i = 0; i < inventory.getSize(); i++) {


            ItemWithProbability selectedItem = getRandomItem(is_pro_chest);
            if (selectedItem != null) {
                if (selectedItem.getItem().getType() == Material.COMPASS) {
                    inventory.setItem(i, CompassUsageListener.createCompass());
                } else {
                    inventory.setItem(i, selectedItem.getItem());

                }
            }
        }
        chestContents.put(location, inventory);
        removeDuplicatesFromChest(inventory);
    }


    private ItemWithProbability getRandomItem(boolean is_pro_chest) {
        double totalWeight = 0;
        ItemWithProbability item;
        if (!is_pro_chest) {
            item = getItemWithProbability(totalWeight, normal_items);
        } else {
            item = getItemWithProbability(totalWeight, pro_items);
        }
        return item;

    }

    private ItemWithProbability getItemWithProbability(double totalWeight, List<ItemWithProbability> proItems) {
        for (ItemWithProbability item : proItems) {
            totalWeight += item.getPercent();

        }

        double randomValue = Math.random() * totalWeight;

        double cumulativeWeight = 0;

        for (ItemWithProbability item : proItems) {
            cumulativeWeight += item.getPercent();
            if (randomValue <= cumulativeWeight) {
                return item;


            }
        }
        return null;
    }

    public void removeDuplicatesFromChest(Inventory chest) {
        Set<ItemStack> uniqueItems = new HashSet<>();
        for (int i = 0; i < chest.getSize(); i++) {
            if (chest.getItem(i) == null) continue;
            if (uniqueItems.contains(chest.getItem(i))) {
                chest.setItem(i, null);
            } else uniqueItems.add(chest.getItem(i));
        }
    }


    public static class ItemWithProbability {
        private final ItemStack item;
        private final double percent;

        public ItemWithProbability(String nbtStr, double percent) throws CommandSyntaxException {
            NBTTagCompound nbt = MojangsonParser.a(nbtStr);
            net.minecraft.world.item.ItemStack nmsItem = net.minecraft.world.item.ItemStack.a(nbt);
            this.item = CraftItemStack.asBukkitCopy(nmsItem);
            this.percent = percent;
        }

        public ItemStack getItem() {
            return item;
        }

        public double getPercent() {
            return percent;
        }
    }
}
