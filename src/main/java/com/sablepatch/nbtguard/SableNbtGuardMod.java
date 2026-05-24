package com.sablepatch.nbtguard;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SableNbtGuard.MOD_ID)
public class SableNbtGuardMod {
    public SableNbtGuardMod(IEventBus bus) {
        SableNbtGuard.LOGGER.info("SableNbtGuard loaded - BlockEntity assembly guard active");
    }
}
