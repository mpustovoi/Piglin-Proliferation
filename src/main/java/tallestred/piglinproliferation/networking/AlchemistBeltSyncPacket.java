package tallestred.piglinproliferation.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class AlchemistBeltSyncPacket {
    final int slotId;
    final ItemStack stack;
    private final int entityId;

    public AlchemistBeltSyncPacket(int entityId, int slotID, ItemStack stack) {
        this.entityId = entityId;
        this.slotId = slotID;
        this.stack = stack;
    }

    public static AlchemistBeltSyncPacket decode(FriendlyByteBuf buf) {
        return new AlchemistBeltSyncPacket(buf.readInt(), buf.readInt(), buf.readItem());
    }

    public static void encode(AlchemistBeltSyncPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.entityId);
        buf.writeInt(msg.slotId);
        buf.writeItem(msg.stack);
    }

    public static void handle(AlchemistBeltSyncPacket msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerToClientPacketStuff.syncBelt(msg);
        });
        context.get().setPacketHandled(true);
    }

    public int getEntityId() {
        return this.entityId;
    }
}