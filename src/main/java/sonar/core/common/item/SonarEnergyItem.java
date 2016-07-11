package sonar.core.common.item;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import sonar.core.helpers.FontHelper;
import sonar.core.network.sync.SyncEnergyStorage;
import sonar.core.network.sync.SyncItemEnergyStorage;
import cofh.api.energy.IEnergyContainerItem;

public class SonarEnergyItem extends SonarItem implements IEnergyContainerItem {

	public SyncItemEnergyStorage storage;
	public int capacity, maxReceive, maxExtract;

	public SonarEnergyItem(int capacity, int maxReceive, int maxExtract) {
		super();
		this.capacity = capacity;
		this.maxReceive = maxReceive;
		this.maxExtract = maxExtract;
		storage = new SyncItemEnergyStorage(null, capacity, maxReceive, maxExtract);
		setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean par4) {
		super.addInformation(stack, player, list, par4);
		list.add(FontHelper.translate("energy.stored") + ": " + getEnergyStored(stack) + " RF");
	}

	@Override
	public int receiveEnergy(ItemStack container, int maxReceive, boolean simulate) {
		return storage.receiveEnergy(maxReceive, simulate);
	}

	@Override
	public int extractEnergy(ItemStack container, int maxExtract, boolean simulate) {
		return storage.extractEnergy(maxExtract, simulate);
	}

	@Override
	public int getEnergyStored(ItemStack container) {
		return storage.getEnergyStored();
	}

	@Override
	public int getMaxEnergyStored(ItemStack container) {
		return storage.getMaxEnergyStored();
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		storage = new SyncItemEnergyStorage(null, capacity, maxReceive, maxExtract);
		return storage;
	}

}