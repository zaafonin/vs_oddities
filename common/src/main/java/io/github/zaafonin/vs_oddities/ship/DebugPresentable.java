package io.github.zaafonin.vs_oddities.ship;

import net.minecraft.client.gui.components.DebugScreenOverlay;

import java.util.List;

/**
 * Implement this interface to export info about your attachment to the F3 overlay.
 */
public interface DebugPresentable {
    /**
     * This method is called from a mixin to the F3 screen (see {@link DebugScreenOverlay#getSystemInformation} for the original code).
     * Do not put expensive calculations there, as this method is frequently called.
     * @param lines List of strings, newlines are added implicitly when printed by {@link DebugScreenOverlay#renderLines}.
     */
    void addDebugLines(List<String> lines);
}
