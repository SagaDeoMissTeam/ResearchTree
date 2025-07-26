package net.sixik.researchtree.client;

public enum HideRender {
    NORMAL,
    HIDDEN,
    ALPHA;

    public boolean isRender() {
        return this == NORMAL || this == ALPHA;
    }
}
