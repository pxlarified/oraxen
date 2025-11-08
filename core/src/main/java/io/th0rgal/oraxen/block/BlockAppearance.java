package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockAppearance {

    private final String modelName;
    private final String parentModel;
    private final String texture;
    private final boolean generateModel;
    private final boolean customRender;

    public BlockAppearance(String modelName, String parentModel, String texture, 
                          boolean generateModel, boolean customRender) {
        this.modelName = modelName;
        this.parentModel = parentModel;
        this.texture = texture;
        this.generateModel = generateModel;
        this.customRender = customRender;
    }

    @NotNull
    public String getModelName() {
        return modelName != null ? modelName : "";
    }

    @Nullable
    public String getParentModel() {
        return parentModel;
    }

    @Nullable
    public String getTexture() {
        return texture;
    }

    public boolean shouldGenerateModel() {
        return generateModel;
    }

    public boolean isCustomRender() {
        return customRender;
    }

    public static class Builder {
        private String modelName;
        private String parentModel = "block/cube_all";
        private String texture;
        private boolean generateModel = false;
        private boolean customRender = false;

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder parentModel(String parentModel) {
            this.parentModel = parentModel;
            return this;
        }

        public Builder texture(String texture) {
            this.texture = texture;
            return this;
        }

        public Builder generateModel(boolean generateModel) {
            this.generateModel = generateModel;
            return this;
        }

        public Builder customRender(boolean customRender) {
            this.customRender = customRender;
            return this;
        }

        public BlockAppearance build() {
            return new BlockAppearance(modelName, parentModel, texture, generateModel, customRender);
        }
    }
}
