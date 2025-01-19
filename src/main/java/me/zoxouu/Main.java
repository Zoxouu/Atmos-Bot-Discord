package me.zoxouu;

import lombok.Getter;
import me.zoxouu.commands.BotCommand;
import me.zoxouu.commands.EmbedCommand;
import me.zoxouu.commands.TicketCommand;
import me.zoxouu.config.Impl.DefaultConfig;
import me.zoxouu.listeners.LogListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.EnumSet;

public class Main {

    @Getter
    public static JDA jda;
    @Getter
    public static DefaultConfig config;
    @Getter
    public static Guild guild;

    public static void main(String[] args) {
        run();
    }

    public static void run(){
        config = new DefaultConfig();

        jda = JDABuilder.createDefault(config.getToken())
                .addEventListeners(new LogListener())
                .addEventListeners(new EmbedCommand())
                .addEventListeners(new TicketCommand())
                .addEventListeners(new BotCommand())
                .setActivity(Activity.customStatus(config.getServerConfig().bio()))
                .enableIntents(EnumSet.allOf(GatewayIntent.class))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ACTIVITY)
                .build();
        try {
            jda.awaitReady();
            guild = jda.getGuildById(config.getServerConfig().guildId());
            registerCommands();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void registerCommands() {
        guild.updateCommands().addCommands(
                Commands.message("Editer"),
                Commands.slash("config", "Modifier la config du bot")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands(new SubcommandData("bio", "Modifier la bio du bot")
                                .addOption(OptionType.STRING, "bio", "Entrez la nouvelle bio (les emoji custom ne fonctionne pas)", true))
                        .addSubcommands(new SubcommandData("count", "Modifier le channel de compteur")
                                .addOption(OptionType.CHANNEL, "channel", "Selectionner le channel", true)
                                .addOption(OptionType.STRING, "format", "Entrez le format du compteur", true))
                        .addSubcommands(new SubcommandData("reboot", "restart le bot")),
                Commands.slash("embed", "Créer un embed personnalise")
                        .addOption(OptionType.CHANNEL, "channel", "Selectionner le channel",false)
                        .addOption(OptionType.STRING, "message", "Entrez l'id du message",false)
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
                Commands.slash("ticket", "Installer le système de tickets")
                        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                        .addSubcommands(new SubcommandData("setup", "Envoyer le message de création de ticket dans un channel")
                                .addOption(OptionType.CHANNEL, "channel","Selectionner le channel", true))
                        .addSubcommands(new SubcommandData("category", "Mettre à jour le nouvel ID de la catégorie")
                                .addOption(OptionType.STRING, "category-id", "Entrez l'ID de la categorie", true))
                        .addSubcommands(new SubcommandData("transcript", "Mettre à jour le nouvel ID du channel transcript")
                                .addOption(OptionType.CHANNEL, "channel", "Selectionner le channel", true))
                        .addSubcommands(new SubcommandData("avis", "Mettre à jour le nouvel ID du channel transcript")
                                .addOption(OptionType.CHANNEL, "channel", "Selectionner le channel", true))
        ).queue();
    }

}