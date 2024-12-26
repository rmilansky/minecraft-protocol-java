package by.milansky.protocol.vanilla.utility;

import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.internal.LazilyParsedNumber;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.handler.codec.DecoderException;
import io.netty.handler.codec.EncoderException;
import lombok.val;
import net.kyori.adventure.nbt.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.json.JSONOptions;
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.option.OptionState;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Utilities for writing and reading data in the Minecraft protocol.
 * <p>
 * A lot of code is taken from <a href="https://github.com/PaperMC/Velocity/blob/dev/3.0.0/proxy/src/main/java/com/velocitypowered/proxy/protocol/ProtocolUtils.java">...</a>
 */
public class ProtocolUtility {
    public static final int DEFAULT_MAX_STRING_SIZE = 65536; // 64KiB

    private static final int MAXIMUM_VARINT_SIZE = 5;
    private static final int[] VAR_INT_LENGTHS = new int[65];

    private static final GsonComponentSerializer PRE_1_16_SERIALIZER, PRE_1_20_3_SERIALIZER, MODERN_SERIALIZER;
    private static final BinaryTagType[] BINARY_TAG_TYPES = new BinaryTagType[]{
            BinaryTagTypes.END, BinaryTagTypes.BYTE, BinaryTagTypes.SHORT, BinaryTagTypes.INT,
            BinaryTagTypes.LONG, BinaryTagTypes.FLOAT, BinaryTagTypes.DOUBLE,
            BinaryTagTypes.BYTE_ARRAY, BinaryTagTypes.STRING, BinaryTagTypes.LIST,
            BinaryTagTypes.COMPOUND, BinaryTagTypes.INT_ARRAY, BinaryTagTypes.LONG_ARRAY};

    static {
        for (int i = 0; i <= 32; ++i) VAR_INT_LENGTHS[i] = (int) Math.ceil((31d - (i - 1)) / 7d);

        VAR_INT_LENGTHS[32] = 1;

        PRE_1_16_SERIALIZER = GsonComponentSerializer.builder()
                .downsampleColors()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
                .options(
                        OptionState.optionState()
                                // before 1.16
                                .value(JSONOptions.EMIT_RGB, Boolean.FALSE)
                                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.LEGACY_ONLY)
                                // before 1.20.3
                                .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.FALSE)
                                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.FALSE)
                                .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.FALSE)
                                .build()
                )
                .build();

        PRE_1_20_3_SERIALIZER = GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
                .options(
                        OptionState.optionState()
                                // after 1.16
                                .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.MODERN_ONLY)
                                // before 1.20.3
                                .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.FALSE)
                                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.FALSE)
                                .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.FALSE)
                                .build()
                )
                .build();

        MODERN_SERIALIZER = GsonComponentSerializer.builder()
                .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
                .options(
                        OptionState.optionState()
                                // after 1.16
                                .value(JSONOptions.EMIT_RGB, Boolean.TRUE)
                                .value(JSONOptions.EMIT_HOVER_EVENT_TYPE, JSONOptions.HoverEventValueMode.MODERN_ONLY)
                                // after 1.20.3
                                .value(JSONOptions.EMIT_COMPACT_TEXT_COMPONENT, Boolean.TRUE)
                                .value(JSONOptions.EMIT_HOVER_SHOW_ENTITY_ID_AS_INT_ARRAY, Boolean.TRUE)
                                .value(JSONOptions.VALIDATE_STRICT_EVENTS, Boolean.TRUE)
                                .build()
                )
                .build();
    }

    private static DecoderException badVarint() {
        return new CorruptedFrameException("Bad VarInt decoded");
    }

    public static int readVarInt(ByteBuf buf) {
        int readable = buf.readableBytes();
        if (readable == 0) {
            // special case for empty buffer
            throw badVarint();
        }

        // we can read at least one byte, and this should be a common case
        int k = buf.readByte();
        if ((k & 0x80) != 128) {
            return k;
        }

        // in case decoding one byte was not enough, use a loop to decode up to the next 4 bytes
        int maxRead = Math.min(MAXIMUM_VARINT_SIZE, readable);
        int i = k & 0x7F;
        for (int j = 1; j < maxRead; j++) {
            k = buf.readByte();
            i |= (k & 0x7F) << j * 7;
            if ((k & 0x80) != 128) {
                return i;
            }
        }
        throw badVarint();
    }

    public static int varIntBytes(int value) {
        return VAR_INT_LENGTHS[Integer.numberOfLeadingZeros(value)];
    }

    public static void writeVarInt(ByteBuf buf, int value) {
        // Peel the one and two byte count cases explicitly as they are the most common VarInt sizes
        // that the proxy will write, to improve inlining.
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else {
            writeVarIntFull(buf, value);
        }
    }

    private static void writeVarIntFull(ByteBuf buf, int value) {
        // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/

        // This essentially is an unrolled version of the "traditional" VarInt encoding.
        if ((value & (0xFFFFFFFF << 7)) == 0) {
            buf.writeByte(value);
        } else if ((value & (0xFFFFFFFF << 14)) == 0) {
            int w = (value & 0x7F | 0x80) << 8 | (value >>> 7);
            buf.writeShort(w);
        } else if ((value & (0xFFFFFFFF << 21)) == 0) {
            write21BitVarInt(buf, value);
        } else if ((value & (0xFFFFFFFF << 28)) == 0) {
            int w = (value & 0x7F | 0x80) << 24 | (((value >>> 7) & 0x7F | 0x80) << 16)
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | (value >>> 21);
            buf.writeInt(w);
        } else {
            int w = (value & 0x7F | 0x80) << 24 | ((value >>> 7) & 0x7F | 0x80) << 16
                    | ((value >>> 14) & 0x7F | 0x80) << 8 | ((value >>> 21) & 0x7F | 0x80);
            buf.writeInt(w);
            buf.writeByte(value >>> 28);
        }
    }

    public static void write21BitVarInt(ByteBuf buf, int value) {
        // See https://steinborn.me/posts/performance/how-fast-can-you-write-a-varint/
        int w = (value & 0x7F | 0x80) << 16 | ((value >>> 7) & 0x7F | 0x80) << 8 | (value >>> 14);
        buf.writeMedium(w);
    }

    public static String readString(ByteBuf buf) {
        return readString(buf, DEFAULT_MAX_STRING_SIZE);
    }

    public static String readString(ByteBuf buf, int cap) {
        int length = readVarInt(buf);
        return readString(buf, cap, length);
    }

    private static String readString(ByteBuf buf, int cap, int length) {
        val charArray = new byte[length];

        buf.readBytes(charArray);

        return new String(charArray);
    }

    public static void writeString(ByteBuf buf, CharSequence str) {
        writeByteArray(buf, str.toString().getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] readByteArray(ByteBuf buf) {
        return readByteArray(buf, DEFAULT_MAX_STRING_SIZE);
    }

    public static byte[] readByteArray(ByteBuf buf, int cap) {
        int length = readVarInt(buf);

        byte[] array = new byte[length];
        buf.readBytes(array);
        return array;
    }

    public static void writeByteArray(ByteBuf buf, byte[] array) {
        writeVarInt(buf, array.length);
        buf.writeBytes(array);
    }

    public static int[] readIntegerArray(ByteBuf buf) {
        int len = readVarInt(buf);
        int[] array = new int[len];
        for (int i = 0; i < len; i++) {
            array[i] = readVarInt(buf);
        }
        return array;
    }

    public static UUID readUuid(ByteBuf buf) {
        long msb = buf.readLong();
        long lsb = buf.readLong();
        return new UUID(msb, lsb);
    }

    public static void writeUuid(ByteBuf buf, UUID uuid) {
        buf.writeLong(uuid.getMostSignificantBits());
        buf.writeLong(uuid.getLeastSignificantBits());
    }

    public static UUID readUuidIntArray(ByteBuf buf) {
        long msbHigh = (long) buf.readInt() << 32;
        long msbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long msb = msbHigh | msbLow;
        long lsbHigh = (long) buf.readInt() << 32;
        long lsbLow = (long) buf.readInt() & 0xFFFFFFFFL;
        long lsb = lsbHigh | lsbLow;
        return new UUID(msb, lsb);
    }

    public static void writeUuidIntArray(ByteBuf buf, UUID uuid) {
        buf.writeInt((int) (uuid.getMostSignificantBits() >> 32));
        buf.writeInt((int) uuid.getMostSignificantBits());
        buf.writeInt((int) (uuid.getLeastSignificantBits() >> 32));
        buf.writeInt((int) uuid.getLeastSignificantBits());
    }

    public static String[] readStringArray(ByteBuf buf) {
        int length = readVarInt(buf);
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readString(buf);
        }
        return ret;
    }

    public static void writeStringArray(ByteBuf buf, String[] stringArray) {
        writeVarInt(buf, stringArray.length);
        for (String s : stringArray) {
            writeString(buf, s);
        }
    }

    public static int[] readVarIntArray(ByteBuf buf) {
        int length = readVarInt(buf);
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readVarInt(buf);
        }
        return ret;
    }

    public static void writeVarIntArray(ByteBuf buf, int[] intArray) {
        writeVarInt(buf, intArray.length);
        for (int i = 0; i < intArray.length; i++) {
            writeVarInt(buf, intArray[i]);
        }
    }

    public static byte[] readByteArray17(ByteBuf buf) {
        int len = readExtendedForgeShort(buf);

        byte[] ret = new byte[len];
        buf.readBytes(ret);
        return ret;
    }

    public static ByteBuf readRetainedByteBufSlice17(ByteBuf buf) {
        int len = readExtendedForgeShort(buf);

        return buf.readRetainedSlice(len);
    }

    public static void writeByteArray17(byte[] b, ByteBuf buf, boolean allowExtended) {
        writeExtendedForgeShort(buf, b.length);
        buf.writeBytes(b);
    }

    public static void writeByteBuf17(ByteBuf b, ByteBuf buf, boolean allowExtended) {
        writeExtendedForgeShort(buf, b.readableBytes());
        buf.writeBytes(b);
    }

    public static int readExtendedForgeShort(ByteBuf buf) {
        int low = buf.readUnsignedShort();
        int high = 0;
        if ((low & 0x8000) != 0) {
            low = low & 0x7FFF;
            high = buf.readUnsignedByte();
        }
        return ((high & 0xFF) << 15) | low;
    }

    public static void writeExtendedForgeShort(ByteBuf buf, int toWrite) {
        int low = toWrite & 0x7FFF;
        int high = (toWrite & 0x7F8000) >> 15;
        if (high != 0) {
            low = low | 0x8000;
        }
        buf.writeShort(low);
        if (high != 0) {
            buf.writeByte(high);
        }
    }

    public static String readStringWithoutLength(ByteBuf buf) {
        return readString(buf, DEFAULT_MAX_STRING_SIZE, buf.readableBytes());
    }

    public static GsonComponentSerializer getJsonChatSerializer(ProtocolVersion version) {
        if (version.greater(VanillaProtocolVersion.MINECRAFT_1_20_3)) {
            return MODERN_SERIALIZER;
        }

        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_16)) {
            return PRE_1_20_3_SERIALIZER;
        }

        return PRE_1_16_SERIALIZER;
    }

    public static <T extends BinaryTag> void writeBinaryTag(ByteBuf buf, ProtocolVersion version,
                                                            T tag) {
        BinaryTagType<T> type = (BinaryTagType<T>) tag.type();
        buf.writeByte(type.id());
        try {
            if (version.lower(VanillaProtocolVersion.MINECRAFT_1_20_2)) {
                // Empty name
                buf.writeShort(0);
            }
            type.write(tag, new ByteBufOutputStream(buf));
        } catch (IOException e) {
            throw new EncoderException("Unable to encode BinaryTag");
        }
    }

    public static BinaryTag readBinaryTag(ByteBuf buf, ProtocolVersion version,
                                          BinaryTagIO.Reader reader) {
        BinaryTagType<?> type = BINARY_TAG_TYPES[buf.readByte()];
        if (version.lower(VanillaProtocolVersion.MINECRAFT_1_20_2)) {
            buf.skipBytes(buf.readUnsignedShort());
        }
        try {
            return type.read(new ByteBufInputStream(buf));
        } catch (IOException thrown) {
            throw new DecoderException("Unable to parse BinaryTag, full error: " + thrown.getMessage());
        }
    }

    public static Component readComponent(final @NotNull ByteBuf buf, final ProtocolVersion version, boolean legacy) {
        if (legacy) return Component.text(readString(buf));

        val serializer = getJsonChatSerializer(version);

        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_20_3)) {
            val binaryTag = readBinaryTag(buf, version, BinaryTagIO.reader());

            return serializer.deserialize(deserializeBinaryTag(binaryTag).toString());
        } else if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_13)) {
            return serializer.deserialize(readString(buf, DEFAULT_MAX_STRING_SIZE));
        } else {
            return serializer.deserialize(readString(buf));
        }
    }

    public static void writeComponent(
            final @NotNull ByteBuf buf,
            final @NotNull ProtocolVersion version,
            final @NotNull Component component,
            final boolean legacy
    ) {
        if (legacy) {
            writeString(buf, LegacyComponentSerializer.legacySection().serialize(component));
            return;
        }

        if (version.greaterEqual(VanillaProtocolVersion.MINECRAFT_1_20_3)) {
            writeBinaryTag(buf, version, serializeBinaryTag(GsonComponentSerializer.gson().serializeToTree(component)));
        } else {
            writeString(buf, getJsonChatSerializer(version).serialize(component));
        }
    }

    public static BinaryTag serializeBinaryTag(final JsonElement json) {
        if (json instanceof JsonPrimitive jsonPrimitive) {
            if (jsonPrimitive.isNumber()) {
                Number number = json.getAsNumber();

                if (number instanceof Byte) {
                    return ByteBinaryTag.byteBinaryTag((Byte) number);
                } else if (number instanceof Short) {
                    return ShortBinaryTag.shortBinaryTag((Short) number);
                } else if (number instanceof Integer) {
                    return IntBinaryTag.intBinaryTag((Integer) number);
                } else if (number instanceof Long) {
                    return LongBinaryTag.longBinaryTag((Long) number);
                } else if (number instanceof Float) {
                    return FloatBinaryTag.floatBinaryTag((Float) number);
                } else if (number instanceof Double) {
                    return DoubleBinaryTag.doubleBinaryTag((Double) number);
                } else if (number instanceof LazilyParsedNumber) {
                    return IntBinaryTag.intBinaryTag(number.intValue());
                }
            } else if (jsonPrimitive.isString()) {
                return StringBinaryTag.stringBinaryTag(jsonPrimitive.getAsString());
            } else if (jsonPrimitive.isBoolean()) {
                return ByteBinaryTag.byteBinaryTag((byte) (jsonPrimitive.getAsBoolean() ? 1 : 0));
            } else {
                throw new IllegalArgumentException("Unknown JSON primitive: " + jsonPrimitive);
            }
        } else if (json instanceof JsonObject) {
            CompoundBinaryTag.Builder compound = CompoundBinaryTag.builder();

            for (Map.Entry<String, JsonElement> property : ((JsonObject) json).entrySet()) {
                compound.put(property.getKey(), serializeBinaryTag(property.getValue()));
            }

            return compound.build();
        } else if (json instanceof JsonArray) {
            List<JsonElement> jsonArray = ((JsonArray) json).asList();

            if (jsonArray.isEmpty()) {
                return ListBinaryTag.empty();
            }

            List<BinaryTag> tagItems = new ArrayList<>(jsonArray.size());
            BinaryTagType<? extends BinaryTag> listType = null;

            for (JsonElement jsonEl : jsonArray) {
                BinaryTag tag = serializeBinaryTag(jsonEl);
                tagItems.add(tag);

                if (listType == null) {
                    listType = tag.type();
                } else if (listType != tag.type()) {
                    listType = BinaryTagTypes.COMPOUND;
                }
            }

            switch (listType.id()) {
                case 1://BinaryTagTypes.BYTE:
                    byte[] bytes = new byte[jsonArray.size()];
                    for (int i = 0; i < bytes.length; i++) {
                        bytes[i] = jsonArray.get(i).getAsNumber().byteValue();
                    }

                    return ByteArrayBinaryTag.byteArrayBinaryTag(bytes);
                case 3://BinaryTagTypes.INT:
                    int[] ints = new int[jsonArray.size()];
                    for (int i = 0; i < ints.length; i++) {
                        ints[i] = jsonArray.get(i).getAsNumber().intValue();
                    }

                    return IntArrayBinaryTag.intArrayBinaryTag(ints);
                case 4://BinaryTagTypes.LONG:
                    long[] longs = new long[jsonArray.size()];
                    for (int i = 0; i < longs.length; i++) {
                        longs[i] = jsonArray.get(i).getAsNumber().longValue();
                    }

                    return LongArrayBinaryTag.longArrayBinaryTag(longs);
                case 10://BinaryTagTypes.COMPOUND:
                    tagItems.replaceAll(tag -> {
                        if (tag.type() == BinaryTagTypes.COMPOUND) {
                            return tag;
                        } else {
                            return CompoundBinaryTag.builder().put("", tag).build();
                        }
                    });
                    break;
            }

            return ListBinaryTag.listBinaryTag(listType, tagItems);
        }

        return EndBinaryTag.endBinaryTag();
    }

    public static JsonElement deserializeBinaryTag(BinaryTag tag) {
        switch (tag.type().id()) {
            case 1://BinaryTagTypes.BYTE:
                return new JsonPrimitive(((ByteBinaryTag) tag).value());
            case 2://BinaryTagTypes.SHORT:
                return new JsonPrimitive(((ShortBinaryTag) tag).value());
            case 3://BinaryTagTypes.INT:
                return new JsonPrimitive(((IntBinaryTag) tag).value());
            case 4://BinaryTagTypes.LONG:
                return new JsonPrimitive(((LongBinaryTag) tag).value());
            case 5://BinaryTagTypes.FLOAT:
                return new JsonPrimitive(((FloatBinaryTag) tag).value());
            case 6://BinaryTagTypes.DOUBLE:
                return new JsonPrimitive(((DoubleBinaryTag) tag).value());
            case 7://BinaryTagTypes.BYTE_ARRAY:
                byte[] byteArray = ((ByteArrayBinaryTag) tag).value();

                JsonArray jsonByteArray = new JsonArray(byteArray.length);
                for (byte b : byteArray) {
                    jsonByteArray.add(new JsonPrimitive(b));
                }

                return jsonByteArray;
            case 8://BinaryTagTypes.STRING:
                return new JsonPrimitive(((StringBinaryTag) tag).value());
            case 9://BinaryTagTypes.LIST:
                ListBinaryTag items = (ListBinaryTag) tag;
                JsonArray jsonList = new JsonArray(items.size());

                for (BinaryTag subTag : items) {
                    jsonList.add(deserializeBinaryTag(subTag));
                }

                return jsonList;
            case 10://BinaryTagTypes.COMPOUND:
                CompoundBinaryTag compound = (CompoundBinaryTag) tag;
                JsonObject jsonObject = new JsonObject();

                compound.keySet().forEach(key -> {
                    // [{"text":"test1"},"test2"] can't be represented as a binary list tag
                    // it is represented by a list tag with two compound tags
                    // the second compound tag will have an empty key mapped to "test2"
                    // without this fix this would lead to an invalid json component:
                    // [{"text":"test1"},{"":"test2"}]
                    jsonObject.add(key.isEmpty() ? "text" : key, deserializeBinaryTag(compound.get(key)));
                });

                return jsonObject;
            case 11://BinaryTagTypes.INT_ARRAY:
                int[] intArray = ((IntArrayBinaryTag) tag).value();

                JsonArray jsonIntArray = new JsonArray(intArray.length);
                for (int i : intArray) {
                    jsonIntArray.add(new JsonPrimitive(i));
                }

                return jsonIntArray;
            case 12://BinaryTagTypes.LONG_ARRAY:
                long[] longArray = ((LongArrayBinaryTag) tag).value();

                JsonArray jsonLongArray = new JsonArray(longArray.length);
                for (long l : longArray) {
                    jsonLongArray.add(new JsonPrimitive(l));
                }

                return jsonLongArray;
            default:
                throw new IllegalArgumentException("Unknown NBT tag: " + tag);
        }
    }
}