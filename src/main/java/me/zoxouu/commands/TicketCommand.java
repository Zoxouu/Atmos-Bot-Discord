package me.zoxouu.commands;

import lombok.NonNull;
import me.zoxouu.Main;
import me.zoxouu.utils.ColorFormat;
import me.zoxouu.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TicketCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("ticket")) {
            if (event.getSubcommandName().equals("setup")) {
                event.getGuild().getTextChannelById(event.getOption("channel").getAsChannel().getId()).sendMessageEmbeds(new EmbedBuilder()
                                .setTitle("\uD83C\uDFAB  Atmos - Support")
                                .setDescription("Selectionner votre le type de votre demande. \n Un membre de l'équipe d'Atmos vous prendra en charge dans les plus brefs délais.")
                                .setFooter(new EmbedUtils().footer())
                                .setColor(ColorFormat.EMBED_MAIN)
                                .build()
                ).addActionRow(
                        StringSelectMenu.create("ticket-menu")
                                .setPlaceholder("Quelle est votre demande ?")
                                .addOptions(SelectOption.of("Support", "ticket-support").withEmoji(Emoji.fromUnicode("\uD83D\uDED2")))
                                .addOptions(SelectOption.of("Commander", "ticket-commander").withEmoji(Emoji.fromUnicode("\uD83D\uDC65")))
                                .build()
                ).queue();
                event.reply("L'embed a biens ete envoyer dans " + event.getGuild().getTextChannelById(event.getOption("channel").getAsChannel().getId()).getAsMention()).setEphemeral(true).queue();
            } else if (event.getSubcommandName().equals("category")) {
                Main.getConfig().getTicketConfig().setCategoryId(event.getOption("category").getAsString());
                Main.getConfig().save();
                event.reply("La categorie a bien ete mise a jour !").setEphemeral(true).queue();
            } else if (event.getSubcommandName().equals("transcript")) {
                Main.getConfig().getTicketConfig().setTranscriptId(event.getOption("channel").getAsChannel().getId().toString());
                Main.getConfig().save();
                event.reply("Le channel transcript a bien ete mise a jour !").setEphemeral(true).queue();
            } else if (event.getSubcommandName().equals("avis")) {
                Main.getConfig().getTicketConfig().setAvisId(event.getOption("channel").getAsChannel().getId().toString());
                Main.getConfig().save();
                event.reply("Le channel avis a bien ete mise a jour !").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("ticket-menu")) {
            TextChannel ticket = null;
            EmbedBuilder embed = new EmbedBuilder();
            if (event.getSelectedOptions().get(0).getValue().equals("ticket-support")) {
                if (event.getGuild().getCategoryById(Main.getConfig().getTicketConfig().categoryId())
                        .getTextChannels()
                        .stream()
                        .anyMatch(textChannel -> textChannel.getName().contains("support-" + event.getUser().getEffectiveName()))) {
                    event.reply("Le nombre maximum de ticket a ete atteint !").setEphemeral(true).queue();
                    return;
                }
                 ticket = createTicket(Main.getGuild(), "\uD83C\uDFAB support " + event.getUser().getEffectiveName().toLowerCase(), event.getUser());
                 embed
                         .setTitle("\uD83D\uDED2  Atmos - Support")
                         .setDescription("Bonjour " + Main.getGuild().getMemberById(event.getUser().getId()).getAsMention() + ", votre ticket a été créé avec succès.")
                         .setFooter(new EmbedUtils().footer())
                         .setColor(ColorFormat.EMBED_MAIN)
                         .build();
                event.reply("Votre demande de support a bien ete prise en compte !" + ticket.getJumpUrl()).setEphemeral(true).queue();
            } else if (event.getSelectedOptions().get(0).getValue().equals("ticket-commander")) {
                if (event.getGuild().getCategoryById(Main.getConfig().getTicketConfig().categoryId())
                        .getTextChannels()
                        .stream()
                        .anyMatch(textChannel -> textChannel.getName().contains("commande-" + event.getUser().getEffectiveName().toLowerCase()))) {
                    event.reply("Le nombre maximum de ticket a ete atteint !").setEphemeral(true).queue();
                    return;
                }
                ticket = createTicket(Main.getGuild(), "\uD83C\uDFAB commande " + event.getUser().getEffectiveName(), event.getUser());
                embed
                        .setTitle("\uD83D\uDC65  Atmos - Commande")
                        .setDescription("Bonjour " + Main.getGuild().getMemberById(event.getUser().getId()).getAsMention() + ", votre ticket a été créé avec succès.")
                        .setFooter(new EmbedUtils().footer())
                        .setColor(ColorFormat.EMBED_MAIN)
                        .build();
                ticket.sendMessage(Main.getGuild().getMemberById(event.getUser().getId()).getAsMention()).queue(message -> message.delete().queue());
                ticket.sendMessage(Main.getGuild().getRoleById("1174477850678665277").getAsMention()).queue( message -> message.delete().queue());
                event.reply("Votre demande de commande a bien ete prise en compte !" + ticket.getJumpUrl()).setEphemeral(true).queue();
            }
            ticket.sendMessageEmbeds(embed.build())
                    .addActionRow(
                            Button.danger("ticket-close", "Fermer"),
                            Button.primary("ticket-avis", "\uD83E\uDD1D Avis")
                    ).queue();
            ticket.sendMessage(Main.getGuild().getMemberById(event.getUser().getId()).getAsMention()).queue(message -> {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        message.delete().queue();
                    }
                }, 1000);
            });
            ticket.sendMessage(Main.getGuild().getRoleById("1174477850678665277").getAsMention()).queue(message -> {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        message.delete().queue();
                    }
                }, 1000);
            });

        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (event.getComponentId().equals("ticket-close")) {
            event.getChannel().getIterableHistory().takeAsync(1000).thenAccept(messages -> {
                StringBuilder transcript = new StringBuilder("Transcription du canal : " + event.getChannel().getName() + "\n\n");

                messages.forEach(message -> {
                    String author = message.getAuthor().getAsTag();
                    String content = message.getContentDisplay();
                    String timestamp = message.getTimeCreated().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    transcript.append(String.format("[%s] %s: %s\n", timestamp, author, content));
                });

                File transcriptFile = new File("transcript-" + event.getChannel().getId() + ".txt");
                try (FileWriter writer = new FileWriter(transcriptFile)) {
                    writer.write(transcript.toString());
                } catch (IOException e) {
                    event.reply("Une erreur est survenue lors de la création de la transcription.").setEphemeral(true).queue();
                    e.printStackTrace();
                    return;
                }
                event.getGuild().getTextChannelById(Main.getConfig().getTicketConfig().transcriptId()).sendMessageEmbeds(
                        new EmbedBuilder()
                                .setTitle("\uD83D\uDCDD  Atmos - Transcription")
                                .addField("\uD83C\uDFAB Ticket ID","`" + event.getChannel().getId() + "`",true)
                                .addField(
                                        event.getGuild().getEmojiById("1330244423128191129").getAsMention() +
                                        event.getGuild().getEmojiById("1330244424780742707").getAsMention() +
                                        event.getGuild().getEmojiById("1330244425904947273").getAsMention() +
                                        event.getGuild().getEmojiById("1330244427049996452").getAsMention() +
                                        event.getGuild().getEmojiById("1330244428341968968").getAsMention() +
                                        event.getGuild().getEmojiById("1330244429520568553").getAsMention(),
                                        getMemberMention(event, event.getChannel().getName().substring(event.getChannel().getName().lastIndexOf('-') + 1)),
                                        true
                                )
                                .addField(
                                        event.getGuild().getEmojiById("1330247546567917662").getAsMention() +
                                             event.getGuild().getEmojiById("1330247547440205834").getAsMention() +
                                             event.getGuild().getEmojiById("1330247548589314112").getAsMention() +
                                             event.getGuild().getEmojiById("1330247550086811850").getAsMention() +
                                             event.getGuild().getEmojiById("1330247550984261703").getAsMention() +
                                             event.getGuild().getEmojiById("1330247552741806215").getAsMention(),
                                        event.getMember().getAsMention(),
                                        true
                                )
                                .setColor(ColorFormat.EMBED_MAIN)
                                .setFooter(new EmbedUtils().footer())
                                .build()
                ).queue();
                event.getGuild().getTextChannelById(Main.getConfig().getTicketConfig().transcriptId()).sendFiles(FileUpload.fromData(transcriptFile)).queue();
                if (transcriptFile.exists()) {
                    boolean deleted = transcriptFile.delete();
                    if (deleted) {
                        System.out.println("Le fichier a été supprimé avec succès.");
                    } else {
                        System.out.println("Erreur lors de la suppression du fichier.");
                    }
                }
                event.getChannel().delete().queue();
            }).exceptionally(throwable -> {
                event.reply("Une erreur est survenue lors de la récupération des messages.").setEphemeral(true).queue();
                throwable.printStackTrace();
                return null;
            });
        } else if (event.getComponentId().equals("ticket-avis")) {
            if (event.getChannel().getName().contains("support")) {
                event.reply("Vous ne pouvez pas donner d'avis sur un ticket de support").setEphemeral(true).queue();
                return;
            }
            event.replyModal(Modal.create("modal-ticket", "Ajout d'une image")
                    .addComponents(
                            ActionRow.of(
                                    TextInput.create("modal-ticket-avis-note", "Quelle notes donnez vous a votre service ?", TextInputStyle.SHORT)
                                            .setPlaceholder("note... (entre 0 & 5)")
                                            .build()
                            ),
                            ActionRow.of(
                                    TextInput.create("modal-ticket-avis-desc", "Dites nous en plus", TextInputStyle.PARAGRAPH)
                                            .setPlaceholder("Partagez votre avis sur notre service...")
                                            .build()
                            )
                    )
                    .build()
            ).queue();
        }
    }

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
        if (event.getModalId().equals("modal-ticket")) {
            int note = Integer.parseInt(event.getInteraction().getValue("modal-ticket-avis-note").getAsString());
            if (note < 0 || note > 5) {
                event.reply("La note doit etre comprise entre 0 et 5").setEphemeral(true).queue();
                return;
            }

            event.getGuild().getTextChannelById(Main.getConfig().getTicketConfig().avisId()).sendMessageEmbeds(
                    new EmbedBuilder()
                            .setTitle("\uD83E\uDD1D  Atmos - Avis")
                            .setDescription("Avis de " + event.getUser().getAsMention())
                            .addField("Note", "⭐".repeat(note), false)
                            .addField("Description", event.getInteraction().getValue("modal-ticket-avis-desc").getAsString(), false)
                            .setFooter(new EmbedUtils().footer())
                            .setColor(ColorFormat.EMBED_MAIN)
                            .setThumbnail(event.getUser().getAvatarUrl())
                            .build()
            ).queue();
            event.reply("Votre avis a bien ete pris en compte !").setEphemeral(true).queue();
        }
    }

    private TextChannel createTicket(Guild guild, String title, User user) {
        return guild.createTextChannel(title)
                .addRolePermissionOverride(guild.getRoleById("1173594455987728425").getIdLong(), null , Arrays.stream(Permission.values()).toList())
                .addMemberPermissionOverride(guild.getMemberById(user.getIdLong()).getIdLong(), Collections.singleton(Permission.VIEW_CHANNEL), Collections.singleton(Permission.MESSAGE_SEND))
                .setParent(guild.getCategoryById(Main.getConfig().getTicketConfig().categoryId()))
                .complete();
    }

    public static String getMemberMention(Event event, String memberName) {
        List<Member> membersByName = Main.getGuild().getMembersByName(memberName, true);
        if (membersByName != null && !membersByName.isEmpty()) {
            return membersByName.get(0).getAsMention(); // Retourne la mention du premier membre trouvé par nom
        }

        List<Member> membersByNickname = Main.getGuild().getMembersByNickname(memberName, true);
        if (membersByNickname != null && !membersByNickname.isEmpty()) {
            return membersByNickname.get(0).getAsMention(); // Retourne la mention du premier membre trouvé par surnom
        }

        return "";
    }
}