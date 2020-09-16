package com.google.ar.sceneform.rendering;

import androidx.annotation.Nullable;

/**
 * @author Akash Verma <akash.v@lenskart.in>
 * @version $Revision 1.0 $, $Date 2020/09/16 16:57 $
 * @since 3.0.4
 */

public abstract class AnimationData {
    private static AnimationData.Factory factory = makeDefaultFactory();

    public AnimationData() {
    }

    @Nullable
    public static AnimationData createInstance(byte[] animationData, String name) {
        return factory.create(animationData, name);
    }

    public static void setFactory(AnimationData.Factory factory) {
        AnimationData.factory = factory;
    }

    public static AnimationData.Factory makeDefaultFactory() {
        return new AnimationData.Factory() {
            @Nullable
            public AnimationData create(byte[] animationData, String name) {
                return new AnimationData() {
                    public String getName() {
                        return "";
                    }

                    public long getDurationMs() {
                        return 0L;
                    }
                };
            }
        };
    }

    public abstract String getName();

    public abstract long getDurationMs();

    public abstract static class Factory {
        public Factory() {
        }

        @Nullable
        public abstract AnimationData create(byte[] animationData, String name);
    }
}
