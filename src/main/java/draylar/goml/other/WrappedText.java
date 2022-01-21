package draylar.goml.other;

import eu.pb4.placeholders.TextParser;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public record WrappedText(Text text, String input) {
    public static WrappedText of(String input) {
        return new WrappedText(TextParser.parse(input), input);
    }

    public static WrappedText ofSafe(String input) {
        return new WrappedText(TextParser.parseSafe(input), input);
    }

    public MutableText mutableText() {
        return this.text.shallowCopy();
    }
}
