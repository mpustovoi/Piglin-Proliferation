package tallestred.piglinproliferation.common.entities.ai.behaviors;

import com.google.common.collect.ImmutableMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import tallestred.piglinproliferation.PPMemoryModules;
import tallestred.piglinproliferation.common.entities.PiglinAlchemist;

import java.util.function.Predicate;

public class BaseThrowPotion<E extends PiglinAlchemist> extends Behavior<E> {
    protected final ItemStack itemToUse; // This should probably be a memory value in the near future
    protected final Predicate<PiglinAlchemist> canUseSelector;
    protected ItemStack potionToThrow;
    protected int ticksUntilThrow;
    protected int panicTicks;

    public BaseThrowPotion(ItemStack stack, Predicate<PiglinAlchemist> pCanUseSelector) {
        super(ImmutableMap.of(PPMemoryModules.POTION_THROW_TARGET.get(), MemoryStatus.VALUE_ABSENT, MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,  MemoryStatus.REGISTERED));
        this.itemToUse = stack;
        this.canUseSelector = pCanUseSelector;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E alchemist) {
        for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
            ItemStack stackInSlot = alchemist.beltInventory.get(slot);
            if (stackInSlot.is(itemToUse.getItem()) && PotionUtils.getPotion(itemToUse) == PotionUtils.getPotion(stackInSlot)) {
                this.potionToThrow = stackInSlot;
                return this.canUseSelector.test(alchemist);
            }
        }
        return false;
    }

    @Override
    protected void start(ServerLevel level, E alchemist, long gameTime) {
        for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
            ItemStack stackInSlot = alchemist.beltInventory.get(slot);
            if (!alchemist.isGonnaThrowPotion()) {
                if (stackInSlot.is(itemToUse.getItem()) && PotionUtils.getPotion(itemToUse) == PotionUtils.getPotion(stackInSlot) && this.canUseSelector.test(alchemist)) {
                    this.potionToThrow = stackInSlot;
                    alchemist.beltInventory.set(slot, ItemStack.EMPTY);
                    alchemist.swing(InteractionHand.OFF_HAND);
                    alchemist.setItemShownOnOffhand(stackInSlot.copy());
                    PotionUtils.setPotion(alchemist.getItemShownOnOffhand(), PotionUtils.getPotion(stackInSlot));
                    alchemist.willThrowPotion(true);
                }
            }
        }
    }

    protected void throwPotion(E alchemist) {
        if (!alchemist.getItemShownOnOffhand().isEmpty()) {
            alchemist.swing(InteractionHand.OFF_HAND);
            alchemist.throwPotion(alchemist.getItemShownOnOffhand(), alchemist.getXRot(), alchemist.getYRot());
        }
    }

    @Override
    protected void stop(ServerLevel level, E alchemist, long gameTime) {
        if (alchemist.isGonnaThrowPotion() && alchemist.getItemShownOnOffhand() != null) {
            alchemist.willThrowPotion(false);
            for (int slot = 0; slot < alchemist.beltInventory.size(); slot++) {
                ItemStack stackInSlot = alchemist.beltInventory.get(slot);
                if (stackInSlot.isEmpty()) {
                    alchemist.beltInventory.set(slot, alchemist.getItemShownOnOffhand().copy());
                    alchemist.setItemShownOnOffhand(ItemStack.EMPTY);
                }
            }
        }
    }
}