package com.seagate.alto.ui;

import android.graphics.RectF;
import android.util.Log;

import java.util.Random;

public class DigestCellLayouts {
    private static String TAG = DigestCellLayouts.class.getName();
    private static final DigestCellLayout[][] COLLAGE_LAYOUTS = {
            // Single item layouts
            {
                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.6f, 1.0f, 1.0f), // Info Panel - Bottom
                            new RectF[] {
                                    new RectF(0.0f, 0.0f, 1.0f, 1.0f)
                            }),

//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF (0.8f, 0.0f, 1.0f, 1.0f), // Info Panel - Right
//                            new RectF[] {
//                                    new RectF(0.0f, 0.0f, 0.8f, 1.0f)
//                            }),
//
//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF (0.0f, 0.0f, 1.0f, 0.2f), // Info Panel - Top
//                            new RectF[] {
//                                    new RectF(0.0f, 0.2f, 1.0f, 1.0f)
//                            }),
//
//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF (0.0f, 0.0f, 0.2f, 1.0f), // Info Panel - Left
//                            new RectF[] {
//                                    new RectF(0.2f, 0.0f, 1.0f, 1.0f)
//                            })
            },

            // Two item layouts
            {
                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.0f, 0.33f, 1.0f),          // Info Panel TopLeft
                            new RectF[]{
                                    new RectF(0.0f, 0.0f, 0.33f, 1.0f),
                                    new RectF(0.33f, 0.0f, 1.0f, 1.0f)
                            }),

                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.0f, 1.0f, 0.5f),          // Info Panel TopRight
                            new RectF[]{
                                    new RectF(0.0f, 0.0f, 1.0f, 0.5f),
                                    new RectF(0.0f, 0.5f, 1.0f, 1.0f)
                            }),

//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.0f, 0.7f, 0.3f, 1.0f),          // Info Panel BottomLeft
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.3f, 0.7f),
//                                    new RectF(0.3f, 0.0f, 1.0f, 1.0f)
//                            }),
//
//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.7f, 0.7f, 1.0f, 1.0f),          // Info Panel BottomRight
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.7f, 1.0f),
//                                    new RectF(0.7f, 0.0f, 1.0f, 0.7f)
//                            })
            },

            // Three item layouts
            {
                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.33f, 0.5f, 1.0f),          // Info Panel MiddleLeft
                            new RectF[]{
                                    new RectF(0.0f, 0.0f, 0.5f, 0.33f),
                                    new RectF(0.0f, 0.33f, 0.5f, 1.0f),
                                    new RectF(0.5f, 0.0f, 1.0f, 1.0f)
                            }),

//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.7f, 0.0f, 1.0f, 0.3f),          // Info Panel TopRight
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.7f, 0.6f),
//                                    new RectF(0.0f, 0.6f, 0.7f, 1.0f),
//                                    new RectF(0.7f, 0.3f, 1.0f, 1.0f)
//                            }),
//
//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.0f, 0.7f, 0.3f, 1.0f),          // Info Panel BottomLeft
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.3f, 0.7f),
//                                    new RectF(0.3f, 0.0f, 1.0f, 0.7f),
//                                    new RectF(0.3f, 0.7f, 1.0f, 1.0f)
//                            }),

//                    new DigestCellLayout(
//                            //
//                            new RectF(),
//                            new RectF[] {
//                                    new RectF(0.0f, 0.0f, 0.5f, 0.5f),
//                                    new RectF(0.5f, 0.0f, 1.0f, 0.5f),
//                                    new RectF(0.0f, 0.5f, 1.0f, 1.0f)
//                            }),

            },

            // Four item layouts
            {
                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.5f, 0.5f, 1.0f),          // Info Panel TopLeft
                            new RectF[]{
                                    new RectF(0.0f, 0.0f, 0.5f, 0.5f),
                                    new RectF(0.5f, 0.0f, 1.0f, 0.5f),
                                    new RectF(0.0f, 0.5f, 0.5f, 1.0f),
                                    new RectF(0.5f, 0.5f, 1.0f, 1.0f)
                            }),

                    new DigestCellLayout(
                            //        Left, Top, Right, Bottom
                            new RectF(0.0f, 0.5f, 0.7f, 1.0f),          // Info Panel MiddleRight
                            new RectF[]{
                                    new RectF(0.0f, 0.0f, 0.7f, 0.5f),
                                    new RectF(0.0f, 0.5f, 0.7f, 1.0f),
                                    new RectF(0.7f, 0.0f, 1.0f, 0.35f),
                                    new RectF(0.7f, 0.35f, 1.0f, 1.0f)
                            }),

//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.0f, 0.7f, 0.3f, 1.0f),          // Info Panel BottomLeft
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.3f, 0.7f),
//                                    new RectF(0.3f, 0.0f, 1.0f, 0.3f),
//                                    new RectF(0.3f, 0.3f, 1.0f, 0.7f),
//                                    new RectF(0.3f, 0.7f, 1.0f, 1.0f)
//                            }),
//
//                    new DigestCellLayout(
//                            //        Left, Top, Right, Bottom
//                            new RectF(0.7f, 0.0f, 1.0f, 0.3f),          // Info Panel TopRight
//                            new RectF[]{
//                                    new RectF(0.0f, 0.0f, 0.3f, 0.5f),
//                                    new RectF(0.3f, 0.0f, 0.7f, 0.5f),
//                                    new RectF(0.0f, 0.5f, 0.7f, 1.0f),
//                                    new RectF(0.7f, 0.3f, 1.0f, 1.0f)
//                            }),
            },

            {
                    new DigestCellLayout(
                            new RectF(0.3f, 0.3f, 0.7f, 0.7f),
                            new RectF[] {
                                    new RectF(0.0f, 0.0f, 0.7f, 0.3f),
                                    new RectF(0.7f, 0.0f, 1.0f, 0.7f),
                                    new RectF(0.3f, 0.7f, 1.0f, 1.0f),
                                    new RectF(0.0f, 0.3f, 0.3f, 1.0f),
                                    new RectF(0.3f, 0.3f, 0.7f, 0.7f)
                            }
                    ),
            }
    };

    public static DigestCellLayout getLayoutForSize(final int albumSize, final int albumIndex) {
        Log.d(TAG, "getLayoutForSize()");
        final Random mRandom = new Random();
        int bucketIndex = Math.min(albumSize - 1, COLLAGE_LAYOUTS.length - 1);
        int randomIndex = albumIndex % COLLAGE_LAYOUTS[bucketIndex].length;
        bucketIndex = Math.max(0, bucketIndex);
        bucketIndex = Math.min(COLLAGE_LAYOUTS.length, bucketIndex);
        return COLLAGE_LAYOUTS[bucketIndex][randomIndex];
    }
}
