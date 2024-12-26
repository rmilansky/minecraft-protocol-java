package by.milansky.protocol.vanilla.registry;

import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.vanilla.standard.ClientboundTeam;
import by.milansky.protocol.vanilla.standard.ServerboundHandshake;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author milansky
 */
class VanillaStateRegistryTest {
    @Test
    void standardRegistry_shouldCreateValidHandshakeRegistry() {
        val stateRegistry = VanillaStateRegistry.standardRegistry();
        val registry = stateRegistry.serverbound(ProtocolState.HANDSHAKE);

        assertNotNull(registry);

        for (val version : VanillaProtocolVersion.VALUES) {
            if (version == VanillaProtocolVersion.UNKNOWN) continue;

            val packetClass = registry.getPacketById(version, 0x00);

            assertNotNull(packetClass);
            assertEquals(packetClass, ServerboundHandshake.class);
        }
    }

    @Test
    void standardRegistry_shouldCreateValidPlayRegistry() {
        val stateRegistry = VanillaStateRegistry.standardRegistry();
        val registry = stateRegistry.clientbound(ProtocolState.PLAY);

        assertNotNull(registry);

        val packetMapping = registry.getMapping(ClientboundTeam.class);

        assertNotNull(packetMapping);

        assertEquals(packetMapping.get(VanillaProtocolVersion.MINECRAFT_1_8), 0x3E);
    }
}

