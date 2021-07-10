package com.google.ar.sceneform;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.ar.sceneform.math.Matrix;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.rendering.SkeletonRig;
import com.google.ar.sceneform.utilities.Preconditions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.HashMap;

/**
 * @author Akash Verma <akash.v@lenskart.in>
 * @version $Revision 1.0 $, $Date 2020/09/16 16:57 $
 * @since 3.0.4
 */

public class SkeletonNode extends Node {
    private final HashMap<String, NodeBinding> boneNamesToNodes = new HashMap();
    private final Matrix boneTransform = new Matrix();
    private final Vector3 bonePosition = new Vector3();
    private final Vector3 boneScale = new Vector3();
    private final Quaternion boneRotation = new Quaternion();
    @VisibleForTesting
    final SkeletonNode.SkeletonSkinningModifier skinningModifier = new SkeletonNode.SkeletonSkinningModifier();
    private boolean isModifiedSinceLastRender = false;

    public SkeletonNode() {
    }

    @Nullable
    public Node getBoneAttachment(String var1) {
        Preconditions.checkNotNull(var1, "Parameter \"boneName\" was null.");
        SkeletonNode.NodeBinding var2;
        return (var2 = (SkeletonNode.NodeBinding) this.boneNamesToNodes.get(var1)) != null ? var2.a : null;
    }

    public void setBoneAttachment(String var1, @Nullable Node var2) {
        Preconditions.checkNotNull(var1, "Parameter \"boneName\" was null.");
        SkeletonNode.NodeBinding var3;
        if ((var3 = (SkeletonNode.NodeBinding) this.boneNamesToNodes.remove(var1)) != null) {
            var3.a();
        }

        if (var2 != null) {
            SkeletonNode.NodeBinding var4 = new SkeletonNode.NodeBinding(var2);
            this.boneNamesToNodes.put(var1, var4);
            int var5;
            if ((var5 = this.getIndexForBoneName(var1)) != -1) {
                this.applyBoneTransformToNode(var5, var4);
            }
        }

    }

    public void setRenderable(@Nullable Renderable var1) {
        super.setRenderable(var1);
        this.updateMappedNodes();
        RenderableInstance var2;
        if ((var2 = this.getRenderableInstance()) != null) {
            var2.setSkinningModifier(this.skinningModifier);
        }

    }

    public void onUpdate(FrameTime var1) {
        if (this.isAnimating()) {
            this.updateMappedNodes();
        }

    }

    private void updateMappedNodes() {
        ModelRenderable var1;
        if ((var1 = this.getModelRenderable()) != null) {
            if (this.getSkeletonRig() != null) {
                for (int var3 = 0; var3 < var1.getBoneCount(); ++var3) {
                    SkeletonNode.NodeBinding var4;
                    if ((var4 = (SkeletonNode.NodeBinding) this.boneNamesToNodes.get(var1.getBoneName(var3))) != null && var4.a.isActive()) {
                        this.applyBoneTransformToNode(var3, var4);
                    }
                }

                this.isModifiedSinceLastRender = false;
            }
        }
    }

    private void applyBoneTransformToNode(int var1, SkeletonNode.NodeBinding var2) {
        SkeletonRig var3;
        if ((var3 = this.getSkeletonRig()) != null) {
            RenderableInstance var4;
            if ((var4 = this.getRenderableInstance()) != null) {
                var3.getMatrixForBone(var1, this.boneTransform);
                Matrix var5;
                if ((var5 = var4.getRelativeTransform()) != null) {
                    Matrix.multiply(var5, this.boneTransform, this.boneTransform);
                }

                Node var6;
                Node var7;
                if ((var7 = (var6 = var2.a).getParent()) != null && var7 != this) {
                    Matrix.multiply(this.getWorldModelMatrix(), this.boneTransform, this.boneTransform);
                    Matrix.multiply(var7.getWorldModelMatrixInverseInternal(), this.boneTransform, this.boneTransform);
                }

                this.boneTransform.decomposeTranslation(this.bonePosition);
                this.boneTransform.decomposeScale(this.boneScale);
                this.boneTransform.decomposeRotation(this.boneScale, this.boneRotation);
                boolean var8 = this.isModifiedSinceLastRender;
                var6.setLocalPosition(this.bonePosition);
                var6.setLocalRotation(this.boneRotation);
                var6.setLocalScale(this.boneScale);
                var2.b = false;
                this.isModifiedSinceLastRender = var8;
            }
        }
    }

    private int getIndexForBoneName(String var1) {
        ModelRenderable var2;
        if ((var2 = this.getModelRenderable()) == null) {
            return -1;
        } else {
            for (int var3 = 0; var3 < var2.getBoneCount(); ++var3) {
                if (var2.getBoneName(var3).equals(var1)) {
                    return var3;
                }
            }

            return -1;
        }
    }

    @Nullable
    private ModelRenderable getModelRenderable() {
        Renderable var1;
        return (var1 = this.getRenderable()) instanceof ModelRenderable ? (ModelRenderable) var1 : null;
    }

    private boolean isAnimating() {
        ModelRenderable var1;
        if ((var1 = this.getModelRenderable()) == null) {
            return false;
        } else {
            SkeletonRig var2;
            return (var2 = this.getSkeletonRig()) == null ? false : var2.isAnimating(var1);
        }
    }

    @VisibleForTesting
    class SkeletonSkinningModifier implements RenderableInstance.SkinningModifier {
        @Nullable
        private FloatBuffer a = null;
        private final Matrix b = new Matrix();

        SkeletonSkinningModifier() {
        }

        public FloatBuffer modifyMaterialBoneTransformsBuffer(FloatBuffer var1) {
            SkeletonRig var2;
            if ((var2 = SkeletonNode.this.getSkeletonRig()) == null) {
                return var1;
            } else {
                ModelRenderable var3;
                if ((var3 = SkeletonNode.this.getModelRenderable()) == null) {
                    return var1;
                } else {
                    FloatBuffer var4 = null;

                    for (int var5 = 0; var5 < var2.getMaterialBoneCount(); ++var5) {
                        int var6 = var2.getMaterialBoneIndex(var5);
                        String var7 = var3.getBoneName(var6);
                        SkeletonNode.NodeBinding var8;
                        if ((var8 = (SkeletonNode.NodeBinding) SkeletonNode.this.boneNamesToNodes.get(var7)) != null && var8.a.isActive() && var8.b) {
                            if (var4 == null) {
                                if (this.a == null || this.a.limit() != var1.limit()) {
                                    ByteBuffer var11 = ByteBuffer.allocateDirect(var1.limit() << 2).order(ByteOrder.nativeOrder());
                                    this.a = var11.asFloatBuffer();
                                }

                                FloatBuffer var20;
                                (var20 = (FloatBuffer) Preconditions.checkNotNull(this.a)).put(var1);
                                var1.rewind();
                                var4 = var20;
                            }

                            Node var12 = var8.a;
                            SkeletonRig var14;
                            RenderableInstance var15;
                            if ((var14 = SkeletonNode.this.getSkeletonRig()) != null && (var15 = SkeletonNode.this.getRenderableInstance()) != null) {
                                var14.getInverseBindPoseForBone(var6, this.b);
                                Matrix var16;
                                Node var17;
                                if ((var17 = var12.getParent()) != null && var17 != SkeletonNode.this) {
                                    Matrix.multiply(SkeletonNode.this.getWorldModelMatrixInverseInternal(), var12.getWorldModelMatrix(), SkeletonNode.this.boneTransform);
                                    var16 = SkeletonNode.this.boneTransform;
                                } else {
                                    var16 = var12.getLocalModelMatrixInternal();
                                }

                                Matrix var18;
                                if ((var18 = var15.getRelativeTransformInverse()) != null) {
                                    Matrix.multiply(var18, var16, SkeletonNode.this.boneTransform);
                                    var16 = SkeletonNode.this.boneTransform;
                                }

                                Matrix.multiply(var16, this.b, SkeletonNode.this.boneTransform);
                                int var19 = var5 << 4;
                                if (var4.position() != var19) {
                                    var4.position(var19);
                                }

                                var4.put(SkeletonNode.this.boneTransform.data);
                            }
                        }
                    }

                    SkeletonNode.this.isModifiedSinceLastRender = false;
                    if (var4 != null) {
                        var4.rewind();
                        return var4;
                    } else {
                        this.a = null;
                        return var1;
                    }
                }
            }
        }

        public boolean isModifiedSinceLastRender() {
            return SkeletonNode.this.isModifiedSinceLastRender;
        }
    }

    class NodeBinding implements TransformChangedListener {
        final Node a;
        boolean b;

        private NodeBinding(Node var2) {
            this.b = true;
            this.a = var2;
            var2.addTransformChangedListener(this);
        }

        private final void a() {
            this.a.removeTransformChangedListener(this);
        }

        public void onTransformChanged(Node var1, Node var2) {
            if (var2 != SkeletonNode.this && !SkeletonNode.this.isDescendantOf(var2) || !var1.isDescendantOf(SkeletonNode.this)) {
                this.b = true;
                SkeletonNode.this.isModifiedSinceLastRender = true;
            }

        }
    }
}
