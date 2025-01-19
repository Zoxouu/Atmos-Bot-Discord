package me.zoxouu.utils;

import me.zoxouu.Main;
import net.dv8tion.jda.api.EmbedBuilder;

import java.time.Instant;

public class EmbedUtils extends EmbedBuilder {

    public EmbedUtils() {
        super();
    }

    public EmbedUtils copyright() {
        super.setFooter("Copyright ATMOS - Tout droits réservés. ");
        return this;
    }

    public EmbedUtils error(String error) {
        super.setColor(ColorFormat.EMBED_ERROR);
        super.setTitle("Erreur");
        super.setDescription(error);
        return this;
    }

    public EmbedUtils warning(String warning) {
        super.setColor(ColorFormat.EMBED_WARNING);
        super.setTitle("Attention");
        super.setDescription(warning);
        return this;
    }

    public EmbedUtils ok(String ok) {
        super.setColor(ColorFormat.EMBED_VALID);
        super.setTitle("Succès");
        super.setDescription(ok);
        return this;
    }

    public EmbedUtils now() {
        super.setTimestamp(Instant.now());
        return this;
    }

    public String footer() {
        return "Copyright ATMOS - Tout droits réservés. ";
    }
}
