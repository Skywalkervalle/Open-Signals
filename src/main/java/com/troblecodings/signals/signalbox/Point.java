package com.troblecodings.signals.signalbox;

import com.troblecodings.core.NBTWrapper;
import com.troblecodings.signals.core.ReadBuffer;
import com.troblecodings.signals.core.WriteBuffer;
import com.troblecodings.signals.signalbox.entrys.INetworkSavable;

public class Point implements INetworkSavable {

    private int x, y;

    public Point() {
        x = 0;
        y = 0;
    }

    public Point(final int x, final int y) {
        this.x = x;
        this.y = y;
    }

    public Point(final Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    public int getX() {
        return x;
    }

    public void setX(final int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(final int y) {
        this.y = y;
    }

    public void translate(final int x, final int y) {
        this.x += x;
        this.y += y;
    }

    public Point delta(final Point other) {
        final Point point = new Point(this);
        point.translate(-other.getX(), -other.getY());
        return point;
    }

    public static Point of(final ReadBuffer buffer) {
        return new Point(buffer.getByteAsInt(), buffer.getByteAsInt());
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null || !(obj instanceof Point))
            return false;
        final Point other = (Point) obj;
        return this.x == other.x && this.y == other.y;
    }

    @Override
    public int hashCode() {
        final int sum = x + y;
        return sum * (sum + 1) / 2 + x;
    }

    @Override
    public String toString() {
        return "Point[x=" + this.x + ",y=" + this.y + "]";
    }

    @Override
    public void write(final NBTWrapper tag) {
        tag.putInteger("x", x);
        tag.putInteger("y", y);
    }

    @Override
    public void read(final NBTWrapper tag) {
        this.x = tag.getInteger("x");
        this.y = tag.getInteger("y");
    }

    @Override
    public void readNetwork(final ReadBuffer buffer) {
        this.x = buffer.getByteAsInt();
        this.y = buffer.getByteAsInt();

    }

    @Override
    public void writeNetwork(final WriteBuffer buffer) {
        buffer.putByte((byte) x);
        buffer.putByte((byte) y);
    }
}