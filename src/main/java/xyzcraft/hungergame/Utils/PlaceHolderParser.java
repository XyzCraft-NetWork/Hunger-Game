package xyzcraft.hungergame.Utils;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlaceHolderParser {

    public static String parse(String template, Map<String, Function<Player, String>> placeholders, Player player) {

        Pattern pattern = Pattern.compile("%(.*?)%");
        Matcher matcher = pattern.matcher(template);

        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Function<Player, String> valueFunction = placeholders.get(placeholder);

            if (valueFunction != null) {
                String replacement = valueFunction.apply(player);

                if (replacement == null) {
                    replacement = "";
                }
                matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
            } else {

                matcher.appendReplacement(result, Matcher.quoteReplacement("%" + placeholder + "%"));
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }
}