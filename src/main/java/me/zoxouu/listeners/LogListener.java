package me.zoxouu.listeners;

import me.zoxouu.Main;
import me.zoxouu.config.Impl.DefaultConfig;
import me.zoxouu.utils.ColorFormat;
import me.zoxouu.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.GenericChannelUpdateEvent;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.*;
import net.dv8tion.jda.api.events.guild.member.update.GenericGuildMemberUpdateEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateAvatarEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceDeafenEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceGuildMuteEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogListener extends ListenerAdapter {

    private final Map<String,String> cache = new HashMap<>();
    private final Timer timer = new Timer();
    private boolean isUpdatePending = false;
    private final DefaultConfig config = Main.getConfig();
    private final EmbedUtils embedUtils = new EmbedUtils();

    @Override
    public void onReady(ReadyEvent event) {
    }


    // ADD
    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        event.getGuild().getTextChannelById(config.getServerConfig().joinChannelId())
                        .sendMessageEmbeds(new EmbedBuilder()
                                .setColor(ColorFormat.EMBED_MAIN)
                                .setTitle(event.getMember().getEffectiveName() + " a rejoint le serveur !")
                                .setDescription("Bienvenue chez ATMOS Project - Production TikTok !")
                                .setThumbnail(event.getMember().getAvatarUrl())
                                .setFooter(embedUtils.footer())
                                .build()).queue();
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                        .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), null, null, event.getMember().getAsMention())).queue();
        scheduleUpdate(event.getGuild());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        cache.put(event.getMessageId(), event.getMessage().getContentRaw());
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getChannel().getAsMention() + " / " + event.getJumpUrl(), Objects.requireNonNull(event.getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(),  event.getRole().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_CREATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).complete().get(0).getUser().isBot())) return;
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getChannel().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_CREATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getRoles().isEmpty() ? null : event.getRoles().get(0).getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).complete().get(0).getUser()).getAsMention(), event.getMember().getAsMention())).queue();
    }

    @Override
    public void onGuildBan(GuildBanEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getGuild().retrieveBanList().complete().stream().filter(ban -> ban.getUser().equals(event.getUser())).map(ban -> ban.getReason() != null ? ban.getReason() : "Aucune raison spécifiée").findFirst().orElse("Aucune raison spécifiée"), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.BAN).complete().get(0).getUser()).getAsMention(), event.getUser().getAsMention())).queue();
    }

    // REMOVE
    @Override
    public void onMessageDelete(MessageDeleteEvent event) {
        if (event.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).complete().get(0).getUser().isBot() || cache.get(event.getMessageId()) == null) return;
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), cache.get(event.getMessageId()), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_DELETE).complete().get(0).getUser()).getAsMention(), event.getJumpUrl())).queue();
        cache.remove(event.getMessageId());
    }

    @Override
    public void onMessageReactionRemove(MessageReactionRemoveEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getChannel().getAsMention() + " / " + event.getJumpUrl(), Objects.requireNonNull(event.getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(),  event.getRole().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_DELETE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_DELETE).complete().get(0).getUser().isBot())) return;
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getChannel().getAsMention(),  Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_DELETE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getRoles().isEmpty() ? null : event.getRoles().get(0).getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_ROLE_UPDATE).complete().get(0).getUser()).getAsMention(), event.getMember().getAsMention())).queue();
    }

    @Override
    public void onGuildUnban(GuildUnbanEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getGuild().retrieveBanList().complete().stream().filter(ban -> ban.getUser().equals(event.getUser())).map(ban -> ban.getReason() != null ? ban.getReason() : "Aucune raison spécifiée").findFirst().orElse("Aucune raison spécifiée"), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.UNBAN).complete().get(0).getUser()).getAsMention(), event.getUser().getAsMention())).queue();
    }

    // EDIT
    @Override
    public void onGenericGuildMemberUpdate(GenericGuildMemberUpdateEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        if (event instanceof GuildMemberUpdateNicknameEvent nicknameEvent) {
            embed.addField("MODIFICATION",
                    (nicknameEvent.getOldValue() != null ? nicknameEvent.getOldNickname() : nicknameEvent.getMember().getEffectiveName())
                            + " ➡\uFE0F "
                            + (nicknameEvent.getNewValue() != null ? nicknameEvent.getNewNickname() : nicknameEvent.getMember().getEffectiveName()),
                    false);

        } else if (event instanceof GuildMemberUpdateAvatarEvent avatarEvent) {
            embed.setAuthor(
                    "MODIFICATION",
                    event.getOldValue() != null ? avatarEvent.getOldAvatarUrl() : event.getMember().getAvatarUrl(),
                    event.getOldValue() != null ? avatarEvent.getOldAvatarUrl() : event.getMember().getAvatarUrl()
            );
            embed.setThumbnail(
                    avatarEvent.getNewValue() != null ? avatarEvent.getNewAvatarUrl() : avatarEvent.getMember().getAvatarUrl()
            );
        } else return;

        embed
                .setTitle("📋 Log détecté : " + event.getClass().getSimpleName().replace("Event", ""))
                .addField("DATE", ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")), false)
                .setFooter(embedUtils.footer())
                .setColor(ColorFormat.EMBED_WARNING);
        embed.addField("AUTEUR", event.getEntity().getAsMention(), false);
        Main.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public void onMessageUpdate(MessageUpdateEvent event) {
        if (event.getAuthor().isBot()) return;
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.EDIT, event.getClass().getSimpleName(), event.getChannel().getName() +" / " + event.getMessageId(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MESSAGE_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGenericRoleUpdate(GenericRoleUpdateEvent event) {
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.EDIT, event.getClass().getSimpleName(), event.getRole().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.ROLE_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }


    @Override
    public void onGenericChannelUpdate(GenericChannelUpdateEvent<?> event) {
        Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).complete().get(0).getUser());
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.EDIT, event.getClass().getSimpleName(), event.getChannel().getName(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.CHANNEL_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGuildVoiceGuildMute(GuildVoiceGuildMuteEvent event) {
        if (event.isGuildMuted()) {
            event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                    .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getMember().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
            return;
        }
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getMember().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGuildVoiceDeafen(GuildVoiceDeafenEvent event) {
        if (event.isDeafened()) {
            event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                    .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getMember().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
            return;
        }
        event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getMember().getAsMention(), Objects.requireNonNull(event.getGuild().retrieveAuditLogs().type(ActionType.MEMBER_UPDATE).complete().get(0).getUser()).getAsMention(), null)).queue();
    }

    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getChannelJoined() != null) {
            event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                    .sendMessageEmbeds(sendLog(Action.ADD, event.getClass().getSimpleName(), event.getChannelJoined().getAsMention(), event.getMember().getAsMention(), null)).queue();
        } else if (event.getChannelLeft() != null) {
            event.getGuild().getTextChannelById(config.getLogConfig().severLog())
                    .sendMessageEmbeds(sendLog(Action.DELETE, event.getClass().getSimpleName(), event.getChannelLeft().getAsMention(), event.getMember().getAsMention(), null)).queue();
        }
    }

    private void scheduleUpdate(Guild guild) {
        if (isUpdatePending) return;
        isUpdatePending = true;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                VoiceChannel channel = guild.getVoiceChannelById(Main.getConfig().getServerConfig().countChannelId());

                if (channel != null) {
                    System.out.println("Salon trouvé, tentative de mise à jour du nom...");

                    channel.getManager()
                            .setName(Main.getConfig().getServerConfig().countFormat() + " " + guild.getMemberCount())
                            .queue(
                                    success -> {
                                        System.out.println("Nom du salon mis à jour avec succès !");
                                        System.out.println("Nouveau nom du salon: " + channel.getName());
                                        isUpdatePending = false;
                                    },
                                    error -> {
                                        System.err.println("Erreur lors de la mise à jour du salon : " + error.getMessage());
                                        isUpdatePending = false;
                                    }
                            );
                } else {
                    System.err.println("Salon vocal introuvable !");
                    isUpdatePending = false;
                }
            }
        }, 10000);
    }

    public MessageEmbed sendLog(Action action, String event, String message, String author, String target) {
        EmbedBuilder embed = new EmbedBuilder();
        if (message != null) {
            embed.addField("MODIFICATION", message, false);
        }
        if (author != null) {
            embed.addField("AUTEUR", author, false);
        }
        if (target != null) {
            embed.addField("CIBLE", target, false);
        }
        embed.build();
        switch (action) {
            case ADD -> embed.setColor(ColorFormat.EMBED_VALID);
            case DELETE -> embed.setColor(ColorFormat.EMBED_ERROR);
            case EDIT -> embed.setColor(ColorFormat.EMBED_WARNING);
        }
        embed
                .setTitle("📋 Log détecté : " + event.replace("Event", ""))
                .addField("DATE", ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")), false)
                .setFooter(embedUtils.footer())
                .build();
        return embed.build();
    }

    public enum Action {
        ADD, DELETE, EDIT;
    }
}
