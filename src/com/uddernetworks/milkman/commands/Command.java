/*
Copyright (C) 2015-2015  Adam Yarris

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


package com.uddernetworks.milkman.commands;

import com.uddernetworks.milkman.main.ArenaManager;
import com.uddernetworks.milkman.main.Game;
import com.uddernetworks.milkman.main.Main;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Command implements CommandExecutor {
    Main plugin;

    public Command(Main passedPlugin)
    {
        this.plugin = passedPlugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String s, String[] args) {
        Player player = (Player) sender;
        if (args[0].equalsIgnoreCase("start")) {
            try {
                ArenaManager.getManager().createArena(player.getLocation());
                new Game(plugin).sendPlayers();
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        } else if (args[0].equalsIgnoreCase("stop")) {
            Game game = new Game(plugin);
            game.clear();
        }
        return true;
    }
}