package draylar.goml.other;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.jetbrains.annotations.ApiStatus;

import java.util.Locale;

@ApiStatus.Internal
public interface StatusEnum<T> {
    Item getIcon();
    T getNext();
    T getPrevious();
    Text getName();

    enum TargetPlayer implements StatusEnum<TargetPlayer> {
        EVERYONE,
        TRUSTED,
        UNTRUSTED,
        DISABLED;

        public Item getIcon() {
            return switch (this) {
                case EVERYONE -> Items.GREEN_WOOL;
                case TRUSTED -> Items.YELLOW_WOOL;
                case UNTRUSTED -> Items.RED_WOOL;
                case DISABLED -> Items.GRAY_WOOL;
            };
        }

        public TargetPlayer getNext() {
            return switch (this) {
                case EVERYONE -> TRUSTED;
                case TRUSTED -> UNTRUSTED;
                case UNTRUSTED -> DISABLED;
                case DISABLED -> EVERYONE;
            };
        }

        public TargetPlayer getPrevious() {
            return switch (this) {
                case EVERYONE -> DISABLED;
                case TRUSTED -> EVERYONE;
                case UNTRUSTED -> TRUSTED;
                case DISABLED -> UNTRUSTED;
            };
        }

        public Text getName() {
            return new TranslatableText("text.goml.mode." + this.name().toLowerCase(Locale.ROOT));
        }
    }

    enum Toggle implements StatusEnum<Toggle> {
        ENABLED,
        DISABLED;

        public Item getIcon() {
            return switch (this) {
                case ENABLED -> Items.GREEN_WOOL;
                case DISABLED -> Items.GRAY_WOOL;
            };
        }

        public Toggle getNext() {
            return ENABLED == this ? DISABLED : ENABLED;
        }

        public Toggle getPrevious() {
            return ENABLED == this ? DISABLED : ENABLED;

        }

        public Text getName() {
            return new TranslatableText("text.goml.mode." + this.name().toLowerCase(Locale.ROOT));
        }
    }
}