package com.zerofall.ezstorage.item;

import com.zerofall.ezstorage.block.BlockStorageCore;
import com.zerofall.ezstorage.tileentity.TileEntityStorageCore;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

/** An Remote Access Terminal to acess storage cores */
public class ItemRemoteAccessTerminal extends EZItem {

	public ItemRemoteAccessTerminal(String name) {
		super(name);
		setMaxStackSize(1);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {

		if(!world.isRemote && hand == EnumHand.MAIN_HAND && !(player instanceof FakePlayer) && !player.isSneaking()) {

			ItemStack stack = player.getHeldItem(hand);
			ActionResult<ItemStack> RESULT_SUCESS = new ActionResult(EnumActionResult.PASS, player.getHeldItem(hand)); //Create default Return value

			// get the tag compound
			NBTTagCompound nbtTagCompound = stack.hasTagCompound() ? stack.getTagCompound() : null;

			// check if the terminal is linked
			if(nbtTagCompound == null || !nbtTagCompound.getBoolean("isLinked")) {
				player.sendMessage(new TextComponentString(""));
				player.sendMessage(new TextComponentString("§e§l ▶ §cO seu [Remote Access Terminal] não está vinculado a nenhum StorageCore!"));
				player.sendMessage(new TextComponentString("§e§l    §e > SEGURE SHIFT e clique com o botão direito em um StorageCore para vincular o mesmo!"));
				player.sendMessage(new TextComponentString(""));
				return RESULT_SUCESS;
			}

			// check if world exists
			String worldName = nbtTagCompound.getString("world");
			WorldServer worldServer = null;

			for (WorldServer aWorld : DimensionManager.getWorlds()) {
				if (aWorld.getWorldInfo().getWorldName().equals(worldName)){
					worldServer = aWorld;
					break;
				}
			}

			if (worldServer == null){
				player.sendMessage(new TextComponentString("§e§l ▶ §cO seu [Remote Access Terminal] está vinculado a um mundo que não existe! (world=" +  worldName + ")"));
				return RESULT_SUCESS;
			}

			int[] xyz = nbtTagCompound.getIntArray("coords");
			BlockPos corePos = new BlockPos(xyz[0], xyz[1], xyz[2]);

			//Check if the chunk where the target block is, is loaded!
			Chunk chunk = worldServer.getChunkProvider().getLoadedChunk(corePos.getX() >> 4, corePos.getZ() >> 4);
			if (chunk == null){
				player.sendMessage(new TextComponentString("§e§l ▶ §cInfelizmente a chunk do bloco §e[" + corePos.getX() + "," + corePos.getY() + "," + corePos.getZ() + "]§c não está carregada!"));
				return RESULT_SUCESS;
			}

			// check if the core block is a valid tile
			if( !(world.getTileEntity(corePos) instanceof TileEntityStorageCore)){
				player.sendMessage(new TextComponentString("§e§l ▶ §cAparentemente o bloco na posição §e[" + corePos.getX() + "," + corePos.getY() + "," + corePos.getZ() + " world=" + worldName + "]§c não é um StorageCore! Talvez alguem quebrou ele?"));
				return RESULT_SUCESS;
			}


			IBlockState blockState = chunk.getBlockState(corePos);
			BlockStorageCore block = (BlockStorageCore) blockState.getBlock();
			ItemStack heldItem = player.getHeldItem(hand);

			BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, corePos, blockState, player); // TODO: replace isSpawner with null in 1.13
			MinecraftForge.EVENT_BUS.post(event);

			if (event.isCanceled()){
				player.sendMessage(new TextComponentString("§e§l ▶ §cVocê não tem permissão para acessar esse StorageCore!"));
				return RESULT_SUCESS;
			}

			block.onBlockActivated(worldServer, corePos, blockState, player, hand, heldItem, EnumFacing.NORTH, 0, 0, 0);
			return RESULT_SUCESS;
		}

		return super.onItemRightClick(world, player, hand);
	}

	// take on a package
	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
			EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(!world.isRemote && hand == EnumHand.MAIN_HAND && !(player instanceof FakePlayer) && player.isSneaking()) {

			// get the tag compound
			NBTTagCompound stackTag = stack.hasTagCompound() ?  stack.getTagCompound() : new NBTTagCompound();

			// check if the clicked block is a valid tile
			if(world.getTileEntity(pos) instanceof TileEntityStorageCore) {

				stackTag.setBoolean("isLinked", true);
				stackTag.setString("world", world.getWorldInfo().getWorldName());
				stackTag.setIntArray("coords", new int[]{
						pos.getX(),
						pos.getY(),
						pos.getZ()
				});

				stack.setTagCompound(stackTag);

				player.sendMessage(new TextComponentString("§2§l ▶ §a[Remote Access Terminal] vinculado com sucesso!"));
			}else {
				player.sendMessage(new TextComponentString("§e§l ▶ §cVocê precisa estar olhando para um StorageCore!"));
			}
			return EnumActionResult.SUCCESS;

		}

		return EnumActionResult.PASS;
	}

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		String name = super.getItemStackDisplayName(stack);
		if(stack.hasTagCompound() && stack.getTagCompound().getBoolean("isLinked")) {
			return name + " §a(Linkado)";
		}
		return name + " §e(Não Linkado)";
    }
	
    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
		if (stack == null || !stack.hasTagCompound()) return;
		NBTTagCompound tagCompound = stack.getTagCompound();
		if(tagCompound.getBoolean("isLinked")) {
			int[] xyz = tagCompound.getIntArray("coords");
			String worldName = tagCompound.getString("world");
			tooltip.add("§e xCoord: §a" + xyz[0]);
			tooltip.add("§e yCoord: §a" + xyz[1]);
			tooltip.add("§e zCoord: §a" + xyz[2]);
			tooltip.add("§e World:   §a" + worldName);
		}
    }

}
