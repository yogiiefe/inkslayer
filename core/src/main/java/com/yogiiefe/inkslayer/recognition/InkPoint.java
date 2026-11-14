package com.yogiiefe.inkslayer.recognition;

public record InkPoint(double x, double y) {
    public double distance(InkPoint other) { return Math.hypot(x - other.x, y - other.y); }
}
