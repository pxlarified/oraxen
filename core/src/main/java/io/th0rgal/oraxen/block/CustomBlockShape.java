package io.th0rgal.oraxen.block;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CustomBlockShape {

    private final List<BoundingBox> boxes;

    public CustomBlockShape() {
        this.boxes = new ArrayList<>();
    }

    public void addBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        boxes.add(new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
    }

    public void addBox(@NotNull BoundingBox box) {
        boxes.add(box);
    }

    @NotNull
    public List<BoundingBox> getBoxes() {
        return boxes;
    }

    public boolean isEmpty() {
        return boxes.isEmpty();
    }

    public static class BoundingBox {
        public final double minX, minY, minZ, maxX, maxY, maxZ;

        public BoundingBox(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
            this.minX = Math.max(0, Math.min(1, minX));
            this.minY = Math.max(0, Math.min(1, minY));
            this.minZ = Math.max(0, Math.min(1, minZ));
            this.maxX = Math.max(0, Math.min(1, maxX));
            this.maxY = Math.max(0, Math.min(1, maxY));
            this.maxZ = Math.max(0, Math.min(1, maxZ));
        }

        public boolean intersects(BoundingBox other) {
            return minX < other.maxX && maxX > other.minX &&
                   minY < other.maxY && maxY > other.minY &&
                   minZ < other.maxZ && maxZ > other.minZ;
        }

        @Override
        public String toString() {
            return String.format("BoundingBox[%.2f,%.2f,%.2f -> %.2f,%.2f,%.2f]", 
                    minX, minY, minZ, maxX, maxY, maxZ);
        }
    }
}
