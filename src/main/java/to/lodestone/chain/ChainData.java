package to.lodestone.chain;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;
import to.lodestone.chain.ChainPlugin;

import java.util.UUID;

public class ChainData {

    private final JavaPlugin plugin;
    private final UUID mainEntity;
    private final UUID attachedEntity;

    @Nullable
    private Vex fakeLeash; // This is only used if the entity selected cannot show a leash.
    private Vex otherFakeLeash; // This is only used if the entity selected cannot show a leash.

    public ChainData(JavaPlugin plugin, UUID mainEntity, UUID attachedEntity) {
        this.mainEntity = mainEntity;
        this.plugin = plugin;
        this.attachedEntity = attachedEntity;
    }

    public void loadFakeLeash(Player entity, Player attachedEntity) {
        this.unload();

        // Entity is attached to the attachedEntity's vex, hide the vex from all players except the entity.
        fakeLeash = attachedEntity.getWorld().spawn(attachedEntity.getLocation().clone().add(0, 0.2, 0), Vex.class, vex -> {
            vex.setLeashHolder(attachedEntity);
            vex.setInvulnerable(true);
            vex.setInvisible(true);
            vex.setCollidable(false);
            vex.getCollidableExemptions().add(entity.getUniqueId());
            vex.getCollidableExemptions().add(attachedEntity.getUniqueId());
            vex.getEquipment().clear();
            vex.setSilent(true);
            vex.setAI(false);
            vex.setCharging(false);
            vex.setLimitedLifetime(false);
            vex.setCanPickupItems(false);
            plugin.getServer().getOnlinePlayers().forEach(v -> {
                if (entity.getUniqueId() == v.getUniqueId()) return;
                v.hideEntity(plugin, vex);
            });
        });

        // Entity is attached to the entity's vex, hide the vex from the attachedEntity.
        otherFakeLeash = attachedEntity.getWorld().spawn(attachedEntity.getLocation().clone().add(0, 0.2, 0), Vex.class, vex -> {
            vex.setLeashHolder(entity);
            vex.setInvulnerable(true);
            vex.setInvisible(true);
            vex.setCollidable(false);
            vex.getCollidableExemptions().add(entity.getUniqueId());
            vex.getCollidableExemptions().add(attachedEntity.getUniqueId());
            vex.getEquipment().clear();
            vex.setSilent(true);
            vex.setAI(false);
            vex.setCharging(false);
            vex.setLimitedLifetime(false);
            vex.setCanPickupItems(false);
            entity.hideEntity(plugin, vex);
        });
    }

    public boolean isChained(Entity entity) {
        return entity.getUniqueId().equals(mainEntity) || entity.getUniqueId().equals(attachedEntity);
    }

    public boolean isAttached(Entity entity) {
        return entity.getUniqueId().equals(attachedEntity);
    }

    public void unload() {
        if (this.fakeLeash != null)
            this.fakeLeash.remove();

        if (this.otherFakeLeash != null)
            this.otherFakeLeash.remove();
    }

    public void tick(ChainPlugin plugin) {
        @Nullable Entity entity = Bukkit.getEntity(mainEntity);
        @Nullable Entity attached = Bukkit.getEntity(attachedEntity);
        if (entity == null || attached == null || attached.isDead() || entity.isDead() || entity.getWorld() != attached.getWorld() || !(entity instanceof LivingEntity) || !(attached instanceof LivingEntity)) {
            unload();
            return;
        }

        if (entity instanceof Player mainPlayer && attached instanceof Player secondPlayer) {
            if (this.fakeLeash == null || this.fakeLeash.isDead() || !this.fakeLeash.isValid() || this.otherFakeLeash == null || this.otherFakeLeash.isDead() || !this.otherFakeLeash.isValid())
                this.loadFakeLeash(mainPlayer, secondPlayer);

            this.fakeLeash.setLeashHolder(entity);
            this.fakeLeash.teleport(attached.getLocation().clone().add(0, 0.2, 0));

            this.otherFakeLeash.setLeashHolder(attached);
            this.otherFakeLeash.teleport(entity.getLocation().clone().add(0, 0.2, 0));
        }

        // If the entities are too far apart, pull them together.
        if (entity.getWorld().getName().equalsIgnoreCase(attached.getWorld().getName()) && entity.getLocation().distance(attached.getLocation()) > Math.max(plugin.config().getInt("max_distance"), 2)) {
            entity.setVelocity(attached.getLocation().toVector().subtract(entity.getLocation().toVector()).normalize().multiply(0.42));
            attached.setVelocity(entity.getLocation().toVector().subtract(attached.getLocation().toVector()).normalize().multiply(0.42));
            if (!((LivingEntity) attached).isLeashed() || ((LivingEntity) attached).getLeashHolder() != entity) ((LivingEntity) attached).setLeashHolder(entity);
        }

//        // If the entities are really far apart, teleport them together.
//        if (entity.getWorld().getName().equalsIgnoreCase(attached.getWorld().getName()) && entity.getLocation().distance(attached.getLocation()) > Math.max(2, plugin.config().getInt("max_distance") * 5)) {
//            attached.teleport(entity);
//        }
    }

    public UUID getAttachedEntity() {
        return attachedEntity;
    }

    public UUID getMainEntity() {
        return mainEntity;
    }

    public UUID getOpposite(Entity entity) {
        if (entity.getUniqueId() == mainEntity)
            return attachedEntity;
        else
            return mainEntity;
    }
}
