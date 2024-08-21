package to.lodestone.chain;

import dev.jorel.commandapi.CommandAPI;
import io.papermc.paper.event.entity.EntityMoveEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Allay;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Team;
import to.lodestone.bookshelfapi.api.Configuration;
import to.lodestone.bookshelfapi.api.Task;
import to.lodestone.bookshelfapi.api.util.LocationUtil;

import java.util.UUID;

public final class ChainPlugin extends JavaPlugin implements Listener {

    public static NamespacedKey CHAINED_KEY;
    public static NamespacedKey FAKE_KEY;
    private Configuration config;

    @EventHandler
    public void on(PlayerMoveEvent event) {
        LivingEntity entity = event.getPlayer();
        Vex fakeLeash = null;
        if (entity.getPersistentDataContainer().has(FAKE_KEY, PersistentDataType.STRING)) {
            String allayId = entity.getPersistentDataContainer().get(FAKE_KEY, PersistentDataType.STRING);
            if (allayId != null) {
                fakeLeash = (Vex) Bukkit.getEntity(UUID.fromString(allayId));
                if (fakeLeash != null) {
                    if (entity.getPersistentDataContainer().has(CHAINED_KEY)) {
                        fakeLeash.teleport(entity.getLocation().clone().add(0, 0.2, 0));
                    } else {
                        fakeLeash.remove();
                        entity.getPersistentDataContainer().remove(FAKE_KEY);
                    }
                } else if (!entity.getPersistentDataContainer().has(CHAINED_KEY)) {
                    entity.getPersistentDataContainer().remove(FAKE_KEY);
                }
            } else if (!entity.getPersistentDataContainer().has(CHAINED_KEY)) {
                entity.getPersistentDataContainer().remove(FAKE_KEY);
            }
        }

        if (entity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
            String uniqueId = entity.getPersistentDataContainer().get(CHAINED_KEY, PersistentDataType.STRING);
            if (uniqueId == null) return;
            LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(UUID.fromString(uniqueId));
            if (livingEntity == null || !livingEntity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING))
                return;

            if (fakeLeash != null)
                fakeLeash.setLeashHolder(livingEntity);

            if (livingEntity.getWorld().getName().equalsIgnoreCase(entity.getWorld().getName()) && livingEntity.getLocation().distance(entity.getLocation()) > Math.min(8, Math.max(2, this.config.getInt("max_distance", 6)))) {
                entity.setVelocity(LocationUtil.getDirection(entity.getLocation(), livingEntity.getLocation()).multiply(0.42));
                livingEntity.setLeashHolder(entity);
            }

            if (livingEntity instanceof Player player) {
                if (player.getPersistentDataContainer().has(FAKE_KEY, PersistentDataType.STRING)) {
                    String allayId = player.getPersistentDataContainer().get(FAKE_KEY, PersistentDataType.STRING);
                    if (allayId != null && Bukkit.getEntity(UUID.fromString(allayId)) != null) return;
                }

                if (entity.getPersistentDataContainer().has(FAKE_KEY, PersistentDataType.STRING)) {
                    String allayId = entity.getPersistentDataContainer().get(FAKE_KEY, PersistentDataType.STRING);
                    if (allayId != null && Bukkit.getEntity(UUID.fromString(allayId)) != null) return;
                }

                fakeLeash = player.getWorld().spawn(player.getLocation().clone().add(0, 0.2, 0), Vex.class, vex -> {
                    Team fakeCollisionTeam = player.getScoreboard().getTeam(player.getUniqueId() + "_collision_team");
                    if (fakeCollisionTeam == null) fakeCollisionTeam = player.getScoreboard().registerNewTeam(player.getUniqueId() + "_collision_team");

                    fakeCollisionTeam.addEntry(vex.getUniqueId().toString());
                    fakeCollisionTeam.addEntry(player.getName());
                    fakeCollisionTeam.addEntry(entity.getUniqueId().toString());
                    fakeCollisionTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.FOR_OTHER_TEAMS);
                    fakeCollisionTeam.setCanSeeFriendlyInvisibles(false);

                    vex.setLeashHolder(entity);
                    vex.setInvulnerable(true);
                    vex.setInvisible(true);
                    vex.getEquipment().clear();
                    vex.setSilent(true);
                    vex.setAI(false);
                    vex.setCharging(false);
                    vex.setLimitedLifetime(false);
                    vex.setCanPickupItems(false);
                });

                player.getPersistentDataContainer().set(FAKE_KEY, PersistentDataType.STRING, fakeLeash.getUniqueId().toString());
            }
        }
    }

    @EventHandler
    public void on(EntityMoveEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.getPersistentDataContainer().has(FAKE_KEY, PersistentDataType.STRING)) {
            String allayId = entity.getPersistentDataContainer().get(FAKE_KEY, PersistentDataType.STRING);
            if (allayId != null) {
                Allay fakeLeash = (Allay) Bukkit.getEntity(UUID.fromString(allayId));
                if (fakeLeash != null) {
                    fakeLeash.teleport(entity.getLocation());
                }
            }
        }

        if (entity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
            String uniqueId = entity.getPersistentDataContainer().get(CHAINED_KEY, PersistentDataType.STRING);
            if (uniqueId == null) return;
            LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(UUID.fromString(uniqueId));
            if (livingEntity == null || !livingEntity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING))
                return;

            if (livingEntity.getWorld().getName().equalsIgnoreCase(entity.getWorld().getName()) && livingEntity.getLocation().distance(entity.getLocation()) > Math.min(8, Math.max(2, this.config.getInt("max_distance", 6)))) {
                entity.setVelocity(LocationUtil.getDirection(entity.getLocation(), livingEntity.getLocation()).multiply(0.42));
                livingEntity.setLeashHolder(entity);
            }
        }
    }

    @EventHandler
    public void on(EntityUnleashEvent event) {
        switch (event.getReason()) {
            case UNKNOWN -> event.setDropLeash(false);
            case DISTANCE -> {
                // This should be a fallback? To ensure that the player is always chained to the entity no matter what.
                if (event.getEntity().getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
                    String uniqueId = event.getEntity().getPersistentDataContainer().get(CHAINED_KEY, PersistentDataType.STRING);
                    if (uniqueId == null) return;
                    LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(UUID.fromString(uniqueId));
                    if (livingEntity == null || !livingEntity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING))
                        return;

                    Task.later(this, () -> {
                        ((LivingEntity) event.getEntity()).setLeashHolder(livingEntity);
                        livingEntity.setVelocity(LocationUtil.getDirection(livingEntity.getLocation(), event.getEntity().getLocation()).multiply(0.5));
                    }, 1L);

                    event.setDropLeash(false);
                    if (!livingEntity.getWorld().getName().equalsIgnoreCase(event.getEntity().getWorld().getName()) || livingEntity.getLocation().distance(event.getEntity().getLocation()) > 30)
                        event.getEntity().getPersistentDataContainer().remove(CHAINED_KEY);
                }
            }
            case HOLDER_GONE -> {
                if (event.getEntity().getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
                    event.getEntity().getPersistentDataContainer().remove(CHAINED_KEY);
                    event.setDropLeash(false);
                }
            }
            case PLAYER_UNLEASH -> {
                if (event.getEntity().getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    event.setDropLeash(false);
                }
            }
        }
    }

    @EventHandler
    public void on(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.LEAD)
            event.setCancelled(true);
    }

    @Override
    public void onLoad() {
        this.config = new Configuration(this, "config.yml");
        this.config.initialize();
    }

    @Override
    public void onEnable() {
        CHAINED_KEY = new NamespacedKey(this, "chained_to");
        FAKE_KEY = new NamespacedKey(this, "fake_leash");

        new ChainCommand(this).register();
        new UnchainCommand().register();

        getServer().getPluginManager().registerEvents(this, this);
    }

    public Configuration config() {
        return config;
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister("chain");
        CommandAPI.unregister("unchain");
    }
}
