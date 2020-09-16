package com.google.ar.sceneform.rendering;

import androidx.annotation.Nullable;

import com.google.ar.sceneform.math.Matrix;
import com.google.ar.schemas.lull.Mat4x3;
import com.google.ar.schemas.lull.ModelInstanceDef;
import com.google.ar.schemas.lull.SkeletonDef;
import com.google.ar.schemas.lull.Vec4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * @author Akash Verma <akash.v@lenskart.in>
 * @version $Revision 1.0 $, $Date 2020/09/16 16:57 $
 * @since 3.0.4
 */

public class SkeletonRig {
    private static SkeletonRig.Factory factory = makeDefaultFactory();
    private final SkeletonDef skeletonDef;
    private final ModelInstanceDef modelInstanceDef;
    private final FloatBuffer materialBoneTransformsBuffer;
    protected static final int FLOATS_PER_BONE = 16;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int BYTES_PER_BONE = 64;

    protected SkeletonRig(SkeletonDef skeletonDef, ModelInstanceDef modelInstanceDef) {
        this.skeletonDef = skeletonDef;
        this.modelInstanceDef = modelInstanceDef;
        this.materialBoneTransformsBuffer = makeBufferForBones(this.getMaterialBoneCount());
        this.setBufferToIdentity(this.materialBoneTransformsBuffer);
    }

    protected SkeletonRig(SkeletonRig other) {
        this(other.skeletonDef, other.modelInstanceDef);
    }

    public String getBoneName(int boneIndex) {
        if (boneIndex < this.getBoneCount()) {
            return this.skeletonDef.boneNames(boneIndex);
        } else {
            throw new IndexOutOfBoundsException("boneIndex must be less than boneCount.");
        }
    }

    public int getBoneParentIndex(int boneIndex) {
        if (boneIndex < this.getBoneCount()) {
            int parentIndex = this.skeletonDef.boneParents(boneIndex);
            return parentIndex != 255 ? parentIndex : -1;
        } else {
            throw new IndexOutOfBoundsException("boneIndex must be less than boneCount.");
        }
    }

    public int getBoneCount() {
        return this.skeletonDef.boneNamesLength();
    }

    public void getMatrixForBone(int boneIndex, Matrix dest) {
        Mat4x3 mat4x3 = this.skeletonDef.boneTransforms(boneIndex);
        initializeMatrix(dest.data, mat4x3);
        Matrix.invert(dest, dest);
    }

    public void getInverseBindPoseForBone(int boneIndex, Matrix dest) {
        if (boneIndex >= this.getBoneCount()) {
            int var4 = this.getBoneCount();
            throw new IllegalArgumentException((new StringBuilder(71)).append("boneIndex is out of range. boneIndex=").append(boneIndex).append(", boneCount=").append(var4).toString());
        } else {
            Mat4x3 mat4x3 = this.skeletonDef.boneTransforms(boneIndex);
            initializeMatrix(dest.data, mat4x3);
        }
    }

    public int getMaterialBoneCount() {
        return this.modelInstanceDef.shaderToMeshBonesLength();
    }

    public int getMaterialBoneIndex(int index) {
        if (index >= this.getMaterialBoneCount()) {
            int var2 = this.getMaterialBoneCount();
            throw new IllegalArgumentException((new StringBuilder(71)).append("index is out of range. index=").append(index).append(", materialBoneCount=").append(var2).toString());
        } else {
            return this.modelInstanceDef.shaderToMeshBones(index);
        }
    }

    public void updateBoneTransforms() {
    }

    public FloatBuffer getMaterialBoneTransformsBuffer() {
        return this.materialBoneTransformsBuffer;
    }

    public boolean isAnimating(ModelRenderable modelRenderable) {
        return false;
    }

    public SkeletonRig makeCopy() {
        return new SkeletonRig(this);
    }

    private void setBufferToIdentity(FloatBuffer buffer) {
        buffer.rewind();

        while (buffer.hasRemaining()) {
            buffer.put(Matrix.IDENTITY_DATA);
        }

        buffer.rewind();
    }

    protected static FloatBuffer makeBufferForBones(int numBones) {
        ByteBuffer rawBuffer = ByteBuffer.allocateDirect(numBones * 64).order(ByteOrder.nativeOrder());
        return rawBuffer.asFloatBuffer();
    }

    protected static void initializeMatrix(float[] matArray, Mat4x3 mat) {
        Vec4 row = new Vec4();
        initializeRow(matArray, 0, mat.c0(row));
        initializeRow(matArray, 1, mat.c1(row));
        initializeRow(matArray, 2, mat.c2(row));
        matArray[15] = 1.0F;
    }

    protected static void initializeRow(float[] matData, int offset, Vec4 row) {
        matData[0 + offset] = row.x();
        matData[4 + offset] = row.y();
        matData[8 + offset] = row.z();
        matData[12 + offset] = row.w();
    }

    @Nullable
    public static SkeletonRig createInstance(SkeletonDef skeletonDef, ModelInstanceDef modelInstanceDef) {
        return factory.create(skeletonDef, modelInstanceDef);
    }

    public static void setFactory(SkeletonRig.Factory factory) {
        SkeletonRig.factory = factory;
    }

    public static SkeletonRig.Factory makeDefaultFactory() {
        return new SkeletonRig.Factory() {
            @Nullable
            public SkeletonRig create(SkeletonDef skeletonDef, ModelInstanceDef modelInstanceDef) {
                return new SkeletonRig(skeletonDef, modelInstanceDef);
            }
        };
    }

    public abstract static class Factory {
        public Factory() {
        }

        @Nullable
        public abstract SkeletonRig create(SkeletonDef skeletonDef, ModelInstanceDef modelInstanceDef);
    }
}
