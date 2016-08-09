package sonar.core;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.oredict.OreDictionary;
import sonar.core.api.SonarAPI;
import sonar.core.api.nbt.INBTSyncable;
import sonar.core.energy.DischargeValues;
import sonar.core.helpers.NBTHelper.SyncType;
import sonar.core.integration.SonarLoader;
import sonar.core.integration.SonarWailaModule;
import sonar.core.integration.fmp.OLDMultipartHelper;
import sonar.core.integration.planting.FertiliserRegistry;
import sonar.core.integration.planting.HarvesterRegistry;
import sonar.core.integration.planting.PlanterRegistry;
import sonar.core.network.PacketBlockInteraction;
import sonar.core.network.PacketByteBuf;
import sonar.core.network.PacketByteBufMultipart;
import sonar.core.network.PacketByteBufString;
import sonar.core.network.PacketInvUpdate;
import sonar.core.network.PacketMultipartSync;
import sonar.core.network.PacketRequestSync;
import sonar.core.network.PacketSonarSides;
import sonar.core.network.PacketStackUpdate;
import sonar.core.network.PacketTileSync;
import sonar.core.network.PacketTileSyncUpdate;
import sonar.core.network.SonarCommon;
import sonar.core.network.utils.IByteBufTile;
import sonar.core.registries.EnergyContainerHandlerRegistry;
import sonar.core.registries.EnergyProviderRegistry;
import sonar.core.registries.EnergyTypeRegistry;
import sonar.core.registries.FluidProviderRegistry;
import sonar.core.registries.ISonarRegistryBlock;
import sonar.core.registries.ISonarRegistryItem;
import sonar.core.registries.InventoryProviderRegistry;
import sonar.core.upgrades.MachineUpgradeRegistry;

@Mod(modid = SonarCore.modid, name = "SonarCore", version = SonarCore.version)
public class SonarCore {

	public static final String modid = "SonarCore";
	public static final String version = "3.1.1";

	@SidedProxy(clientSide = "sonar.core.network.SonarClient", serverSide = "sonar.core.network.SonarCommon")
	public static SonarCommon proxy;

	@Instance(modid)
	public static SonarCore instance;

	public static InventoryProviderRegistry inventoryProviders = new InventoryProviderRegistry();
	public static FluidProviderRegistry fluidProviders = new FluidProviderRegistry();
	public static EnergyProviderRegistry energyProviders = new EnergyProviderRegistry();
	public static EnergyContainerHandlerRegistry energyContainerHandlers = new EnergyContainerHandlerRegistry();
	public static EnergyTypeRegistry energyTypes = new EnergyTypeRegistry();
	public static MachineUpgradeRegistry machineUpgrades = new MachineUpgradeRegistry();
	public static SimpleNetworkWrapper network;

	public static PlanterRegistry planters = new PlanterRegistry();
	public static HarvesterRegistry harvesters = new HarvesterRegistry();
	public static FertiliserRegistry fertilisers = new FertiliserRegistry();

	public static Logger logger = (Logger) LogManager.getLogger(modid);

	// common blocks
	public static Block reinforcedStoneBlock, reinforcedStoneBrick, reinforcedDirtBlock, reinforcedDirtBrick, stableGlass, clearStableGlass;
	public static Block[] stableStone = new Block[16], stablestonerimmedBlock = new Block[16], stablestonerimmedblackBlock = new Block[16];
	// public static Block toughenedStoneBlock, toughenedStoneBrick;
	// public static Block toughenedDirtBlock, toughenedDirtBrick;
	public static Block reinforcedStoneStairs, reinforcedStoneBrickStairs, reinforcedDirtStairs, reinforcedDirtBrickStairs;
	public static Block reinforcedStoneFence, reinforcedStoneBrickFence, reinforcedDirtFence, reinforcedDirtBrickFence;
	public static Block reinforcedStoneGate, reinforcedStoneBrickGate, reinforcedDirtGate, reinforcedDirtBrickGate;
	public static Block reinforcedStoneSlab_half, reinforcedStoneBrickSlab_half, reinforcedDirtSlab_half, reinforcedDirtBrickSlab_half;
	public static Block reinforcedStoneSlab_double, reinforcedStoneBrickSlab_double, reinforcedDirtSlab_double, reinforcedDirtBrickSlab_double;

	public static final Random rand = new Random();

	public static CreativeTabs tab = new CreativeTabs("SonarCore") {
		@Override
		public Item getTabIconItem() {
			return Item.getItemFromBlock(reinforcedStoneBlock);
		}
	};

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		logger.info("Initilising API");
		SonarAPI.init();
		logger.info("Initilised API");

		logger.info("Registering Blocks");
		SonarBlocks.registerBlocks();
		logger.info("Loaded Blocks");

		logger.info("Registering Crafting Recipes");
		SonarCrafting.registerCraftingRecipes();
		logger.info("Register Crafting Recipes");

		logger.info("Registering Renderers");
		proxy.registerRenderThings();
		logger.info("Registered Renderers");

		for (int i = 0; i < 16; i++) {
			OreDictionary.registerOre("sonarStableStone", SonarCore.stableStone[i]);
			OreDictionary.registerOre("sonarStableStone", SonarCore.stablestonerimmedBlock[i]);
			OreDictionary.registerOre("sonarStableStone", SonarCore.stablestonerimmedblackBlock[i]);
		}

	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		logger.info("Checking Loaded Mods");
		SonarLoader.initLoader();

		logger.info("Registering Packets");
		registerPackets();
		logger.info("Register Packets");

		if (SonarLoader.wailaLoaded) {
			SonarWailaModule.register();
			logger.info("Integrated with WAILA");
		} else {
			logger.warn("'WAILA' - unavailable or disabled in config");
		}
		MinecraftForge.EVENT_BUS.register(new SonarEvents());
		logger.info("Registered Events");
		energyTypes.register();
		inventoryProviders.register();
		fluidProviders.register();
		energyProviders.register();
		energyContainerHandlers.register();
		machineUpgrades.register();
		planters.register();
		harvesters.register();
		fertilisers.register();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		logger.info("Adding Discharge Values");
		DischargeValues.addValues();
		logger.info("Added " + DischargeValues.dischargeList.size() + " Discharge Values");
		for (Map.Entry<ItemStack, Integer> entry : DischargeValues.dischargeList.entrySet()) {
			logger.info("Discharge Values: " + entry.toString());
		}
		logger.info("Registered " + energyTypes.getObjects().size() + " Energy Types");
		logger.info("Registered " + inventoryProviders.getObjects().size() + " Inventory Providers");
		logger.info("Registered " + fluidProviders.getObjects().size() + " Fluid Providers");
		logger.info("Registered " + energyProviders.getObjects().size() + " Energy Providers");
		logger.info("Registered " + energyContainerHandlers.getObjects().size() + " Energy Container Providers");
		logger.info("Registered " + machineUpgrades.getMap().size() + " Machine Upgrades");
	}

	private void registerPackets() {
		if (network == null) {
			network = NetworkRegistry.INSTANCE.newSimpleChannel("Sonar-Packets");
			network.registerMessage(PacketTileSync.Handler.class, PacketTileSync.class, 0, Side.CLIENT);
			network.registerMessage(PacketSonarSides.Handler.class, PacketSonarSides.class, 1, Side.CLIENT);
			network.registerMessage(PacketRequestSync.Handler.class, PacketRequestSync.class, 2, Side.SERVER);
			network.registerMessage(PacketByteBuf.Handler.class, PacketByteBuf.class, 4, Side.CLIENT);
			network.registerMessage(PacketByteBuf.Handler.class, PacketByteBuf.class, 5, Side.SERVER);
			network.registerMessage(PacketBlockInteraction.Handler.class, PacketBlockInteraction.class, 6, Side.SERVER);
			network.registerMessage(PacketStackUpdate.Handler.class, PacketStackUpdate.class, 7, Side.CLIENT);
			network.registerMessage(PacketInvUpdate.Handler.class, PacketInvUpdate.class, 8, Side.CLIENT);
			network.registerMessage(PacketTileSyncUpdate.Handler.class, PacketTileSyncUpdate.class, 9, Side.CLIENT);
			if (SonarLoader.mcmultipartLoaded) {
				network.registerMessage(PacketMultipartSync.Handler.class, PacketMultipartSync.class, 10, Side.CLIENT);
				network.registerMessage(PacketByteBufMultipart.Handler.class, PacketByteBufMultipart.class, 11, Side.CLIENT);
				network.registerMessage(PacketByteBufMultipart.Handler.class, PacketByteBufMultipart.class, 12, Side.SERVER);
			}
		}
	}

	public static void registerItems(ArrayList<ISonarRegistryItem> items) {
		for (ISonarRegistryItem item : items) {
			GameRegistry.registerItem(item.getItem(), item.getRegistryName());
		}
	}

	public static void registerBlocks(ArrayList<ISonarRegistryBlock> blocks) {
		for (ISonarRegistryBlock block : blocks) {
			GameRegistry.registerBlock(block.getBlock(), block.getRegistryName());
			if (block.hasTileEntity()) {
				GameRegistry.registerTileEntity(block.getTileEntity(), block.getRegistryName());
			}
		}
	}

	public static void sendPacketAround(TileEntity tile, int spread, int id) {
		Object object = OLDMultipartHelper.checkObject(tile);
		if (object == null || !(object instanceof IByteBufTile)) {
			object = OLDMultipartHelper.getHandler(object);
		}
		if (object != null && object instanceof IByteBufTile) {
			if (!tile.getWorld().isRemote) {
				SonarCore.network.sendToAllAround(new PacketByteBuf((IByteBufTile) object, tile.getPos(), id), new TargetPoint(tile.getWorld().provider.getDimension(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), spread));
			} else {
				SonarCore.network.sendToServer(new PacketByteBuf((IByteBufTile) object, tile.getPos(), id));
			}
		}
	}

	public static void sendFullSyncAround(TileEntity tile, int spread) {
		if (!tile.getWorld().isRemote) {
			Object object = OLDMultipartHelper.checkObject(tile);
			if (object != null && object instanceof INBTSyncable) {
				NBTTagCompound tag = ((INBTSyncable) object).writeData(new NBTTagCompound(), SyncType.SYNC_OVERRIDE);
				if (!tag.hasNoTags()) {
					SonarCore.network.sendToAllAround(new PacketTileSync(tile.getPos(), tag), new TargetPoint(tile.getWorld().provider.getDimension(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), spread));
				}
			}
		}
	}

	public static void sendFullSyncAroundWithRenderUpdate(TileEntity tile, int spread) {
		if (!tile.getWorld().isRemote) {
			Object object = OLDMultipartHelper.checkObject(tile);
			if (object != null && object instanceof INBTSyncable) {
				NBTTagCompound tag = ((INBTSyncable) object).writeData(new NBTTagCompound(), SyncType.SYNC_OVERRIDE);
				if (!tag.hasNoTags()) {
					SonarCore.network.sendToAllAround(new PacketTileSyncUpdate(tile.getPos(), tag), new TargetPoint(tile.getWorld().provider.getDimension(), tile.getPos().getX(), tile.getPos().getY(), tile.getPos().getZ(), spread));
				}
			}
		}
	}

	public static void sendPacketToServer(TileEntity tile, int id) {
		if (tile.getWorld().isRemote)
			SonarCore.network.sendToServer(new PacketByteBuf((IByteBufTile) tile, tile.getPos(), id));
	}

	public static void sendPacketToServer(TileEntity tile, String string, int id) {
		if (tile.getWorld().isRemote)
			SonarCore.network.sendToServer(new PacketByteBufString((IByteBufTile) tile, string, tile.getPos(), id));
	}

	public static int randInt(int min, int max) {
		return rand.nextInt((max - min) + 1) + min;
	}
}
