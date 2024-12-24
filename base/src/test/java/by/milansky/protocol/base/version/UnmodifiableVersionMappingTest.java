package by.milansky.protocol.base.version;

import by.milansky.protocol.api.version.ProtocolVersion;
import by.milansky.protocol.base.throwable.UnsupportedMappingException;
import by.milansky.protocol.base.throwable.UnsupportedPacketException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UnmodifiableVersionMappingTest {
    @Getter
    @RequiredArgsConstructor
    @Accessors(chain = true, fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private enum TestProtocolVersion implements ProtocolVersion {
        MINECRAFT_1_7_2(5),
        MINECRAFT_1_7_6(6),
        MINECRAFT_1_8(47),
        MINECRAFT_1_9(107),
        MINECRAFT_1_12(335),
        MINECRAFT_1_20(763),
        MINECRAFT_1_21_4(1000);

        int protocol;

        @Override
        public ProtocolVersion next() {
            int ordinal = this.ordinal();
            return ordinal < TestProtocolVersion.values().length - 1
                    ? TestProtocolVersion.values()[ordinal + 1]
                    : null;
        }
    }

    @Test
    void createMapping_shouldCreateValidMapping() {
        val mappings = new Object[]{
                TestProtocolVersion.MINECRAFT_1_7_2, 1,
                TestProtocolVersion.MINECRAFT_1_8, 2,
                TestProtocolVersion.MINECRAFT_1_9, 3,
                TestProtocolVersion.MINECRAFT_1_12, 4,
                TestProtocolVersion.MINECRAFT_1_20, 5,
                TestProtocolVersion.MINECRAFT_1_21_4
        };

        val versionMapping = UnmodifiableVersionMapping.createMapping(mappings);

        assertNotNull(versionMapping);

        assertEquals(1, versionMapping.get(TestProtocolVersion.MINECRAFT_1_7_2));
        assertEquals(2, versionMapping.get(TestProtocolVersion.MINECRAFT_1_8));
        assertEquals(3, versionMapping.get(TestProtocolVersion.MINECRAFT_1_9));
        assertEquals(4, versionMapping.get(TestProtocolVersion.MINECRAFT_1_12));
        assertEquals(5, versionMapping.get(TestProtocolVersion.MINECRAFT_1_20));

        assertThrows(UnsupportedPacketException.class, () -> versionMapping.get(TestProtocolVersion.MINECRAFT_1_21_4));
    }

    @Test
    void createMapping_withIntermediateVersions_shouldFillAll() {
        val mappings = new Object[]{
                TestProtocolVersion.MINECRAFT_1_7_2, 1,
                TestProtocolVersion.MINECRAFT_1_7_6, 2
        };

        val versionMapping = UnmodifiableVersionMapping.createMapping(mappings);

        assertNotNull(versionMapping);

        for (val version : TestProtocolVersion.values()) {
            if (version.lower(TestProtocolVersion.MINECRAFT_1_7_6) &&
                    version.greaterEqual(TestProtocolVersion.MINECRAFT_1_7_2)) {
                assertEquals(1, versionMapping.get(version));
            }
        }

        assertEquals(2, versionMapping.get(TestProtocolVersion.MINECRAFT_1_7_6));
    }

    @Test
    void createMapping_invalidInput_shouldThrowException() {
        val invalidMappings = new Object[]{
                "Invalid", 1,
                TestProtocolVersion.MINECRAFT_1_8, "Invalid"
        };

        assertThrows(UnsupportedMappingException.class,
                () -> UnmodifiableVersionMapping.createMapping(invalidMappings));
    }

    @Test
    void createMapping_emptyInput_shouldReturnEmptyMapping() {
        val versionMapping = UnmodifiableVersionMapping.createMapping();

        assertNotNull(versionMapping);
        assertTrue(versionMapping.asVersionMap().isEmpty());
    }

    @Test
    void asVersionMap_shouldReturnCorrectUnmodifiableMap() {
        val mappings = new Object[]{
                TestProtocolVersion.MINECRAFT_1_8, 2
        };

        val versionMapping = UnmodifiableVersionMapping.createMapping(mappings);
        val map = versionMapping.asVersionMap();

        assertNotNull(map);

        assertEquals(2, map.get(TestProtocolVersion.MINECRAFT_1_8));

        assertThrows(UnsupportedOperationException.class, () -> map.put(TestProtocolVersion.MINECRAFT_1_8, 1));
    }
}
