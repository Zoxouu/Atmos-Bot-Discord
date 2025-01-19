package me.zoxouu.commands;

import lombok.NonNull;
import me.zoxouu.utils.ColorFormat;
import me.zoxouu.utils.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import net.dv8tion.jda.api.interactions.commands.context.MessageContextInteraction;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;

import java.awt.*;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class EmbedCommand extends ListenerAdapter {

    private EmbedBuilder embed = new EmbedBuilder();

    private String title;
    private String titleLink;
    private String description;
    private String author;
    private String authorIcon;
    private String authorUrl;
    private Color color;
    private String timestamp;
    private String image;
    private List<MessageEmbed.Field> fields;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (event.getName().equals("embed")) {
            sendDefaultEmbed(event);
        }
    }

    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        if (event.getName().equals("Editer")) {
            if (!event.getTarget().getAuthor().isBot()) {
                event.reply("Vous ne pouvez pas editer ce message").setEphemeral(true).queue();
                return;
            }

            if (event.getTarget().getEmbeds().isEmpty()) {
                event.reply("Ce message ne contient pas d'embed").setEphemeral(true).queue();
                return;
            }

            if (event.getTarget().getEmbeds().size() > 1) {
                System.out.println("Ce message contient plus d'un embed");
                return;
            }
            MessageEmbed messageEmbed = event.getTarget().getEmbeds().get(0);
            title = messageEmbed.getTitle();
            titleLink = messageEmbed.getUrl();
            description = messageEmbed.getDescription();
            author = messageEmbed.getAuthor() == null ? null : messageEmbed.getAuthor().getName();
            authorIcon = messageEmbed.getAuthor() == null ? null : messageEmbed.getAuthor().getIconUrl();
            authorUrl = messageEmbed.getAuthor() == null ? null : messageEmbed.getAuthor().getUrl();
            color = messageEmbed.getColor() == null ? null : messageEmbed.getColor();
            timestamp = messageEmbed.getTimestamp() == null ? null : messageEmbed.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss"));
            image = messageEmbed.getThumbnail() == null ? null : messageEmbed.getThumbnail().getUrl();
            fields = messageEmbed.getFields().isEmpty() ? null : messageEmbed.getFields();

            sendDefaultEmbed(event);
        }
    }

    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("embed-menu")) {
            switch (event.getValues().get(0)) {
                case "embed-title" -> {
                    event.replyModal(
                            Modal.create("modal-title","Modification du titre")
                                    .addComponents(
                                            ActionRow.of(
                                                    TextInput.create("modal-title-input", "Quelle est le titre de votre embed ?", TextInputStyle.SHORT)
                                                            .setPlaceholder("Nouveau titre...")
                                                            .build()
                                            ),
                                            ActionRow.of(
                                                    TextInput.create("modal-title-link", "Quelle est le lien du titre de l'embed ?", TextInputStyle.SHORT)
                                                            .setPlaceholder("https://exemple.com")
                                                            .setRequired(false)
                                                            .build()
                                            )
                                    )
                            .build()
                    ).queue();
                }
                case "embed-description" -> {
                    event.replyModal(Modal.create("modal-description","Modification de la description")
                                    .addComponents(
                                            ActionRow.of(
                                                    TextInput.create("modal-description-input", "Quelle est la description de votre embed ?", TextInputStyle.PARAGRAPH)
                                                            .setPlaceholder("Description...")
                                                            .build()
                                            )
                                    )
                            .build()
                    ).queue();
                }
                case "embed-author" -> {
                    event.replyModal(Modal.create("modal-author","Modification de l'auteur")
                                    .addComponents(
                                            ActionRow.of(
                                                    TextInput.create("modal-author-name", "Autheur", TextInputStyle.SHORT)
                                                            .build()
                                            ),
                                            ActionRow.of(
                                                    TextInput.create("modal-author-icon", "Autheur URL", TextInputStyle.SHORT)
                                                            .setRequired(false)
                                                            .build()
                                            ),
                                            ActionRow.of(
                                                    TextInput.create("modal-author-url", "Autheur Icon URL", TextInputStyle.SHORT)
                                                            .setRequired(false)
                                                            .build()
                                            )
                                    )
                            .build()
                    ).queue();
                }
                case "embed-color" -> {
                    event.replyModal(Modal.create("modal-color","Modification de la couleur")
                                    .addComponents(
                                            ActionRow.of(
                                                    TextInput.create("modal-color-input", "Quelle est la couleur de votre embed ?", TextInputStyle.SHORT)
                                                            .setPlaceholder("#FFFFFF")
                                                            .build()
                                            )
                                    )
                            .build()
                    ).queue();
                }
                case "embed-timestamp" -> {
                    event.replyModal(Modal.create("modal-timestamp","Ajout d'un timestamp")
                                    .addComponents(
                                            ActionRow.of(
                                                    TextInput.create("modal-timestamp-input", "Quelle est le texte du timestamp ?", TextInputStyle.SHORT)
                                                            .setPlaceholder(ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")))
                                                            .build()
                                            )
                                    )
                            .build()
                    ).queue();
                }
                case "embed-image" -> {
                    event.replyModal(Modal.create("modal-image", "Ajout d'une image")
                            .addComponents(
                                    ActionRow.of(
                                            TextInput.create("modal-image-input", "Quelle est l'url de image ?", TextInputStyle.SHORT)
                                                    .setPlaceholder("https://exemple.com/image.png")
                                                    .build()
                                    )
                            )
                            .build()
                    ).queue();
                }
            }
        }
    }

    @Override
    public void onModalInteraction(@NonNull ModalInteractionEvent event) {
        switch (event.getModalId()){
            case "modal-title" -> {
                title = event.getValue("modal-title-input").getAsString();
                titleLink = event.getValue("modal-title-link").getAsString();
                event.reply("Le titre de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
            case "modal-description" -> {
                description = event.getValue("modal-description-input").getAsString();
                event.reply("La description de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
            case "modal-author" -> {
                author = event.getValue("modal-author-name").getAsString();
                authorIcon = event.getValue("modal-author-icon").getAsString();
                authorUrl = event.getValue("modal-author-url").getAsString();
                event.reply("L'auteur de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
            case "modal-color" -> {
                color = Color.decode(event.getValue("modal-color-input").getAsString());
                event.reply("La couleur de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
            case "modal-timestamp" -> {
                timestamp = event.getValue("modal-timestamp-input").getAsString();
                event.reply("Le timestamp de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
            case "modal-image" -> {
                image = event.getValue("modal-image-input").getAsString();
                event.reply("Le'image de l'embed a bien été modifié !").setEphemeral(true).queue();
            }
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        Message message = event.getChannel().getHistory().retrievePast(1).complete().get(0);
        if (event.getComponentId().equals("embed-annuler")) {
            if (!message.getEmbeds().isEmpty()) {
                if (message.getEmbeds().get(0).getTitle().equals(title)) {
                    message.delete().queue();
                }
            }
            event.reply("Annulation de la creation de l'embed").setEphemeral(true).queue();
        } else if (event.getComponentId().equals("embed-valider")) {
            try {
                EmbedBuilder embed = new EmbedBuilder();
                embed
                        .setColor(color == null ? ColorFormat.EMBED_MAIN : color)
                        .setTitle(title == null ? null : title)
                        .setTitle(title, titleLink)
                        .setDescription(description == null ? null : description)
                        .setAuthor(author == null ? null : author, authorIcon != null && authorIcon.matches("https?://.*") ? authorIcon : null, authorUrl != null && authorUrl.matches("https?://.*") ? authorUrl : null)
                        .setThumbnail(image == null ? null : image)
                        .setFooter(new EmbedUtils().footer() + " " + (timestamp == null ? ZonedDateTime.now(ZoneId.of("Europe/Paris")).format(DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm:ss")) : timestamp));
                for (MessageEmbed.Field field : fields) {
                    if (field.getName() == null || field.getValue() == null) return;
                    embed.addField(field);
                }
                event.getChannel().sendMessageEmbeds(embed.build()).queue();
                event.reply("L'embed a bien été créé !").setEphemeral(true).queue();
            } catch (RuntimeException e) {
                event.reply("Une erreur est survenue lors de la creation de l'embed:\n" +e.getMessage()).setEphemeral(true).queue();
            }
        }
    }

    public void sendDefaultEmbed(Object event) {
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(ColorFormat.EMBED_MAIN)
                .setTitle("Création d'embed")
                .setDescription("Bienvenue dans l'outil de création d'embed simplifié, ceci est un embed par défaut, tu peux le modifier simplement à partir du menu de sélection ci-dessous !")
                .setFooter(new EmbedUtils().footer());

        StringSelectMenu selectMenu = StringSelectMenu.create("embed-menu")
                .setPlaceholder("Modification de l'embed")
                .addOptions(
                        SelectOption.of("Titre", "embed-title")
                                .withDescription("Modifier le titre de l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),
                        SelectOption.of("Description", "embed-description")
                                .withDescription("Modifier la description de l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),
                        SelectOption.of("Author", "embed-author")
                                .withDescription("Modifier l'auteur de l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),
                        SelectOption.of("Couleur", "embed-color")
                                .withDescription("Modifier la couleur de l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),
                        SelectOption.of("Timestamp", "embed-timestamp")
                                .withDescription("Ajouter un timestamp à l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD")),
                        SelectOption.of("Image", "embed-image")
                                .withDescription("Ajouter une image à l'embed")
                                .withEmoji(Emoji.fromUnicode("\uD83D\uDCDD"))
                ).build();

        if (event instanceof SlashCommandInteraction) {
            SlashCommandInteraction slashEvent = (SlashCommandInteraction) event;
            slashEvent.replyEmbeds(embed.build())
                    .addActionRow(selectMenu)
                    .addActionRow(Button.danger("embed-annuler", "Annuler"), Button.success("embed-valider", "Valider"))
                    .setEphemeral(true).queue();
        } else if (event instanceof MessageContextInteraction) {
            MessageContextInteraction contextEvent = (MessageContextInteraction) event;
            contextEvent.replyEmbeds(embed.build())
                    .addActionRow(selectMenu)
                    .addActionRow(Button.danger("embed-annuler", "Annuler"), Button.success("embed-valider", "Valider"))
                    .setEphemeral(true).queue();
        } else {
            System.out.println("Événement non pris en charge.");
        }
    }


}
