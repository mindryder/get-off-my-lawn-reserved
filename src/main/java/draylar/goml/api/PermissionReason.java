package draylar.goml.api;

import net.minecraft.text.TranslatableText;

public enum PermissionReason {
    BLOCK_PROTECTED(new TranslatableText("text.goml.block_protected")),
    ENTITY_PROTECTED(new TranslatableText("text.goml.entity_protected")),
    AREA_PROTECTED(new TranslatableText("text.goml.area_protected"));

    private TranslatableText reason;

    PermissionReason(TranslatableText reason) {
        this.reason = reason;
    }

    public TranslatableText getReason() {
        return reason;
    }
}
