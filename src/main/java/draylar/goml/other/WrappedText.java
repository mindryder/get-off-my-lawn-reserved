package draylar.goml.other;

import eu.pb4.placeholders.api.TextParserUtils;
import eu.pb4.placeholders.api.node.TextNode;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public record WrappedText(Text text, TextNode node, String input) {
    public static WrappedText of(String input) {
        var val = TextParserUtils.formatNodes(input);
        return new WrappedText(val.toText(null, true), val, input);
    }

    public static WrappedText ofSafe(String input) {
        var val = TextParserUtils.formatNodesSafe(input);
        return new WrappedText(val.toText(null, true), val, input);
    }

    public MutableText mutableText() {
        return this.text.copy();
    }
}
