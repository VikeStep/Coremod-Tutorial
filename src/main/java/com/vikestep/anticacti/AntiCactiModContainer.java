package com.vikestep.anticacti;

import com.google.common.eventbus.EventBus;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

import java.util.Arrays;

public class AntiCactiModContainer extends DummyModContainer
{
    public AntiCactiModContainer()
    {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId = "anticacti";
        meta.name = "AntiCacti";
        meta.description = "Stops Cactus Damage";
        meta.version = "1.7.10-1.0";
        meta.authorList = Arrays.asList("VikeStep");
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }
}