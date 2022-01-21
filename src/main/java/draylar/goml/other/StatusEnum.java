package draylar.goml.other;

import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.util.Locale;

public interface StatusEnum<T> {
    Item getIcon();
    T getNext();
    T getPrevious();
    Text getName();

    enum TargetPlayer implements StatusEnum<TargetPlayer> {
        EVERYONE,
        TRUSTED,
        DISABLED;

        public Item getIcon() {
            return switch (this) {
                case EVERYONE -> Items.GREEN_WOOL;
                case TRUSTED -> Items.YELLOW_WOOL;
                case DISABLED -> Items.GRAY_WOOL;
            };
        }

        public TargetPlayer getNext() {
            return switch (this) {
                case EVERYONE -> TRUSTED;
                case TRUSTED -> DISABLED;
                case DISABLED -> EVERYONE;
            };
        }

        public TargetPlayer getPrevious() {
            return switch (this) {
                case EVERYONE -> DISABLED;
                case TRUSTED -> EVERYONE;
                case DISABLED -> TRUSTED;
            };
        }

        public Text getName() {
            return new TranslatableText("text.goml.mode." + this.name().toLowerCase(Locale.ROOT));
        }
    }
}