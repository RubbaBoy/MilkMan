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

import com.uddernetworks.milkman.commands.Command;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;

public class Main extends JavaPlugin implements Listener {

    public static Main plugin;

    @Override
    public void onEnable(){
        plugin = this;
        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        this.getCommand("mm").setExecutor(new Command(this));
        new ArenaManager(this);

        Bukkit.getServer().getPluginManager().registerEvents(new Game(this), this);
    }

    @Override
    public void onDisable(){

        try {
            Game game = new Game(plugin);
            game.clear();
        } catch (Exception ignored) {}

        plugin = null;
    }

    public ArrayList<Location> getSpawnBlocks() {
            reloadConfig();
            int spawn1_x = this.getConfig().getInt("Spawn1-X");
            int spawn1_y = this.getConfig().getInt("Spawn1-Y");
            int spawn1_z = this.getConfig().getInt("Spawn1-Z");

            int spawn2_x = this.getConfig().getInt("Spawn2-X");
            int spawn2_y = this.getConfig().getInt("Spawn2-Y");
            int spawn2_z = this.getConfig().getInt("Spawn2-Z");

            HashSet<Location> locs = new HashSet<>();

            for (int x = spawn1_x; x <= spawn2_x; x++) {
                for (int y = spawn1_y; y <= spawn2_y; y++) {
                    for (int z = spawn1_z; z <= spawn2_z; z++) {
                        locs.add(new Location(getPlayWorld(), x, y, z));
                    }
                }
            }

            return new ArrayList<>(locs);
    }

    public ArrayList<Location> getPowerupLocs() throws NullPointerException {

        reloadConfig();

        HashSet<Location> locs = new HashSet<>();

        for (int i = 0; i < 8; i++) {
            locs.add(new Location(getPlayWorld(), this.getConfig().getInt("Orb_" + (i + 1) + "_X"), this.getConfig().getInt("Orb_" + (i + 1) + "_Y"), this.getConfig().getInt("Orb_" + (i + 1) + "_Z")));
        }

        return new ArrayList<>(locs);
    }

    public World getPlayWorld() {
        return Bukkit.getWorld(this.getConfig().getString("Spawn-World"));
    }

    public static Main getPlugin() {
        return plugin;
    }
}