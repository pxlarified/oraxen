package io.th0rgal.oraxen.pack.generation.validator;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import io.th0rgal.oraxen.utils.VirtualFile;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ResourcePackValidator {

    private final List<VirtualFile> files;
    private final ValidatorReport report;
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile("[a-z0-9_-]+");
    private static final Pattern VALID_PATH_PATTERN = Pattern.compile("[a-z0-9/._-]+");

    public ResourcePackValidator(List<VirtualFile> files) {
        this.files = files;
        this.report = new ValidatorReport();
    }

    public ValidatorReport validate() {
        Logs.logInfo("Starting resource pack validation...");

        validateJsonSyntax();
        validateYamlSyntax();
        validateTextureReferences();
        validateTextureResolutions();
        validatePathNaming();
        validateDuplicateNamespaces();
        validateMiniMessageFormatting();
        validateModelHierarchy();

        return report;
    }

    private void validateJsonSyntax() {
        Set<VirtualFile> jsonFiles = files.stream()
                .filter(f -> f.getPath().endsWith(".json"))
                .collect(Collectors.toSet());

        for (VirtualFile file : jsonFiles) {
            try {
                String content = readFileContent(file);
                if (!content.isEmpty()) {
                    JsonParser.parseString(content);
                }
            } catch (JsonSyntaxException e) {
                report.addIssue(ValidatorReport.Severity.ERROR,
                        "Invalid JSON syntax: " + e.getMessage(),
                        file.getPath());
            } catch (Exception e) {
                report.addIssue(ValidatorReport.Severity.WARNING,
                        "Failed to validate JSON: " + e.getClass().getSimpleName(),
                        file.getPath());
            }
        }
    }

    private void validateYamlSyntax() {
        Set<VirtualFile> yamlFiles = files.stream()
                .filter(f -> f.getPath().endsWith(".yml") || f.getPath().endsWith(".yaml"))
                .collect(Collectors.toSet());

        Yaml yaml = new Yaml();
        for (VirtualFile file : yamlFiles) {
            try {
                String content = readFileContent(file);
                if (!content.isEmpty()) {
                    yaml.load(content);
                }
            } catch (YAMLException e) {
                report.addIssue(ValidatorReport.Severity.ERROR,
                        "Invalid YAML syntax: " + e.getMessage(),
                        file.getPath());
            } catch (Exception e) {
                report.addIssue(ValidatorReport.Severity.WARNING,
                        "Failed to validate YAML: " + e.getClass().getSimpleName(),
                        file.getPath());
            }
        }
    }

    private void validateTextureReferences() {
        Set<VirtualFile> modelFiles = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/models/.*.json"))
                .collect(Collectors.toSet());

        Set<String> texturePaths = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/textures/.*.png"))
                .map(VirtualFile::getPath)
                .collect(Collectors.toSet());

        for (VirtualFile modelFile : modelFiles) {
            try {
                String content = readFileContent(modelFile);
                if (!content.isEmpty()) {
                    JsonObject model = JsonParser.parseString(content).getAsJsonObject();

                    if (model.has("textures")) {
                        JsonObject textures = model.getAsJsonObject("textures");
                        for (Map.Entry<String, JsonElement> entry : textures.entrySet()) {
                            String texturePath = entry.getValue().getAsString();
                            validateTextureReference(texturePath, texturePaths, modelFile.getPath());
                        }
                    }

                    if (model.has("elements")) {
                        for (JsonElement element : model.getAsJsonArray("elements")) {
                            validateElementTextures(element.getAsJsonObject(), texturePaths, modelFile.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                // Already reported in validateJsonSyntax
            }
        }
    }

    private void validateTextureReference(String textureRef, Set<String> availableTextures, String modelPath) {
        if (textureRef.startsWith("#") || textureRef.startsWith("item/") || 
            textureRef.startsWith("block/") || textureRef.startsWith("entity/")) {
            return;
        }

        String fullPath = modelPathToTexturePath(textureRef);
        if (!availableTextures.contains(fullPath)) {
            report.addIssue(ValidatorReport.Severity.WARNING,
                    "Missing texture reference: " + textureRef,
                    modelPath);
        }
    }

    private void validateElementTextures(JsonObject element, Set<String> texturePaths, String modelPath) {
        if (element.has("faces")) {
            JsonObject faces = element.getAsJsonObject("faces");
            for (String faceName : faces.keySet()) {
                JsonObject face = faces.getAsJsonObject(faceName);
                if (face.has("texture")) {
                    validateTextureReference(face.get("texture").getAsString(), texturePaths, modelPath);
                }
            }
        }
    }

    private void validateTextureResolutions() {
        Set<VirtualFile> textureFiles = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/textures/.*.png"))
                .filter(f -> !f.getPath().matches(".*_layer_.*\\.png"))
                .collect(Collectors.toSet());

        for (VirtualFile textureFile : textureFiles) {
            try {
                BufferedImage image = readImageFile(textureFile);
                if (image != null) {
                    validateTextureResolution(image, textureFile.getPath());
                }
            } catch (Exception e) {
                report.addIssue(ValidatorReport.Severity.WARNING,
                        "Could not read texture file: " + e.getMessage(),
                        textureFile.getPath());
            }
        }
    }

    private void validateTextureResolution(BufferedImage image, String texturePath) {
        int width = image.getWidth();
        int height = image.getHeight();

        if (width > 512 || height > 512) {
            report.addIssue(ValidatorReport.Severity.ERROR,
                    "Texture resolution exceeds 512x512: " + width + "x" + height,
                    texturePath);
        }

        if (!isPowerOfTwo(width) || !isPowerOfTwo(height)) {
            report.addIssue(ValidatorReport.Severity.WARNING,
                    "Texture dimensions are not power of 2: " + width + "x" + height + " (may cause issues in some cases)",
                    texturePath);
        }
    }

    private void validatePathNaming() {
        Set<VirtualFile> modelFiles = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/models/.*.json"))
                .collect(Collectors.toSet());

        Set<VirtualFile> textureFiles = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/textures/.*.png"))
                .collect(Collectors.toSet());

        for (VirtualFile file : modelFiles) {
            if (!file.getPath().matches(".*[a-z0-9/._-]+.*")) {
                report.addIssue(ValidatorReport.Severity.ERROR,
                        "Model path contains invalid characters (must be [a-z0-9/._-])",
                        file.getPath());
            }
        }

        for (VirtualFile file : textureFiles) {
            if (!file.getPath().matches(".*[a-z0-9/._-]+.*")) {
                report.addIssue(ValidatorReport.Severity.ERROR,
                        "Texture path contains invalid characters (must be [a-z0-9/._-])",
                        file.getPath());
            }
        }
    }

    private void validateDuplicateNamespaces() {
        Map<String, List<String>> namespaceItems = new HashMap<>();

        for (VirtualFile file : files) {
            String path = file.getPath();
            if (path.startsWith("assets/") && path.contains("/")) {
                String namespace = path.split("/")[1];
                String itemPath = path;

                namespaceItems.computeIfAbsent(namespace, k -> new ArrayList<>()).add(itemPath);
            }
        }

        for (String namespace : namespaceItems.keySet()) {
            if (!namespace.matches(NAMESPACE_PATTERN.pattern())) {
                report.addIssue(ValidatorReport.Severity.WARNING,
                        "Namespace contains invalid characters: " + namespace);
            }
        }
    }

    private void validateMiniMessageFormatting() {
        for (VirtualFile file : files) {
            if (file.getPath().endsWith("lang.json")) {
                try {
                    String content = readFileContent(file);
                    if (!content.isEmpty()) {
                        JsonObject langFile = JsonParser.parseString(content).getAsJsonObject();
                        for (Map.Entry<String, JsonElement> entry : langFile.entrySet()) {
                            validateMiniMessage(entry.getValue().getAsString(), file.getPath());
                        }
                    }
                } catch (Exception e) {
                    // Already reported in validateJsonSyntax
                }
            }
        }
    }

    private void validateMiniMessage(String text, String filePath) {
        int openBrackets = countChar(text, '<');
        int closeBrackets = countChar(text, '>');

        if (openBrackets != closeBrackets) {
            report.addIssue(ValidatorReport.Severity.WARNING,
                    "Mismatched MiniMessage tags (< and >): " + openBrackets + " open, " + closeBrackets + " closed",
                    filePath);
        }

        int openBraces = countChar(text, '{');
        int closeBraces = countChar(text, '}');

        if (openBraces != closeBraces) {
            report.addIssue(ValidatorReport.Severity.WARNING,
                    "Mismatched braces in text: " + openBraces + " open, " + closeBraces + " closed",
                    filePath);
        }
    }

    private void validateModelHierarchy() {
        Set<String> models = files.stream()
                .filter(f -> f.getPath().matches("assets/.*/models/.*.json"))
                .map(VirtualFile::getPath)
                .collect(Collectors.toSet());

        for (VirtualFile file : files) {
            if (file.getPath().matches("assets/.*/models/.*.json")) {
                try {
                    String content = readFileContent(file);
                    if (!content.isEmpty()) {
                        JsonObject model = JsonParser.parseString(content).getAsJsonObject();
                        if (model.has("parent")) {
                            String parentPath = model.get("parent").getAsString();
                            if (!parentPath.startsWith("builtin/")) {
                                String fullParentPath = modelPathToModelFullPath(parentPath);
                                if (!models.contains(fullParentPath)) {
                                    report.addIssue(ValidatorReport.Severity.WARNING,
                                            "Parent model not found: " + parentPath,
                                            file.getPath());
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Already reported
                }
            }
        }
    }

    private String modelPathToTexturePath(String modelPath) {
        String namespace = modelPath.contains(":") ? modelPath.split(":")[0] : "minecraft";
        String texturePath = modelPath.contains(":") ? modelPath.split(":")[1] : modelPath;
        if (!texturePath.endsWith(".png")) texturePath += ".png";
        return "assets/" + namespace + "/textures/" + texturePath;
    }

    private String modelPathToModelFullPath(String modelPath) {
        String namespace = modelPath.contains(":") ? modelPath.split(":")[0] : "minecraft";
        String modelName = modelPath.contains(":") ? modelPath.split(":")[1] : modelPath;
        if (!modelName.endsWith(".json")) modelName += ".json";
        return "assets/" + namespace + "/models/" + modelName;
    }

    private String readFileContent(VirtualFile file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream = file.getInputStream();
        try {
            inputStream.transferTo(baos);
            return baos.toString(StandardCharsets.UTF_8);
        } finally {
            try {
                baos.close();
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private BufferedImage readImageFile(VirtualFile file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InputStream inputStream = file.getInputStream();
        try {
            inputStream.transferTo(baos);
            return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
        } finally {
            try {
                baos.close();
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }

    private boolean isPowerOfTwo(int n) {
        return n > 0 && (n & (n - 1)) == 0;
    }

    private int countChar(String str, char c) {
        int count = 0;
        for (char ch : str.toCharArray()) {
            if (ch == c) count++;
        }
        return count;
    }
}

class ByteArrayInputStream extends java.io.ByteArrayInputStream {
    public ByteArrayInputStream(byte[] buf) {
        super(buf);
    }
}
