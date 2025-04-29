package io.github.zaafonin.vs_oddities.mixin.create.content.kinetics.fan;

import com.simibubi.create.content.kinetics.fan.AirCurrent;
import com.simibubi.create.content.kinetics.fan.IAirCurrentSource;
import io.github.zaafonin.vs_oddities.ship.ThrustInducer;
import io.github.zaafonin.vs_oddities.util.OddUtils;
import net.fabricmc.loader.impl.lib.sat4j.core.Vec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.primitives.AABBd;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.valkyrienskies.core.api.ships.LoadedServerShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.world.RaycastUtilsKt;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(value = AirCurrent.class, remap = false)
public abstract class MixinAirCurrent {
    @Shadow
    @Final
    public IAirCurrentSource source;
    @Shadow
    public AABB bounds;
    @Shadow
    public Direction direction;
    @Shadow
    public boolean pushing;
    @Shadow
    public float maxDistance;

    @Unique
    protected Iterable<Ship> vs_oddities$caughtShips = new ArrayList<>();

    @Inject(method = "findEntities", at = @At("TAIL"))
    public void findEntities(CallbackInfo ci) {
        // Also find ships.
        Level level = source.getAirCurrentWorld();
        if (level.isClientSide) return;

        vs_oddities$caughtShips = VSGameUtilsKt.getShipsIntersecting((ServerLevel)level, bounds);
    }

    @Inject(method = "tickAffectedEntities", at = @At("TAIL"))
    public void tickAffectedEntities(Level world, CallbackInfo ci) {
        if (world.isClientSide) return;
        ServerLevel slevel = (ServerLevel)world;

        Vec3 raycastStart = source.getAirCurrentPos().getCenter();
        Vec3i v = direction.getNormal();
        Vec3 raycastEnd = raycastStart;
        raycastStart.add(new Vec3(v.getX(), v.getY(), v.getZ()).scale(0.5));
        raycastEnd.add(new Vec3(v.getX(), v.getY(), v.getZ()).scale(maxDistance));

        BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(world, new ClipContext(
                raycastStart,
                raycastEnd,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                null
        ), false, null);

        if (hit.getType() == HitResult.Type.BLOCK) {
            LoadedServerShip self = VSGameUtilsKt.getShipObjectManagingPos(slevel, source.getAirCurrentPos());
            LoadedServerShip ship = VSGameUtilsKt.getShipObjectManagingPos(slevel, hit.getBlockPos());

            if (ship != null) {
                if (self != null) {
                    if (ship.getId() == self.getId()) {
                        // Do not act if we are blowing ourselves.
                        return;
                    }
                    Vector3d fanShipPos = VectorConversionsMCKt.toJOML(source.getAirCurrentPos().getCenter());
                    fanShipPos.sub(self.getTransform().getPositionInShip());

                    // TODO: Thrust self if Clockwork is not installed.
                    ThrustInducer selfInducer = ThrustInducer.getOrCreate(self);
                }
                Vector3d hitShipPos = VectorConversionsMCKt.toJOML(hit.getBlockPos().getCenter());
                hitShipPos.sub(ship.getTransform().getPositionInShip());

                ThrustInducer shipInducer = ThrustInducer.getOrCreate(ship);

                /*
                Vec3i flow = (pushing ? direction : direction.getOpposite()).getNormal();
                float speed = Math.abs(source.getSpeed());
                float acceleration = speed / (float) (worldHitCenter.distance(worldCurrentPos) / maxDistance);
                float maxAcceleration = 5;

                double suggested = acceleration * 500;
                double cap = lsship.getInertiaData().getMass() * 25 * maxAcceleration;
                double force = Math.min(suggested, cap);
                if (!lsship.isStatic()) {
                    inducer.applyPulse(
                            worldCurrentPos.sub(worldHitCenter, new Vector3d()).normalize(force).mul(pushing ? -1.0 : 1.0),
                            null, shipHitCenter, 3, true
                    );
                }*/
            }
        }

        /*
        ServerShip fanShip = VSGameUtilsKt.getShipManagingPos((ServerLevel)world, source.getAirCurrentPos());
        Vector3d fanCenter = VectorConversionsMCKt.toJOML(source.getAirCurrentPos().getCenter());
        Vector3dc worldCurrentPos = VSGameUtilsKt.getWorldCoordinates(world, source.getAirCurrentPos(), fanCenter);
        AABBd worldBounds = VectorConversionsMCKt.toJOML(bounds);
        if (fanShip != null) {
            worldBounds.transform(fanShip.getTransform().getShipToWorld());
        }

        // Apply forces to ships.
        vs_oddities$caughtShips.forEach(ship -> {
            // Only safe because we skip the ship check on client.
            ServerShip sship = (ServerShip)ship;
            AABBd worldIntersection = sship.getWorldAABB().intersection(worldBounds, new AABBd());
            Vector3dc worldIntersectionCenter = worldIntersection.center(new Vector3d());

            if (!worldIntersectionCenter.isFinite()) return;

            System.out.printf("Intersecting %s, center: %s\n", sship.getSlug(), worldIntersectionCenter.toString());

            Ship self = VSGameUtilsKt.getShipManagingPos(world, source.getAirCurrentPos());

            Vector3d worldRaycastStart = worldCurrentPos.add(worldIntersectionCenter.sub(worldCurrentPos, new Vector3d()).normalize(0.5), new Vector3d());
            Vector3d worldRaycastEnd = worldIntersectionCenter.add(worldIntersectionCenter.sub(worldCurrentPos, new Vector3d()).normalize(1), new Vector3d());
            BlockHitResult hit = RaycastUtilsKt.clipIncludeShips(world, new ClipContext(
                    VectorConversionsMCKt.toMinecraft(worldRaycastStart),
                    VectorConversionsMCKt.toMinecraft(worldRaycastEnd),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    null
            ), false, (self != null ? self.getId() : null));

            if (hit.getType() == HitResult.Type.BLOCK) {
                System.out.printf("Successful raycast to %s", hit.getBlockPos().toString());
                Vector3d shipyardHitCenter = VectorConversionsMCKt.toJOML(hit.getBlockPos().getCenter());
                Vector3dc worldHitCenter = VSGameUtilsKt.getWorldCoordinates(world, hit.getBlockPos(), shipyardHitCenter);
                Vector3d shipHitCenter = VectorConversionsMCKt.toJOML(hit.getBlockPos().getCenter());
                shipHitCenter.sub(sship.getTransform().getPositionInShip());

                LoadedServerShip lsship = VSGameUtilsKt.getShipObjectManagingPos((ServerLevel)world, hit.getBlockPos());
                if (lsship != null) {
                    if (lsship.getId() != ship.getId()) return;

                    ThrustInducer inducer = ThrustInducer.getOrCreate(lsship);

                    Vec3i flow = (pushing ? direction : direction.getOpposite()).getNormal();
                    float speed = Math.abs(source.getSpeed());
                    float acceleration = speed / (float) (worldHitCenter.distance(worldCurrentPos) / maxDistance);
                    float maxAcceleration = 5;

                    double suggested = acceleration * 500;
                    double cap = lsship.getInertiaData().getMass() * 25 * maxAcceleration;
                    double force = Math.min(suggested, cap);
                    if (!lsship.isStatic()) {
                        inducer.applyPulse(
                                worldCurrentPos.sub(worldHitCenter, new Vector3d()).normalize(force).mul(pushing ? -1.0 : 1.0),
                                null, shipHitCenter, 3, true
                        );
                    }
                }
            }
        });*/
    }
}
