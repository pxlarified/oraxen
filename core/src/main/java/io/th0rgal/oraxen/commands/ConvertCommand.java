package io.th0rgal.oraxen.commands;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.TextArgument;
import io.th0rgal.oraxen.config.Message;
import io.th0rgal.oraxen.converter.ItemsAdderMigrator;
import io.th0rgal.oraxen.utils.AdventureUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

public class ConvertCommand {

    CommandAPICommand getConvertCommand() {
        return new CommandAPICommand("convert")
                .withPermission("oraxen.command.convert")
                .withArguments(new TextArgument("type").replaceSuggestions(
                        ArgumentSuggestions.strings("itemsadder")))
                .executes((sender, args) -> {
                    String type = (String) args.get("type");
                    if ("itemsadder".equalsIgnoreCase(type)) {
                        convertItemsAdder(sender);
                    }
                });
    }

    private void convertItemsAdder(@Nullable CommandSender sender) {
        Message.CONVERTING.send(sender, AdventureUtils.tagResolver("type", "ItemsAdder"));
        ItemsAdderMigrator migrator = new ItemsAdderMigrator();
        boolean success = migrator.migrate();
        if (success) {
            Message.CONVERT_SUCCESS.send(sender, AdventureUtils.tagResolver("type", "ItemsAdder"));
        } else {
            Message.CONVERT_FAILED.send(sender, AdventureUtils.tagResolver("type", "ItemsAdder"));
        }
    }
}
