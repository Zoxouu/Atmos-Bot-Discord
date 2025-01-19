package me.zoxouu.config.common;

import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class TicketConfig {

    private String categoryId;
    private String transcriptId;
    private String avisId;

    public String categoryId() {
        return categoryId;
    }

    public String transcriptId() {
        return transcriptId;
    }

    public String avisId() {
        return avisId;
    }
}
