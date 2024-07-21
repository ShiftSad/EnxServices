package codes.shiftmc.commom.math;

import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.math.FinePosition;
import io.papermc.paper.math.Position;
import org.jetbrains.annotations.NotNull;

public class Vec3D implements Position {
    public static final Vec3D ZERO = new Vec3D(0.0D, 0.0D, 0.0D);
    public final double x;
    public final double y;
    public final double z;

    public Vec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public int blockX() {
        return 0;
    }

    @Override
    public int blockY() {
        return 0;
    }

    @Override
    public int blockZ() {
        return 0;
    }

    @Override
    public double x() {
        return 0;
    }

    @Override
    public double y() {
        return 0;
    }

    @Override
    public double z() {
        return 0;
    }

    @Override
    public boolean isBlock() {
        return false;
    }

    @Override
    public boolean isFine() {
        return false;
    }

    @Override
    public @NotNull Position offset(int x, int y, int z) {
        return null;
    }

    @Override
    public @NotNull FinePosition offset(double x, double y, double z) {
        return null;
    }

    @Override
    public @NotNull BlockPosition toBlock() {
        return null;
    }
}
