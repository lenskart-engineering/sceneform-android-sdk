package com.google.ar.sceneform.rendering;

import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.ar.sceneform.resources.ResourceRegistry;
import com.google.ar.sceneform.utilities.AndroidPreconditions;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Renders a 3D Model by attaching it to a {@link com.google.ar.sceneform.Node} with {@link
 * com.google.ar.sceneform.Node#setRenderable(Renderable)}.
 *
 * <pre>{@code
 * future = ModelRenderable.builder().setSource(context, R.raw.renderable).build();
 * renderable = future.thenAccept(...);
 * }</pre>
 */

@RequiresApi(api = Build.VERSION_CODES.N)
public class ModelRenderable extends Renderable {
    @Nullable
    private ArrayList<AnimationData> animationData;
    @Nullable
    private SkeletonRig skeletonRig;

    private ModelRenderable(ModelRenderable.Builder builder) {
        super(builder);
        this.animationData = null;
    }

    private ModelRenderable(ModelRenderable other) {
        super(other);
        this.animationData = null;
        this.copyAnimationFrom(other);
    }

    public String getBoneName(int boneIndex) {
        return this.skeletonRig != null ? this.skeletonRig.getBoneName(boneIndex) : "";
    }

    public int getBoneParentIndex(int boneIndex) {
        return this.skeletonRig != null ? this.skeletonRig.getBoneParentIndex(boneIndex) : -1;
    }

    public int getBoneCount() {
        return this.skeletonRig != null ? this.skeletonRig.getBoneCount() : 0;
    }

    private void copyAnimationFrom(ModelRenderable other) {
        if (other.skeletonRig != null) {
            this.skeletonRig = other.skeletonRig.makeCopy();
        }

        if (other.animationData != null) {
            this.animationData = new ArrayList(other.animationData);
        }

    }

    public ModelRenderable makeCopy() {
        return new ModelRenderable(this);
    }

    public static ModelRenderable.Builder builder() {
        AndroidPreconditions.checkMinAndroidApiLevel();
        return new ModelRenderable.Builder();
    }

    @Nullable
    SkeletonRig getSkeletonRig() {
        return this.skeletonRig;
    }

    @Nullable
    public AnimationData getAnimationData(String name) {
        if (this.animationData == null) {
            return null;
        } else {
            Iterator var2 = this.animationData.iterator();

            AnimationData animation;
            do {
                if (!var2.hasNext()) {
                    return null;
                }

                animation = (AnimationData) var2.next();
            } while (!animation.getName().equals(name));

            return animation;
        }
    }

    public AnimationData getAnimationData(int index) {
        if (this.animationData == null) {
            throw new IndexOutOfBoundsException();
        } else {
            return (AnimationData) this.animationData.get(index);
        }
    }

    public int getAnimationDataCount() {
        return this.animationData == null ? 0 : this.animationData.size();
    }

    public void onAnimationEngineUpdated() {
        if (this.skeletonRig != null) {
            this.skeletonRig.updateBoneTransforms();
        }

    }

    void setAnimationData(@Nullable ArrayList<AnimationData> animationData) {
        this.animationData = animationData;
    }

    void setSkeletonRig(@Nullable SkeletonRig skeleton) {
        this.skeletonRig = skeleton;
    }

    public static final class Builder extends com.google.ar.sceneform.rendering.Renderable.Builder<ModelRenderable, ModelRenderable.Builder> {
        public Builder() {
        }

        protected ModelRenderable makeRenderable() {
            return new ModelRenderable(this);
        }

        protected Class<ModelRenderable> getRenderableClass() {
            return ModelRenderable.class;
        }

        protected ResourceRegistry<ModelRenderable> getRenderableRegistry() {
            return ResourceManager.getInstance().getModelRenderableRegistry();
        }

        protected ModelRenderable.Builder getSelf() {
            return this;
        }
    }
}
