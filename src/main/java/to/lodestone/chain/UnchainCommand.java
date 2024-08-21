package to.lodestone.chain;

import dev.jorel.commandapi.arguments.EntitySelectorArgument;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataType;
import to.lodestone.bookshelfapi.api.command.Command;
import to.lodestone.bookshelfapi.api.util.MiniMessageUtil;

import java.util.UUID;

public class UnchainCommand extends Command {
    public UnchainCommand() {
        super("unchain");
        permission("lodestone.chain.commands.unchain");
        arguments(new EntitySelectorArgument.OneEntity("target"));
        executes((sender, args) -> {
            if (args.get(0) instanceof LivingEntity livingEntity) {
                if (livingEntity.getPersistentDataContainer().has(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING)) {
                    String uniqueId = livingEntity.getPersistentDataContainer().get(ChainPlugin.CHAINED_KEY, PersistentDataType.STRING);
                    if (uniqueId == null) {
                        sender.sendMessage(MiniMessageUtil.deserialize("<red>%s is not chained to anyone", livingEntity.getName()));
                        return;
                    }

                    LivingEntity otherEntity = (LivingEntity) livingEntity.getWorld().getEntity(UUID.fromString(uniqueId));
                    livingEntity.getPersistentDataContainer().remove(ChainPlugin.CHAINED_KEY);

                    if (otherEntity != null) {
                        otherEntity.setLeashHolder(null);
                        otherEntity.getPersistentDataContainer().remove(ChainPlugin.CHAINED_KEY);
                    }

                    livingEntity.setLeashHolder(null);
                    sender.sendMessage(MiniMessageUtil.deserialize("%s has been unchained", livingEntity.getName()));
                } else {
                    sender.sendMessage(MiniMessageUtil.deserialize("<red>%s is not chained to anyone", livingEntity.getName()));
                }
            }else {
                sender.sendMessage(MiniMessageUtil.deserialize("<red>Only living entities can be chained"));
            }
        });
    }
}
