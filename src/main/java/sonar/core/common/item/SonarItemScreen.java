package sonar.core.common.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SonarItemScreen extends SonarItem {

	public abstract Block getScreenBlock();

	public abstract boolean canPlaceScreenOn(World world, IBlockState state, BlockPos pos, EnumFacing screenFacing);

	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos, facing, stack) || facing == EnumFacing.DOWN || facing == EnumFacing.UP || world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
			return EnumActionResult.PASS;
		}
		EnumFacing orientation = player.getHorizontalFacing().getOpposite();
		BlockPos adjPos = pos.offset(orientation.getOpposite());
		IBlockState adjState = world.getBlockState(adjPos);
		if (canPlaceScreenOn(world, adjState, adjPos, null)) {
			if (!world.isRemote) {
				world.setBlockState(pos, getScreenBlock().getDefaultState(), 3);
				stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;

	}
}
