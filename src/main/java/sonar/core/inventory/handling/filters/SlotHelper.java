package sonar.core.inventory.handling.filters;

import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import sonar.core.api.SonarAPI;
import sonar.core.energy.DischargeValues;
import sonar.core.api.inventories.ISonarInventory;
import sonar.core.inventory.handling.EnumFilterType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Predicate;

public class SlotHelper {

    public static IInsertFilter filterSlot(int slot, Predicate<ItemStack> filter){
        return (SLOT,STACK,FACE) -> SLOT == slot ? filter.test(STACK) : null;
    }

    public static IInsertFilter blockSlot(int slot){
        return (SLOT,STACK,FACE) -> SLOT == slot ? false : null;
    }

    public static IInsertFilter chargeSlot(int slot){
        return (SLOT,STACK,FACE) -> SLOT == slot ? SlotHelper.chargeSlot(STACK) : null;
    }

    public static boolean chargeSlot(ItemStack stack){
        return SonarAPI.getEnergyHelper().canTransferEnergy(stack) != null;
    }

    public static IInsertFilter dischargeSlot(int slot){
        return (SLOT,STACK,FACE) -> SLOT == slot ? SlotHelper.dischargeSlot(STACK) : null;
    }

    public static boolean dischargeSlot(ItemStack stack){
        return DischargeValues.getValueOf(stack) > 0 || SonarAPI.getEnergyHelper().canTransferEnergy(stack) != null;
    }


    public static boolean checkInsert(int slot, @Nonnull ItemStack stack, @Nullable EnumFacing face, EnumFilterType internal, ISonarInventory inv, boolean def){
        boolean validFilters = false;
        boolean insert = def;
        for(Map.Entry<IInsertFilter, EnumFilterType> filter : inv.getInsertFilters().entrySet()){
            if(!filter.getValue().matches(internal)){
                continue;
            }
            validFilters = true;
            Boolean result = filter.getKey().canInsert(slot, stack, face);
            if(result != null){
                if(!result){
                    return false;
                }
                insert = true;
            }
        }
        if(!validFilters){
            return true;
        }
        return insert;
    }

    public static boolean checkExtract(int slot, int count, @Nullable EnumFacing face, EnumFilterType internal, ISonarInventory inv, boolean def){
        boolean validFilters = false;
        boolean extract = def;
        for(Map.Entry<IExtractFilter, EnumFilterType> filter : inv.getExtractFilters().entrySet()){
            if(!filter.getValue().matches(internal)){
                continue;
            }
            validFilters = true;
            Boolean result = filter.getKey().canExtract(slot, count, face);
            if(result != null){
                if(!result){
                    return false;
                }
                extract = true;
            }
        }
        if(!validFilters){
            return true;
        }
        return extract;
    }
}
