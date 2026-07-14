package io.github.pepe3012.arcadia.mixin.client;

import io.github.pepe3012.arcadia.common.component.world.bloodmoon.Bloodmoon;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SkyRenderer;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Environment(EnvType.CLIENT)
@Mixin(SkyRenderer.class)
public abstract class SkyRendererMixin {

    @ModifyArg(method = "renderMoon", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/DynamicUniforms;writeTransform(Lorg/joml/Matrix4f;Lorg/joml/Vector4f;)Lcom/mojang/blaze3d/buffers/GpuBufferSlice;"), index = 1)
    private Vector4f arcadia$renderMoon(Vector4f colorModulator) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || !Bloodmoon.isBloodmoonActive(minecraft.level)) return colorModulator;
        return colorModulator.set(Bloodmoon.MOON_COLOR.x(), Bloodmoon.MOON_COLOR.y(), Bloodmoon.MOON_COLOR.z(), colorModulator.w());
    }
}