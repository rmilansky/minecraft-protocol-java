package by.milansky.protocol.vanilla.registry;

import by.milansky.protocol.api.state.ProtocolState;
import by.milansky.protocol.base.packet.registry.BaseStateRegistry;
import by.milansky.protocol.base.packet.registry.BaseSuppliedPacketRegistry;
import by.milansky.protocol.base.version.UnmodifiableVersionMapping;
import by.milansky.protocol.vanilla.standard.ClientboundTeam;
import by.milansky.protocol.vanilla.standard.ClientboundUpdateHealth;
import by.milansky.protocol.vanilla.standard.ServerboundHandshake;
import by.milansky.protocol.vanilla.standard.ServerboundTabcomplete;
import by.milansky.protocol.vanilla.version.VanillaProtocolVersion;
import lombok.extern.log4j.Log4j2;

/**
 * @author milansky
 */
@Log4j2
public final class VanillaStateRegistry extends BaseStateRegistry {
    private VanillaStateRegistry() {
        registerServerboundState(ProtocolState.HANDSHAKE, BaseSuppliedPacketRegistry.create(registry -> {
            registry.register(ServerboundHandshake.class,
                    UnmodifiableVersionMapping.createMapping(VanillaProtocolVersion.MINECRAFT_1_7_2, 0x00));
        }));

        registerServerboundState(ProtocolState.PLAY, BaseSuppliedPacketRegistry.create(registry -> {
            registry.register(ServerboundTabcomplete.class,
                    UnmodifiableVersionMapping.createMapping(VanillaProtocolVersion.MINECRAFT_1_8, 0x14,
                            VanillaProtocolVersion.MINECRAFT_1_9, 0x01, VanillaProtocolVersion.MINECRAFT_1_12, 0x02,
                            VanillaProtocolVersion.MINECRAFT_1_12_1, 0x01, VanillaProtocolVersion.MINECRAFT_1_13, 0x05,
                            VanillaProtocolVersion.MINECRAFT_1_14, 0x06, VanillaProtocolVersion.MINECRAFT_1_19, 0x08,
                            VanillaProtocolVersion.MINECRAFT_1_19_1, 0x09, VanillaProtocolVersion.MINECRAFT_1_19_3, 0x08,
                            VanillaProtocolVersion.MINECRAFT_1_19_4, 0x09, VanillaProtocolVersion.MINECRAFT_1_20_2, 0x0A,
                            VanillaProtocolVersion.MINECRAFT_1_20_5, 0x0B, VanillaProtocolVersion.MINECRAFT_1_21_2, 0x0D
                    ));
        }));

        registerClientboundState(ProtocolState.PLAY, BaseSuppliedPacketRegistry.create(registry -> {
            registry.register(ClientboundUpdateHealth.class,
                    UnmodifiableVersionMapping.createMapping(VanillaProtocolVersion.MINECRAFT_1_8, 0x41));

            registry.register(ClientboundTeam.class,
                    UnmodifiableVersionMapping.createMapping(VanillaProtocolVersion.MINECRAFT_1_8, 0x3E,
                            VanillaProtocolVersion.MINECRAFT_1_9, 0x41, VanillaProtocolVersion.MINECRAFT_1_12, 0x43,
                            VanillaProtocolVersion.MINECRAFT_1_12_1, 0x44, VanillaProtocolVersion.MINECRAFT_1_13, 0x47,
                            VanillaProtocolVersion.MINECRAFT_1_14, 0x4B, VanillaProtocolVersion.MINECRAFT_1_15, 0x4C,
                            VanillaProtocolVersion.MINECRAFT_1_17, 0x55, VanillaProtocolVersion.MINECRAFT_1_19_1, 0x58,
                            VanillaProtocolVersion.MINECRAFT_1_19_3, 0x56, VanillaProtocolVersion.MINECRAFT_1_19_4, 0x5A,
                            VanillaProtocolVersion.MINECRAFT_1_20_2, 0x5C, VanillaProtocolVersion.MINECRAFT_1_20_3, 0x5E,
                            VanillaProtocolVersion.MINECRAFT_1_20_5, 0x60, VanillaProtocolVersion.MINECRAFT_1_21_2, 0x67));
        }));
    }

    public static VanillaStateRegistry standardRegistry() {
        return new VanillaStateRegistry();
    }
}
