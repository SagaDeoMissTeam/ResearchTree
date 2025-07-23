package net.sixik.researchtree.client.render;

public class RenderTooltip {

    protected int tooltipSizeX;
    protected int tooltipSizeY;

    public RenderTooltip(int tooltipSizeX, int tooltipSizeY) {
        this.tooltipSizeX = tooltipSizeX;
        this.tooltipSizeY = tooltipSizeY;
    }

    public int getTooltipSizeX() {
        return tooltipSizeX;
    }

    public int getTooltipSizeY() {
        return tooltipSizeY;
    }

    public void setTooltipSizeX(int tooltipSizeX) {
        this.tooltipSizeX = tooltipSizeX;
    }

    public void setTooltipSizeY(int tooltipSizeY) {
        this.tooltipSizeY = tooltipSizeY;
    }
}
