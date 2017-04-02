package sonar.core.handlers.inventories;

import java.util.List;

import crazypants.enderio.conduit.TileConduitBundle;
import crazypants.enderio.conduit.item.ItemConduit;
import crazypants.enderio.conduit.item.ItemConduitNetwork;
import crazypants.enderio.conduit.item.NetworkedInventory;
import crazypants.enderio.machine.invpanel.chest.TileInventoryChest;
import crazypants.enderio.machine.invpanel.server.InventoryDatabaseServer;
import crazypants.enderio.machine.invpanel.server.ItemEntry;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import sonar.core.api.SonarAPI;
import sonar.core.api.StorageSize;
import sonar.core.api.asm.InventoryHandler;
import sonar.core.api.inventories.StoredItemStack;
import sonar.core.api.utils.ActionType;

@InventoryHandler(modid = "EnderIO", handlerID = EnderIOInvSystemHandler.name, priority = -1)
public class EnderIOInvSystemHandler extends ItemHandlerHandler {

	public static final String name = "Ender-IO-System";

	public boolean canHandleItems(TileEntity tile, EnumFacing dir) {
		return tile instanceof TileConduitBundle || tile instanceof TileInventoryChest;
	}

	public static ItemConduitNetwork getNetworkFromTile(TileEntity tile) {
		// just in case other ENDER IO blocks could be connected
		if (tile instanceof TileConduitBundle) {
			return getNetworkFromBundle((TileConduitBundle) tile);
		}
		if (tile instanceof TileInventoryChest) {
			for (EnumFacing face : EnumFacing.VALUES) {
				BlockPos pos = tile.getPos().offset(face);
				TileEntity bundle = tile.getWorld().getTileEntity(pos);
				if (bundle != null && bundle instanceof TileConduitBundle) {
					return getNetworkFromBundle((TileConduitBundle) bundle);
				}
			}
		}
		return null;
	}

	public static ItemConduitNetwork getNetworkFromBundle(TileConduitBundle bundle) {
		ItemConduitNetwork network = null;
		ItemConduit conduit = bundle.getConduit(ItemConduit.class);
		if (conduit != null) {
			network = (ItemConduitNetwork) conduit.getNetwork();
		}
		return network;
	}

	public StoredItemStack getStack(int slot, TileEntity tile, EnumFacing dir) {
		ItemConduitNetwork network = getNetworkFromTile(tile);
		if (network != null) {
			List<NetworkedInventory> invs = network.getInventoryPanelSources();
			for (NetworkedInventory inv : invs) {
				IItemHandler handler = inv.getInventory();
				if (handler != null) {
					return ItemHandlerHandler.getStack(slot, handler, inv.getInventorySide());
				}
			}
		}
		return null;
	}

	public StorageSize getItems(List<StoredItemStack> storedStacks, TileEntity tile, EnumFacing dir) {
		ItemConduitNetwork network = getNetworkFromTile(tile);
		StorageSize networkSize = new StorageSize(0, 0);
		if (network != null) {
			List<NetworkedInventory> invs = network.getInventoryPanelSources();
			for (NetworkedInventory inv : invs) {
				IItemHandler handler = inv.getInventory();
				if (handler != null) {
					networkSize.add(SonarAPI.getItemHelper().addItemHandlerToList(storedStacks, handler));
				}
			}
		}
		return networkSize;
	}

	public StoredItemStack addStack(StoredItemStack add, TileEntity tile, EnumFacing dir, ActionType action) {
		ItemConduitNetwork network = getNetworkFromTile(tile);
		if (network != null) {

			List<NetworkedInventory> invs = network.getInventoryPanelSources();
			for (NetworkedInventory inv : invs) {
				IItemHandler handler = inv.getInventory();
				if (handler != null) {
					add = ItemHandlerHandler.addStack(add, handler, inv.getInventorySide(), action);
					if (add == null) {
						return null;
					}
				}
			}
		}
		return add;
	}

	public StoredItemStack removeStack(StoredItemStack remove, TileEntity tile, EnumFacing dir, ActionType action) {
		ItemConduitNetwork network = getNetworkFromTile(tile);
		if (network != null) {
			List<NetworkedInventory> invs = network.getInventoryPanelSources();
			for (NetworkedInventory inv : invs) {
				IItemHandler handler = inv.getInventory();
				if (handler != null) {
					remove = ItemHandlerHandler.removeStack(remove, handler, inv.getInventorySide(), action);
					if (remove == null) {
						return null;
					}
				}
			}
		}
		return remove;
	}
}