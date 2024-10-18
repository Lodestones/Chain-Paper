package to.lodestone.chain;

import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.entity.LivingEntity;
import to.lodestone.bookshelfapi.api.command.Command;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

public class UnchainCommand extends Command {
    public UnchainCommand(ChainPlugin plugin) {
        super("unchain");
        permission("lodestone.chain.commands.unchain");
        arguments(new EntitySelectorArgument.OneEntity("target"));
        executes((sender, args) -> {
            if (args.get(0) instanceof LivingEntity livingEntity) {
                if (!plugin.isChained(livingEntity)) {
                    sender.sendMessage(MiniMessageUtil.deserialize("<red>%s is not chained to anyone", livingEntity.getName()));
                    return;
                }

                plugin.unchain(livingEntity);
                sender.sendMessage(MiniMessageUtil.deserialize("%s has been unchained", livingEntity.getName()));
            } else {
                sender.sendMessage(MiniMessageUtil.deserialize("<red>Only living entities can be chained"));
            }
        });
    }
}
