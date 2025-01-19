package me.zoxouu.commands;

import me.zoxouu.Main;
import me.zoxouu.config.common.ServerConfig;
import net.dv8tion.jda.api.entities.Widget;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class BotCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("config")) {
            switch (event.getSubcommandName()) {
                case "bio" -> {
                    ServerConfig config = new ServerConfig(Main.getConfig().getServerConfig().guildId(), event.getOption("bio").getAsString(), Main.getConfig().getServerConfig().countChannelId(), Main.getConfig().getServerConfig().countFormat(), Main.getConfig().getServerConfig().joinChannelId());
                    Main.getConfig().setServerConfig(config);
                    Main.getConfig().save();
                    event.reply("La bio a été modifiée").setEphemeral(true).queue();
                }
                case "count" -> {
                    if (event.getOption("count").getAsChannel() instanceof Widget.VoiceChannel) {
                        event.reply("Le channel doit être un salon vocal").setEphemeral(true).queue();
                        return;
                    }
                    ServerConfig config = new ServerConfig(Main.getConfig().getServerConfig().guildId(), Main.getConfig().getServerConfig().bio(), event.getOption("count").getAsChannel().asVoiceChannel().getId(), event.getOption("format").getAsString(), Main.getConfig().getServerConfig().joinChannelId());
                    Main.getConfig().setServerConfig(config);
                    Main.getConfig().save();
                    event.reply("Le channel de compteur a été modifié").setEphemeral(true).queue();
                }
                case "reboot" -> {
                    event.reply("Le bot va redémarrer").setEphemeral(true).queue();
                    Main.getJda().shutdown();
                    Main.run();
                }
            }
        }
    }
}
