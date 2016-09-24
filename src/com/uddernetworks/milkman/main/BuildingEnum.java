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

import org.bukkit.DyeColor;
import org.bukkit.Material;

public enum BuildingEnum {

    RED(Material.STAINED_CLAY, DyeColor.RED),
    YELLOW(Material.STAINED_CLAY, DyeColor.YELLOW),
    BLUE(Material.STAINED_CLAY, DyeColor.BLUE),
    GREEN(Material.STAINED_CLAY, DyeColor.GREEN),
    BLACK(Material.STAINED_CLAY, DyeColor.BLACK),
    WHITE(Material.WOOL, DyeColor.WHITE),
    CYAN(Material.STAINED_CLAY, DyeColor.CYAN);

    private Material material;
    private DyeColor color;

    BuildingEnum(Material material, DyeColor color){
        this.material = material;
        this.color = color;

    }

    public Material getMaterial(){
        return material;
    }

    public DyeColor getColor() {
        return color;
    }
}
