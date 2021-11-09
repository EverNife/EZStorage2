package com.zerofall.ezstorage.integration;

import com.gamerforea.eventhelper.util.EventUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.Loader;

public class EverForgeLibIntegration {

    private static Boolean isPresent = null;

    private static boolean isPresent(){
        if (isPresent == null){
            isPresent = Loader.isModLoaded("eventhelper");
        }
        return isPresent;
    }

    public static boolean cantBreak(EntityPlayer player, BlockPos blockPos){
        if (isPresent()){
            return EventUtils.cantBreak(player, blockPos);
        }
        return false;
    }

}
