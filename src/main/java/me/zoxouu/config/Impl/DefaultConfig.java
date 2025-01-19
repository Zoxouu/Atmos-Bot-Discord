package me.zoxouu.config.Impl;

import lombok.Getter;
import lombok.Setter;
import me.zoxouu.config.Config;
import me.zoxouu.config.ConfigField;
import me.zoxouu.config.common.LogConfig;
import me.zoxouu.config.common.ServerConfig;
import me.zoxouu.config.common.TicketConfig;

@Getter
@Setter
public class DefaultConfig extends Config {

    @ConfigField
    private String token = "your_token_here";

    @ConfigField
    private ServerConfig serverConfig = new ServerConfig("guild_id_here", "bio_here", "count_channel_here", "count_format_here", "join_channel_here");

    @ConfigField
    public LogConfig logConfig = new LogConfig("server_log_id_here", "moderation_log_id_here");

    @ConfigField
    public TicketConfig ticketConfig = new TicketConfig( "ticket_category_id_here", "ticket_transcript_id_here", "ticket_avis_id_here");

    public DefaultConfig() {
        loadInstance("config", "ATMOS Bot", this);
    }
}
