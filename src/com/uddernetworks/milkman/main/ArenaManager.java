/*
Copyright (C) 2015-2016  Adam Yarris

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


package com.uddernetworks.milkman.main;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ArenaManager {

    static Main plugin;

    private static ArenaManager am;

    private final Map<UUID, Location> locs = new HashMap<UUID, Location>();
    private final Map<UUID, ItemStack[]> inv = new HashMap<UUID, ItemStack[]>();
    private final Map<UUID, ItemStack[]> armor = new HashMap<UUID, ItemStack[]>();

    private final List<Arena> arenas = new ArrayList<Arena>();
    private int arenaSize = 0;

    public ArenaManager(Main main) {
        if (main != null) {
            plugin = main;
        }
    }

    public static ArenaManager getManager() {
        if (am == null)
            am = new ArenaManager(plugin);

        return am;
    }

    public Arena getArena(){
        for (Arena a : this.arenas) {
            if (a.getId() == 1) {
                return a;
            }
        }

        return null;
    }

    public void addPlayer(Player p) {
        Arena a = this.getArena();
        if (a == null) {
            p.sendMessage("Invalid arena!");
            return;
        }

        if (this.isInGame(p)) {
            p.sendMessage("Cannot join more than 1 game!");
            return;
        }

        a.getPlayers().add(p.getUniqueId());

        inv.put(p.getUniqueId(), p.getInventory().getContents());
        armor.put(p.getUniqueId(), p.getInventory().getArmorContents());

        p.getInventory().setArmorContents(null);
        p.getInventory().clear();


        ItemStack milk = new ItemStack(Material.MILK_BUCKET);
        ItemMeta milk_meta = milk.getItemMeta();
        milk_meta.setDisplayName(ChatColor.RESET + "Milk");
        ArrayList<String> tnt_Lore = new ArrayList<>();
        tnt_Lore.add(ChatColor.RESET + "Right click to deliver the milk!");
        milk_meta.setLore(tnt_Lore);
        milk.setItemMeta(milk_meta);

        p.getInventory().addItem(milk);
        p.getInventory().addItem(new ItemStack(Material.MAP, 1, (short) 3));

        p.setGameMode(GameMode.SURVIVAL);

        locs.put(p.getUniqueId(), p.getLocation());


        p.setFlying(false);
        p.setFoodLevel(20);
        try {
            p.setWalkSpeed(0.5F);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        Random random = new Random();

        p.teleport(plugin.getSpawnBlocks().get(random.nextInt(plugin.getSpawnBlocks().size())));
    }

    public void removePlayer(Player p) {
        try {
            Arena a = null;

            for (Arena arena : this.arenas) {
                if (arena.getPlayers().contains(p.getUniqueId()))
                    a = arena;
            }

            if (a == null) {
                p.sendMessage("Invalid operation!");
                return;
            }

            a.getPlayers().remove(p.getUniqueId());

            p.getActivePotionEffects().clear();

//        p.setWalkSpeed(1);

            p.getInventory().clear();
            p.getInventory().setArmorContents(null);

            p.getInventory().setContents(inv.get(p.getUniqueId()));
            p.getInventory().setArmorContents(armor.get(p.getUniqueId()));

            inv.remove(p.getUniqueId());
            armor.remove(p.getUniqueId());

            p.teleport(locs.get(p.getUniqueId()));
            locs.remove(p.getUniqueId());

            p.setGameMode(GameMode.ADVENTURE);

            p.setFireTicks(0);
            p.setHealth(p.getMaxHealth());
        } catch (NullPointerException ignored) {}
    }

    public Arena createArena(Location l) {
        this.arenaSize++;

        Arena a = new Arena(l, this.arenaSize);
        this.arenas.add(a);

        return a;
    }


    public void clearArenas() {
        this.arenas.clear();
        this.arenaSize = 0;
        this.locs.clear();
        this.inv.clear();
        this.armor.clear();
    }

    public boolean isInGame(Player p) {
        for (Arena a : this.arenas) {
            if (a.getPlayers().contains(p.getUniqueId()))
                return true;
        }
        return false;
    }

    public String serializeLoc(Location l){
        return l.getWorld().getName() + "," + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }

    public Location deserializeLoc(String s){
        String[] st = s.split(",");
        return new Location(Bukkit.getWorld(st[0]), Integer.parseInt(st[1]), Integer.parseInt(st[2]), Integer.parseInt(st[3]));
    }
}
