package com.zerofall.ezstorage.proxy;

import com.zerofall.ezstorage.gui.client.GuiStorageCore;
import com.zerofall.ezstorage.init.EZBlocks;
import com.zerofall.ezstorage.init.EZItems;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;

/** The mod's client proxy */
public class ClientProxy extends CommonProxy {

	@Override
	public void registerRenders() {
		EZBlocks.registerRenders();
		EZItems.registerRenders();
	}

	@Override
	public EntityPlayer getClientPlayer() {
		return Minecraft.getMinecraft().player;
	}

	@Override
	public void markGuiDirty() {
		GuiScreen scr = Minecraft.getMinecraft().currentScreen;
		if (scr instanceof GuiStorageCore) {
			GuiStorageCore gui = (GuiStorageCore) scr;
			gui.markFilterUpdate();
		}
	}

}
