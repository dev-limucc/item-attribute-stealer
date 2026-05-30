package dev.limucc.itemattributestealer.client.config;

/**
 * Plain-old-Java-object that holds all mod settings.
 * This is serialised to / deserialised from JSON by ConfigManager.
 *
 * Learning note: keeping config as a POJO makes it easy to save with any
 * JSON library and to integrate with Cloth Config's builder API.
 */
public class ModConfig {

    // ── General ──────────────────────────────────────────────────────────────

    /** Master switch: when false the mod does nothing at all. */
    public boolean enabled = false;

    // ── Slot selection ───────────────────────────────────────────────────────

    /** Hotbar slot (0–8) treated as "Weapon A" — the weapon you normally hold. */
    public int weaponSlotA = 0;

    /** Hotbar slot (0–8) treated as "Weapon B" — the weapon swapped to-and-from. */
    public int weaponSlotB = 1;

    /**
     * When true, the swap only fires if both slots actually contain items
     * (basic weapon check: not empty).  Prevents accidental swaps.
     */
    public boolean requireBothSlotsFilled = true;

    // ── Feedback ─────────────────────────────────────────────────────────────

    /** Whether to show any visual feedback on a successful swap. */
    public boolean showMessage = true;

    /** How to show the "Magic attribute merge has been used!" message. */
    public FeedbackStyle feedbackStyle = FeedbackStyle.ACTION_BAR;

    // ── Inner types ──────────────────────────────────────────────────────────

    public enum FeedbackStyle {
        /** Appears above the hotbar — unobtrusive. */
        ACTION_BAR,
        /** Appears in the chat window. */
        CHAT,
        /** Small toast notification in the top-right corner. */
        TOAST
    }
}
