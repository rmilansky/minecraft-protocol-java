package by.milansky.protocol.bukkit.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * @author milansky
 */
public abstract class ProtocolEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    public ProtocolEvent(final boolean async) {
        super(async);
    }

    public ProtocolEvent() {
        super();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public HandlerList getHandlers() {
        return handlers;
    }
}
