package com.lclc98.billboard;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class Configuration {

    private final ForgeConfigSpec spec;

    private final ForgeConfigSpec.ConfigValue<String> imageUrlRegex;

    public Configuration() {

        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Regex to match image url. Recommended to use Imgur for security reasons.");
        this.imageUrlRegex = builder.define("imageUrlRegex", "^https://i\\.imgur\\.com/[a-zA-Z0-9]+\\.(png|jpg|jpeg)$");

        this.spec = builder.build();

        this.save();
    }

    public Pattern getPattern() {

        return Pattern.compile(this.imageUrlRegex.get());
    }

    private void save() {

        final ModConfig modConfig = new ModConfig(ModConfig.Type.SERVER, this.spec, ModLoadingContext.get().getActiveContainer());
        final CommentedFileConfig configData = modConfig.getHandler().reader(FMLPaths.CONFIGDIR.relative()).apply(modConfig);
        final Method setConfigDataMethod = ObfuscationReflectionHelper.findMethod(ModConfig.class, "setConfigData", CommentedConfig.class);

        try {
            setConfigDataMethod.invoke(modConfig, configData);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }

        modConfig.save();
    }
}
