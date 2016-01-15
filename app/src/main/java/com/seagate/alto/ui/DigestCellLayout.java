package com.seagate.alto.ui;

import android.graphics.Rect;
import android.graphics.RectF;

public class DigestCellLayout {
    private RectF mInfoPanel;
    private final RectF[] mThumbnailPanels;

    public DigestCellLayout(final RectF[] thumbnailPanels) {
        this(null, thumbnailPanels);
    }

    public DigestCellLayout(final RectF infoPanel, final RectF[] thumbnailPanels) {
        mInfoPanel = infoPanel;
        mThumbnailPanels = thumbnailPanels;
    }

    public int getPanelCount() {
        return mThumbnailPanels == null ? 0 : mThumbnailPanels.length;
    }

    public RectF getPanel(final int index) {
        return mThumbnailPanels[index];
    }

    public boolean hasInfoPanel() {
        return mInfoPanel != null;
    }

    public Rect getInfoRect(final Rect bounds) {
        if (hasInfoPanel()) {
            return new Rect(
                    Math.round(bounds.left + bounds.width() * mInfoPanel.left),
                    Math.round(bounds.top + bounds.height() * mInfoPanel.top),
                    Math.round(bounds.left + bounds.width() * mInfoPanel.right),
                    Math.round(bounds.top + bounds.height() * mInfoPanel.bottom)
            );
        }

        return null;
    }

    public Rect getPanelRect(final int panelIndex, final Rect bounds) {
        RectF panelRect = getPanel(panelIndex);

        return new Rect(
                Math.round(bounds.left + bounds.width() * panelRect.left),
                Math.round(bounds.top + bounds.height() * panelRect.top),
                Math.round(bounds.left + bounds.width() * panelRect.right),
                Math.round(bounds.top + bounds.height() * panelRect.bottom)
        );
    }
}
