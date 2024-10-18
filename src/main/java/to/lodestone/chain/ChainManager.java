package to.lodestone.chain;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vex;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class ChainManager extends BukkitRunnable implements Listener {
    private final ChainPlugin plugin;
    private final List<ChainData> chainedEntities = new ArrayList<>();

    public ChainManager(ChainPlugin plugin) {
        this.plugin = plugin;

        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        this.runTaskTimer(plugin, 0, 1);
    }

    @EventHandler
    public void on(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        if (event.getTo().getWorld().getEnvironment() != event.getFrom().getWorld().getEnvironment()) {
            chainedEntities.stream()
                    .filter(data -> data.isChained(player))
                    .forEach(data -> {
                        Entity entity = plugin.getServer().getEntity(data.getOpposite(player));
                        if (entity == null) return;
                        entity.teleport(event.getTo());
                    });
        }
    }

    @EventHandler
    public void on(ItemSpawnEvent event) {
        if (event.getEntity().getItemStack().getType() == Material.LEAD)
            event.setCancelled(true);
    }

    public List<ChainData> getChainedEntities() {
        return chainedEntities;
    }

    @Override
    public void run() {
        chainedEntities.forEach(data -> data.tick(plugin));
    }
}
