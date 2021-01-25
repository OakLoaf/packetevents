/*
 * MIT License
 *
 * Copyright (c) 2020 retrooper
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.retrooper.packetevents.packetwrappers.play.out.animation;

import io.github.retrooper.packetevents.packettype.PacketTypeClasses;
import io.github.retrooper.packetevents.packetwrappers.NMSPacket;
import io.github.retrooper.packetevents.packetwrappers.SendableWrapper;
import io.github.retrooper.packetevents.packetwrappers.WrappedPacket;
import io.github.retrooper.packetevents.utils.nms.NMSUtils;
import org.bukkit.entity.Entity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

public final class WrappedPacketOutAnimation extends WrappedPacket implements SendableWrapper {
    private static Constructor<?> animationConstructor;
    private Entity entity;
    private int entityID = -1;
    private EntityAnimationType type;

    public WrappedPacketOutAnimation(final NMSPacket packet) {
        super(packet);
    }

    public WrappedPacketOutAnimation(final Entity target, final EntityAnimationType type) {
        this.entityID = target.getEntityId();
        this.entity = target;
        this.type = type;
    }

    public WrappedPacketOutAnimation(final int entityID, final EntityAnimationType type) {
        this.entityID = entityID;
        this.entity = null;
        this.type = type;
    }

    @Override
    protected void load() {
        Class<?> animationClass = PacketTypeClasses.Play.Server.ANIMATION;

        try {
            animationConstructor = animationClass.getConstructor(NMSUtils.nmsEntityClass, int.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    /**
     * Lookup the associated entity by the ID that was sent in the packet.
     *
     * @return Entity
     */
    public Entity getEntity() {
        if (entity != null) {
            return entity;
        }
        return entity = NMSUtils.getEntityById(getEntityId());
    }

    /**
     * Get the ID of the entity.
     * If you do not want to use {@link #getEntity()},
     * you lookup the entity by yourself with this entity ID.
     *
     * @return Entity ID
     */
    public int getEntityId() {
        if (entityID == -1) {
            entityID = readInt(0);
        }
        return entityID;
    }

    /**
     * Get the entity animation type.
     *
     * @return Get Entity Animation Type
     */
    public EntityAnimationType getAnimationType() {
        if (packet != null) {
            byte id = (byte)readInt(1);
            return EntityAnimationType.getById(id);
        } else {
            return type;
        }
    }

    @Override
    public Object asNMSPacket() {
        final Object nmsEntity = NMSUtils.getNMSEntity(getEntity());
        final int index = getAnimationType().id;
        try {
            return animationConstructor.newInstance(nmsEntity, index);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public enum EntityAnimationType {
        SWING_MAIN_ARM((byte) 0), TAKE_DAMAGE((byte) 1), LEAVE_BED((byte) 2),
        SWING_OFFHAND((byte) 3), CRITICAL_EFFECT((byte) 4), MAGIC_CRITICAL_EFFECT((byte) 5);

        final byte id;

        EntityAnimationType(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static EntityAnimationType getById(byte id) {
            return values()[id]; //id is at the same time the index
        }
    }
}
