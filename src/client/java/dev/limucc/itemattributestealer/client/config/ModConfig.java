package dev.limucc.itemattributestealer.client.config;

/**
 * All mod settings. Serialised to JSON by ConfigManager.
 * New fields automatically get their defaults on first load (Gson skips missing fields).
 */
public class ModConfig {

    // ── Master toggle (also flipped by the keybind) ───────────────────────────
    public boolean enabled = false;

    // ── Mode ──────────────────────────────────────────────────────────────────
    /** Which swap strategy to use. */
    public Mode mode = Mode.SLOT;

    public enum Mode { WEAPON, SLOT }

    // ── Weapon Mode ───────────────────────────────────────────────────────────

    /** The item type you hold when you want the swap to fire. */
    public HeldKind heldKind = HeldKind.ANY;

    /** The item type whose attributes are merged into your attack. Must be in your hotbar. */
    public CopyKind copyKind = CopyKind.SWORD;

    /**
     * When true: if your hand is empty, still do the swap (to any tool in your hotbar).
     * Lets you "use" a tool's damage without spending its durability.
     */
    public boolean emptyHandUsage = false;

    /**
     * When true: breaking a crop auto-swaps to the highest-Fortune tool in your hotbar
     * for the instant break, then swaps back. Crops are one-tick breaks, so no durability
     * is spent on the Fortune tool.
     */
    public boolean useFortuneForCrops = false;

    // ── Slot Mode ─────────────────────────────────────────────────────────────
    /** Hotbar slot shown to user as 1–9 (stored as 1–9, converted to 0–8 internally). */
    public int slotA = 1;
    public int slotB = 2;

    // ── Enums ─────────────────────────────────────────────────────────────────

    /**
     * Item type the player is HOLDING to trigger the swap.
     * ANY = any tool (not a placeable block).
     */
    public enum HeldKind {
        ANY, SWORD, AXE, MACE, TRIDENT, PICKAXE, SHOVEL, HOE
    }

    /**
     * Item type whose attributes are COPIED. No ANY option — must be a specific type
     * that can be found in the hotbar.
     */
    public enum CopyKind {
        SWORD, AXE, MACE, TRIDENT, PICKAXE, SHOVEL, HOE
    }
}
