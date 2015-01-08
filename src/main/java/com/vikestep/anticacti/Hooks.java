package com.vikestep.anticacti;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

public class Hooks
{
    public static boolean cactusDoesDamage(Entity entity)
    {
        return !(entity instanceof EntityPlayer);
    }
}
