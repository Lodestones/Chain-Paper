package to.lodestone.chain;

import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import to.lodestone.bookshelfapi.api.command.Command;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

public class ChainCommand extends Command {

    public ChainCommand(ChainPlugin plugin) {
        super("chain");
        aliases("lodestone.chain.commands.chain");
        subCommand(new Command("reload")
                .executes((sender, args) -> {
                    long timeAt = System.currentTimeMillis();
                    plugin.config().initialize();
                    sender.sendMessage(MiniMessageUtil.deserialize("<italic><gray>[Chain was reloaded in %s ms]", System.currentTimeMillis() - timeAt));
                })
        );
        arguments(new EntitySelectorArgument.OneEntity("target_one"));
        optionalArguments(new EntitySelectorArgument.OneEntity("target_two"));
        executes((sender, args) -> {
            if (args.get(0) instanceof LivingEntity targetOne) {
                if (args.get(1) instanceof LivingEntity targetTwo) {
                    if (targetOne == targetTwo) {
                        sender.sendMessage(MiniMessageUtil.deserialize("<red>You cannot chain yourself with yourself"));
                        return;
                    }

                    targetTwo.setLeashHolder(targetOne);
                    targetTwo.getPersistentDataContainer().set(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING, targetOne.getUniqueId().toString());
                    targetOne.getPersistentDataContainer().set(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING, targetTwo.getUniqueId().toString());
                    sender.sendMessage(MiniMessageUtil.deserialize("%s is now chained with %s", targetOne.getName(), targetTwo.getName()));
                } else {
                    if (sender instanceof Player player) {
                        if (targetOne == player) {
                            player.sendMessage(MiniMessageUtil.deserialize("<red>You cannot chain yourself with yourself"));
                            return;
                        }

                        targetOne.setLeashHolder(player);
                        targetOne.getPersistentDataContainer().set(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING, player.getUniqueId().toString());
                        player.getPersistentDataContainer().set(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING, targetOne.getUniqueId().toString());
                        player.sendMessage(MiniMessageUtil.deserialize("%s is now chained with %s.", player.getName(), targetOne.getName()));
                    } else {
                        sender.sendMessage(MiniMessageUtil.deserialize("<red>Only players can supply a single entity"));
                    }
                }
            } else {
                sender.sendMessage(MiniMessageUtil.deserialize("<red>Only living entities can be chained"));
            }
        });
    }
}
