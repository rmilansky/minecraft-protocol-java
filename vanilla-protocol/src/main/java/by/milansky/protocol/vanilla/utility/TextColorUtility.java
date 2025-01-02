package by.milansky.protocol.vanilla.utility;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * @author milansky
 */
@UtilityClass
public final class TextColorUtility {
    private static final Map<Integer, NamedTextColor> COLOR_MAP = new Int2ObjectOpenHashMap<>();
    private static final Map<NamedTextColor, Integer> REVERSE_COLOR_MAP = new HashMap<>();

    static {
        COLOR_MAP.put(0, NamedTextColor.BLACK);
        COLOR_MAP.put(1, NamedTextColor.DARK_BLUE);
        COLOR_MAP.put(2, NamedTextColor.DARK_GREEN);
        COLOR_MAP.put(3, NamedTextColor.DARK_AQUA);
        COLOR_MAP.put(4, NamedTextColor.DARK_RED);
        COLOR_MAP.put(5, NamedTextColor.DARK_PURPLE);
        COLOR_MAP.put(6, NamedTextColor.GOLD);
        COLOR_MAP.put(7, NamedTextColor.GRAY);
        COLOR_MAP.put(8, NamedTextColor.DARK_GRAY);
        COLOR_MAP.put(9, NamedTextColor.BLUE);
        COLOR_MAP.put(10, NamedTextColor.GREEN);
        COLOR_MAP.put(11, NamedTextColor.AQUA);
        COLOR_MAP.put(12, NamedTextColor.RED);
        COLOR_MAP.put(13, NamedTextColor.LIGHT_PURPLE);
        COLOR_MAP.put(14, NamedTextColor.YELLOW);
        COLOR_MAP.put(15, NamedTextColor.WHITE);

        COLOR_MAP.forEach((integer, namedTextColor) -> REVERSE_COLOR_MAP.put(namedTextColor, integer));
    }

    public static @Nullable NamedTextColor fromId(int id) {
        return COLOR_MAP.get(id);
    }

    public static int toId(final @NotNull NamedTextColor color) {
        return REVERSE_COLOR_MAP.getOrDefault(color, 15);
    }
}
