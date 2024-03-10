package tallestred.piglinproliferation.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import tallestred.piglinproliferation.common.entities.PiglinTraveller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CompassCanFindLocationCondition implements LootItemCondition {
    @Override
    public LootItemConditionType getType() {
        return PPLoot.COMPASS_CAN_FIND_LOCATION.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        if (lootContext.getParam(LootContextParams.THIS_ENTITY) instanceof PiglinTraveller traveller) {
            ServerLevel level = lootContext.getLevel();
            List<CompassLocationMap.SearchObject> objectsToSearch = CompassLocationMap.objectsToSearch(level);
            Collections.shuffle(objectsToSearch);
            for (CompassLocationMap.SearchObject object : objectsToSearch) {
                if (!traveller.alreadyLocatedObjects.containsKey(object) && !object.entityAtObjectType(traveller)) {
                    BlockPos pos = object.locateObject(level, traveller.getOnPos());
                    if (pos != null) {
                        traveller.currentlyLocatedObject = Map.entry(object, pos);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<CompassCanFindLocationCondition> {
        @Override
        public void serialize(JsonObject jsonObject, CompassCanFindLocationCondition compassCanFindLocationCondition, JsonSerializationContext jsonSerializationContext) {
        }

        @Override
        public CompassCanFindLocationCondition deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext) {
            return new CompassCanFindLocationCondition();
        }
    }
}