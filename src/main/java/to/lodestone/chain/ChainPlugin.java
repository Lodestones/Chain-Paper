package to.lodestone.chain;

import dev.jorel.commandapi.CommandAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityUnleashEvent;
import org.bukkit.plugin.java.JavaPlugin;
import to.lodestone.bookshelfapi.api.Configuration;
import to.lodestone.bookshelfapi.api.Task;
import to.lodestone.bookshelfapi.api.util.LocationUtil;
import to.lodestone.chainapi.ChainAPI;
import to.lodestone.chainapi.IChainAPI;

public final class ChainPlugin extends JavaPlugin implements Listener, IChainAPI {

    private Configuration data;
    private Configuration config;
    private ChainManager chainManager;


//    @EventHandler
//    public void on(EntityMoveEvent event) {
//        LivingEntity entity = event.getEntity();
//        if (entity.getPersistentDataContainer().has(FAKE_KEY, PersistentDataType.STRING)) {
//            String allayId = entity.getPersistentDataContainer().get(FAKE_KEY, PersistentDataType.STRING);
//            if (allayId != null) {
//                Allay fakeLeash = (Allay) Bukkit.getEntity(UUID.fromString(allayId));
//                if (fakeLeash != null) {
//                    fakeLeash.teleport(entity.getLocation());
//                }
//            }
//        }
//
//        if (entity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING)) {
//            String uniqueId = entity.getPersistentDataContainer().get(CHAINED_KEY, PersistentDataType.STRING);
//            if (uniqueId == null) return;
//            LivingEntity livingEntity = (LivingEntity) Bukkit.getEntity(UUID.fromString(uniqueId));
//            if (livingEntity == null || !livingEntity.getPersistentDataContainer().has(CHAINED_KEY, PersistentDataType.STRING))
//                return;
//
//            if (livingEntity.getWorld().getName().equalsIgnoreCase(entity.getWorld().getName()) && livingEntity.getLocation().distance(entity.getLocation()) > Math.min(8, Math.max(2, this.config.getInt("max_distance", 6)))) {
//                entity.setVelocity(LocationUtil.getDirection(entity.getLocation(), livingEntity.getLocation()).multiply(0.42));
//                livingEntity.setLeashHolder(entity);
//            }
//        }
//    }

    @EventHandler
    public void on(EntityUnleashEvent event) {
        switch (event.getReason()) {
            case UNKNOWN -> event.setDropLeash(false);
            case DISTANCE -> {
                // This should be a fallback? To ensure that the player is always chained to the entity no matter what.
                ChainData chainData = getChainManager().getChainedEntities().stream().filter(data -> data.isChained(event.getEntity())).findFirst().orElse(null);
                if (chainData != null) {
                    Task.later(this, () -> {
                        ((LivingEntity) event.getEntity()).setLeashHolder(Bukkit.getEntity(chainData.getOpposite(event.getEntity())));
                        event.getEntity().setVelocity(LocationUtil.getDirection(event.getEntity().getLocation(), Bukkit.getEntity(chainData.getOpposite(event.getEntity())).getLocation()).multiply(0.5));
                    }, 1L);

                    event.setDropLeash(false);
                }
            }
            case PLAYER_UNLEASH -> {
                ChainData chainData = getChainManager().getChainedEntities().stream().filter(data -> data.isChained(event.getEntity())).findFirst().orElse(null);
                if (chainData != null) event.setCancelled(true);
            }
        }
    }

    @Override
    public void onLoad() {
        this.config = new Configuration(this, "config.yml");
        this.config.initialize();

        this.data = new Configuration(this, "data.yml");
        this.data.initialize();

        ChainAPI.setApi(this);
    }

    @Override
    public void onEnable() {
        this.chainManager = new ChainManager(this);

        new ChainCommand(this).register();
        new UnchainCommand(this).register();

        getServer().getPluginManager().registerEvents(this, this);
    }

    public ChainManager getChainManager() {
        return chainManager;
    }

    public Configuration data() {
        return data;
    }

    public Configuration config() {
        return config;
    }

    @Override
    public void onDisable() {
        CommandAPI.unregister("chain");
        CommandAPI.unregister("unchain");

        this.data.save();
    }

    @Override
    public void chain(LivingEntity victimOne, LivingEntity victimTwo) {
        getChainManager().getChainedEntities().add(new ChainData(this, victimOne.getUniqueId(), victimTwo.getUniqueId()));
        getLogger().warning("Chained " + victimOne.getName() + " to " + victimTwo.getName());
    }

    @Override
    public void unchain(LivingEntity livingEntity) {
        getLogger().warning("Unchained " + livingEntity.getName());
        getChainManager().getChainedEntities().removeIf(chainData -> {
            boolean flag = chainData.isChained(livingEntity);
            if (flag) chainData.unload();
            return flag;
        });
    }

    @Override
    public boolean isChained(LivingEntity livingEntity) {
        return getChainManager().getChainedEntities().stream().anyMatch(chainData -> chainData.isChained(livingEntity));
    }
}
