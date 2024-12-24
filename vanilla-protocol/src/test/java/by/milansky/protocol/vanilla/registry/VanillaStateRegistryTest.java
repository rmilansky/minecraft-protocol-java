package by.milansky.protocol.vanilla.registry;

import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.vanilla.standard.ClientboundUpdateHealth;
import by.milansky.protocol.vanilla.standard.ServerboundHandshake;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author milansky
 */
class VanillaStateRegistryTest {
    @Test
    void standardRegistry_shouldCreateValidRegistry() {
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
}

