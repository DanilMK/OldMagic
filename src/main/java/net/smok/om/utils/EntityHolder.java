package net.smok.om.utils;

import com.mojang.serialization.Codec;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.smok.om.Debug;

import java.util.UUID;

public class EntityHolder {

    public static final Codec<EntityHolder> CODEC = UUIDUtil.CODEC.xmap(EntityHolder::new, EntityHolder::getUuid);

    private Entity entity;
    private final UUID uuid;

    public EntityHolder(UUID uuid) {
        this.uuid = uuid;
    }

    public EntityHolder(Entity entity) {
        this.entity = entity;
        this.uuid = entity.getUUID();
    }

    public Entity getEntity() {
        return entity;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void init(ServerLevel level) {
        this.entity = level.getEntity(uuid);
    }


}
