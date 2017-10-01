package one.oktw.sponge.command;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

public class CommandSpawn implements CommandBase {
    @Override
    public CommandSpec getSpec() {
        return CommandSpec.builder()
                .executor(this)
                .description(Text.of("傳送到世界重生點"))
                .arguments(GenericArguments.playerOrSource(Text.of("Player")))
                .permission("oktw.command.home")
                .build();
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        Player player = (Player) args.getOne("Player").get();
        World world = player.getWorld();

        if (!player.setLocationSafely(world.getSpawnLocation())) {
            player.setLocation(Sponge.getGame().getTeleportHelper().getSafeLocation(world.getSpawnLocation(), 255, 9).get());
        }

        return CommandResult.success();
    }
}
