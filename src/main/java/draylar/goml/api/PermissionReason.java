package draylar.goml.api;

import net.minecraft.text.Text;

public enum PermissionReason {
    BLOCK_PROTECTED(Text.translatable("text.goml.block_protected")),
    ENTITY_PROTECTED(Text.translatable("text.goml.entity_protected")),
    AREA_PROTECTED(Text.translatable("text.goml.area_protected"));

    private Text reason;

    PermissionReason(Text reason) {
        this.reason = reason;
    }

    public Text getReason() {
        return reason;
    }
}
