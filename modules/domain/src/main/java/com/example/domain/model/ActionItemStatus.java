package com.example.domain.model;

/**
 * Enumeration of possible statuses for an Action Item.
 * <p>
 * <ul>
 *   <li><b>OPEN:</b> The action item is active and requires attention or completion.</li>
 *   <li><b>CLOSE:</b> The action item has been completed, resolved, or closed.</li>
 * </ul>
 *
 * This enum is used to track the lifecycle state of an action item.
 */
public enum ActionItemStatus {
    /** The action item is active and requires attention or completion. */
    OPEN,
    /** The action item has been completed, resolved, or closed. */
    CLOSE
}
