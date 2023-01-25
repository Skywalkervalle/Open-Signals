package com.troblecodings.signals.models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Matrix4f;
import com.mojang.math.Quaternion;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.troblecodings.signals.OpenSignalsMain;
import com.troblecodings.signals.core.SignalAngel;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.SimpleModelState;
import net.minecraftforge.client.model.data.EmptyModelData;

@OnlyIn(Dist.CLIENT)
public class SignalCustomModel implements UnbakedModel {

    public static final Random RANDOM = new Random();

    private final SignalAngel angel;
    private final List<SignalModelLoaderInfo> list;
    private final List<ResourceLocation> dependencies;

    public SignalCustomModel(SignalAngel angel, List<SignalModelLoaderInfo> list) {
        super();
        this.angel = angel;
        this.list = list;
        this.dependencies = list.stream()
                .map(info -> new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name))
                .collect(Collectors.toUnmodifiableList());
    }

    private static void transform(BakedQuad quad, Matrix4f quaterion) {
        final int[] oldVertex = quad.getVertices();

        final int size = DefaultVertexFormat.BLOCK.getIntegerSize();
        for (int i = 0; i < oldVertex.length; i += size) {
            float x = Float.intBitsToFloat(oldVertex[i]);
            float y = Float.intBitsToFloat(oldVertex[i + 1]);
            float z = Float.intBitsToFloat(oldVertex[i + 2]);
            final Vector4f vector = new Vector4f(x, y, z, 1);
            vector.transform(quaterion);
            oldVertex[i + 0] = Float.floatToIntBits(vector.x());
            oldVertex[i + 1] = Float.floatToIntBits(vector.y());
            oldVertex[i + 2] = Float.floatToIntBits(vector.z());
        }
    }

    private static BakedModelPair transform(final SignalModelLoaderInfo info,
            final ModelBakery bakery, ResourceLocation location,
            Function<Material, TextureAtlasSprite> function, Quaternion rotation) {
        final Transformation transformation = new Transformation(
                new Vector3f(info.x, info.y, info.z), null, null, null);
        final BakedModel model = info.model.bake(bakery, function,
                new SimpleModelState(transformation), location);
        final Matrix4f reverse = new Matrix4f();
        reverse.setIdentity();
        reverse.multiplyWithTranslation(-0.5f, 0, -0.5f);

        final Matrix4f matrix = Matrix4f.createScaleMatrix(1, 1, 1);
        matrix.multiplyWithTranslation(0.5f, 0, 0.5f);
        matrix.multiply(rotation);
        matrix.multiply(reverse);

        model.getQuads(null, null, RANDOM, EmptyModelData.INSTANCE)
                .forEach(quad -> transform(quad, matrix));
        for (Direction direction : Direction.values()) {
            model.getQuads(null, direction, RANDOM, EmptyModelData.INSTANCE)
                    .forEach(quad -> transform(quad, matrix));
        }
        return new BakedModelPair(info.state, model);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return this.dependencies;
    }

    @Override
    public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> function,
            Set<Pair<String, String>> modelState) {
        List<Material> material = new ArrayList<>();
        this.dependencies.forEach(location -> material
                .addAll(function.apply(location).getMaterials(function, modelState)));
        return material;
    }

    @Override
    public BakedModel bake(ModelBakery bakery, Function<Material, TextureAtlasSprite> function,
            ModelState state, ResourceLocation resource) {
        list.forEach(info -> {
            if (info.model == null) {
                info.model = bakery.getModel(
                        new ResourceLocation(OpenSignalsMain.MODID, "block/" + info.name));
            }
        });
        final Quaternion quaternion = angel.getQuaternion();
        return new SignalBakedModel(
                list.stream().map(info -> transform(info, bakery, resource, function, quaternion))
                        .collect(Collectors.toUnmodifiableList()));
    }

}