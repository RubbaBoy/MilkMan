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

import net.minecraft.server.v1_10_R1.*;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_10_R1.entity.CraftPlayer;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class Game implements Listener {

    EnderCrystal crystal;

    private boolean collected = true;

    Main main;

    public int count = 0;

    static GameState state = GameState.NOTHING;
    static BuildingEnum building = null;

    public Map<String, Integer> places = new HashMap<>();

    public Game(Main main) {
        this.main = main;
    }

    public void sendPlayers() throws Exception {
        state = GameState.WAITING;
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            ArenaManager.getManager().addPlayer(player);
        }

        state = GameState.COUNTDOWN;
        count = 0;
        new BukkitRunnable() {
            public void run() {
                if(count < 5) {
                    try {
                         for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                             sendTitle(player, "Starting game in " + (5 - count), "Get ready!", 0, 20, 0);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                } else if (count == 6) {
                    try {
                        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                            sendTitle(player, "The game has begun!", "Good luck!", 5, 30, 5);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                } else {
                    count = 0;
                    this.cancel();
                    start();
                }
            }
        }.runTaskTimer(Main.getPlugin(), 20, 20);
    }

    public void start() {


        Bukkit.getServer().getOnlinePlayers().stream().filter(udder -> ArenaManager.getManager().isInGame(udder)).forEach(udder -> {

            places.put(udder.getName(), 1);

            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Scoreboard board = manager.getNewScoreboard();
            Objective obj = board.registerNewObjective("Kills", "Player");

            obj.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Player Info");
            obj.setDisplaySlot(DisplaySlot.SIDEBAR);

            for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                Score score = obj.getScore(Bukkit.getPlayer(p1.getName()));
                score.setScore(0);
            }

            udder.setScoreboard(manager.getNewScoreboard());
            udder.setScoreboard(board);

        });

        state = GameState.INGAME;

        whileRunning();
    }

    public static GameState getGameState(){
        return state;
    }

    public static BuildingEnum getBuilding() {
        return building;
    }


    public static void sendTitle(Player player, String title, String subtitle, int fadeInTime, int staytime, int fadeOutTime) {
        PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;
        IChatBaseComponent titleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', title) + "\"}");
        IChatBaseComponent subtitleComponent = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + ChatColor.translateAlternateColorCodes('&', subtitle) + "\"}");
        PacketPlayOutTitle titlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleComponent);
        PacketPlayOutTitle subtitlePacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleComponent);
        PacketPlayOutTitle thyme = new PacketPlayOutTitle(fadeInTime, staytime, fadeOutTime);

        connection.sendPacket(titlePacket);
        connection.sendPacket(subtitlePacket);
        connection.sendPacket(thyme);
    }


    public void whileRunning() {
        if (state == GameState.INGAME) {

            if (building == null) {
                new BukkitRunnable() {
                    public void run() {
                        tellHouse();
                    }
                }.runTaskLater(main, 40);
            }
        }
    }

    public void clear() {
        try {
            ScoreboardManager manager = Bukkit.getScoreboardManager();
            Bukkit.getServer().getOnlinePlayers().stream().filter(for_player -> ArenaManager.getManager().isInGame(for_player)).forEach(for_player -> {
                for_player.setWalkSpeed(0.2F);

                for_player.setScoreboard(manager.getNewScoreboard());

                ArenaManager.getManager().removePlayer(for_player);
            });

            this.places.clear();
            state = GameState.NOTHING;
            building = null;
            this.count = 0;
            collected = true;
            crystal.remove();

            ArenaManager AM = new ArenaManager(main);
            AM.clearArenas();
        } catch (NullPointerException ignored) {}
    }

    int lastsite = -1;

    public void tellHouse() {
        Random random = new Random();
        int site = random.nextInt(7);

        if (lastsite != -1) {
            if (site == lastsite) {
                if (site == 6) {
                    site = lastsite - random.nextInt(6);
                } else {
                    site = lastsite + 1;
                }
            }
        }

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            if (ArenaManager.getManager().isInGame(player)) {
                switch (site) {
                    case 0:
                        sendTitle(player, ChatColor.RED + "Deliver to the red building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.RED + "Deliver to the red building!");
                        building = BuildingEnum.RED;
                        break;
                    case 1:
                        sendTitle(player, ChatColor.YELLOW + "Deliver to the yellow building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.YELLOW + "Deliver to the yellow building!");
                        building = BuildingEnum.YELLOW;
                        break;
                    case 2:
                        sendTitle(player, ChatColor.BLUE + "Deliver to the blue building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.BLUE + "Deliver to the blue building!");
                        building = BuildingEnum.BLUE;
                        break;
                    case 3:
                        sendTitle(player, ChatColor.GREEN + "Deliver to the green building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.GREEN + "Deliver to the green building!");
                        building = BuildingEnum.GREEN;
                        break;
                    case 4:
                        sendTitle(player, ChatColor.BLACK + "Deliver to the black building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.BLACK + "Deliver to the black building!");
                        building = BuildingEnum.BLACK;
                        break;
                    case 5:
                        sendTitle(player, ChatColor.WHITE + "Deliver to the white building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.WHITE + "Deliver to the white building!");
                        building = BuildingEnum.WHITE;
                        break;
                    case 6:
                        sendTitle(player, ChatColor.DARK_AQUA + "Deliver to the cyan building!", "", 5, 60, 5);
                        player.sendMessage(ChatColor.DARK_AQUA + "Deliver to the cyan building!");
                        building = BuildingEnum.CYAN;
                        break;
                }
            }
        }

        lastsite = site;
    }

    @EventHandler
    public void onFoodChange(FoodLevelChangeEvent event) {
        Player player = (Player) event.getEntity();
        if (Game.getGameState() == GameState.INGAME) {
            if (ArenaManager.getManager().isInGame(player)) {
                event.setFoodLevel(2000);
                player.setFoodLevel(2000);
            }
        }
    }


    public void createOrb(Location location) {
        crystal = main.getPlayWorld().spawn(location, EnderCrystal.class);
        collected = false;
    }

    public void destroyOrb(EnderCrystal crystal) {
        crystal.remove();
        crystal = null;
        collected = true;
    }


    @EventHandler
    public void onCrystalExplode(EntityDamageByEntityEvent event) {
        if (event.getEntity().getType().equals(EntityType.ENDER_CRYSTAL)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (Game.getGameState().equals(GameState.COUNTDOWN) || Game.getGameState().equals(GameState.WAITING)) {
            if (ArenaManager.getManager().isInGame(event.getPlayer())) {
                if (event.getFrom() != event.getTo()) {
                    Location to = event.getFrom();
                    to.setPitch(event.getTo().getPitch());
                    to.setYaw(event.getTo().getYaw());
                    event.setTo(to);
                }
            }
        } else
        if (Game.getGameState().equals(GameState.INGAME)) {
            if (ArenaManager.getManager().isInGame(event.getPlayer())) {
                if (crystal != null && !collected) {
                    if (event.getPlayer().getLocation().getBlockX() == crystal.getLocation().getBlockX()
                            && event.getPlayer().getLocation().getBlockY() == crystal.getLocation().getBlockY()
                            && event.getPlayer().getLocation().getBlockZ() == crystal.getLocation().getBlockZ()) {
                        destroyOrb(crystal);
                        crystal = null;
                        Random random = new Random();
                        int i = 0;
                        i = random.nextInt(3);

                        for (Player for_player : Bukkit.getServer().getOnlinePlayers()) {
                            if (ArenaManager.getManager().isInGame(for_player)) {
                                for_player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Player " + event.getPlayer().getDisplayName() + ChatColor.RED + " has collected a powerup!");
                                switch (i) {
                                    case 0:
                                        for_player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Now activating blindness for all other players");
                                        if (for_player != event.getPlayer()) {
                                            for_player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 200, 1));
                                        }
                                        break;
                                    case 1:
                                        for_player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Now activating speed I for " + event.getPlayer().getDisplayName());
                                        if (for_player == event.getPlayer()) {
                                            for_player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, i - 1));
                                        }
                                        break;
                                    case 2:
                                        for_player.sendMessage(ChatColor.BOLD + "" + ChatColor.RED + "Now activating speed II for " + event.getPlayer().getDisplayName());
                                        if (for_player == event.getPlayer()) {
                                            for_player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, i - 1));
                                        }
                                        break;
                                }
                            }
                        }

                    }
                }
            }
        }
    }


    @EventHandler
    public void onItemUse(PlayerInteractEvent event) {
        try {
            final Player player = event.getPlayer();
            if (Game.getGameState() == GameState.INGAME) {
                if (ArenaManager.getManager().isInGame(player)) {
                    if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (player.getItemInHand().getType() == Material.MILK_BUCKET) {
                            event.setCancelled(true);
                            Block standingOn = player.getLocation().getBlock().getLocation().add(0, -1, 0).getBlock();
                            Block standingOnBelow = player.getLocation().getBlock().getLocation().add(0, -2, 0).getBlock();
                            if (Game.getBuilding().getMaterial() != null) {
                                if (standingOn.getType() == Game.getBuilding().getMaterial() || standingOnBelow.getType() == Game.getBuilding().getMaterial()) {
                                    Block temp_block = null;
                                    if (standingOn.getType() == Game.getBuilding().getMaterial()) {
                                        temp_block = standingOn;
                                    }
                                    if (standingOnBelow.getType() == Game.getBuilding().getMaterial()) {
                                        temp_block = standingOnBelow;
                                    }
                                    if (temp_block != null && temp_block.getData() == getBuilding().getColor().getData()) {


                                        Random random = new Random();
                                        if (collected) {
                                            createOrb(main.getPowerupLocs().get(random.nextInt(main.getPowerupLocs().size())));
                                        }

                                        Integer int_ = 0;

                                        if (!places.containsKey(player.getName())) {
                                            places.put(player.getName(), 1);
                                        } else {
                                            int_ = places.get(player.getName());
                                            places.put(player.getName(), int_ + 1);
                                        }


                                        Integer max = 10;
                                        if (Objects.equals(places.get(player.getName()), max)) {
                                            Bukkit.getServer().getOnlinePlayers().stream().filter(for_player -> ArenaManager.getManager().isInGame(for_player)).forEach(for_player -> {
                                                sendTitle(for_player, player.getName() + " has won!", "", 5, 110, 5);
                                            });
                                            clear();
                                        }


                                        for (Player for_player : Bukkit.getServer().getOnlinePlayers()) {
                                            if (ArenaManager.getManager().isInGame(for_player)) {

                                                ScoreboardManager manager = Bukkit.getScoreboardManager();
                                                Scoreboard board = manager.getNewScoreboard();
                                                Objective obj = board.registerNewObjective("Kills", "Player");

                                                obj.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Player Info");
                                                obj.setDisplaySlot(DisplaySlot.SIDEBAR);


                                                Score score = obj.getScore(Bukkit.getPlayer(player.getName()));

                                                score.setScore(places.get(player.getName()));


                                                for (Player p1 : Bukkit.getServer().getOnlinePlayers()) {
                                                    if (!Objects.equals(p1.getName(), player.getName())) {
                                                        score = obj.getScore(p1.getName());
                                                        if (places.get(p1.getName()) != null) {
                                                            score.setScore(places.get(p1.getName()));
                                                        } else {
                                                            places.put(p1.getName(), 0);
                                                            score.setScore(places.get(p1.getName()));
                                                        }
                                                    }
                                                }

                                                for_player.setScoreboard(manager.getNewScoreboard());
                                                for_player.setScoreboard(board);

                                                building = null;

                                                Game.sendTitle(for_player, "", player.getName() + " has delivered to the site!", 5, 90, 5);
                                                for_player.sendMessage(ChatColor.BOLD + player.getName() + " has delivered to the site!");
                                            }
                                        }

                                        whileRunning();

                                    } else {
                                        player.sendMessage(ChatColor.BOLD + "You must deliver to the right place!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (NullPointerException ignored) {}
    }


}