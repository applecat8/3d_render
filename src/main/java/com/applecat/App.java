package com.applecat;

import java.awt.image.BufferedImage;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;

/**
 * Hello world!
 *
 * 在这篇文章中，我将假设X坐标意味着在左右方向的运动，Y意味着在屏幕上的上下运动，而Z将是深度（所以Z轴与你的屏幕垂直）。正的Z意味着 "朝向观察者"。
 *
 */
public class App {
    public static Vertex getSide(Vertex v1, Vertex v2) {
        return new Vertex(v1.x - v2.x, v1.y - v2.y, v1.z - v2.z);
    }

    public static Color getShade(Color c, double shade) {
        double redLinear = Math.pow(c.getRed(), 2.4) * shade;
        double greenLinear = Math.pow(c.getGreen(), 2.4) * shade;
        double blueLinear = Math.pow(c.getBlue(), 2.4) * shade;

        int red = (int) Math.pow(redLinear, 1 / 2.4);
        int green = (int) Math.pow(greenLinear, 1 / 2.4);
        int blue = (int) Math.pow(blueLinear, 1 / 2.4);
        return new Color(red, green, blue);
    }

    public static List<Triangle> inflate(List<Triangle> tris) {
        List<Triangle> result = new ArrayList<>();
        // 将一个大三角形分为四个小三角形
        for (Triangle t : tris) {
            Vertex m1 = new Vertex((t.v1.x + t.v2.x) / 2, (t.v1.y + t.v2.y) / 2, (t.v1.z + t.v2.z) / 2);
            Vertex m2 = new Vertex((t.v2.x + t.v3.x) / 2, (t.v2.y + t.v3.y) / 2, (t.v2.z + t.v3.z) / 2);
            Vertex m3 = new Vertex((t.v1.x + t.v3.x) / 2, (t.v1.y + t.v3.y) / 2, (t.v1.z + t.v3.z) / 2);
            result.add(new Triangle(t.v1, m1, m3, t.color));
            result.add(new Triangle(t.v2, m1, m2, t.color));
            result.add(new Triangle(t.v3, m2, m3, t.color));
            result.add(new Triangle(m1, m2, m3, t.color));
        }
        for (Triangle t : result) {
            for (Vertex v : new Vertex[] { t.v1, t.v2, t.v3 }) {
                double l = Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z) / Math.sqrt(30000);
                v.x /= l;
                v.y /= l;
                v.z /= l;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        JFrame frame = new JFrame();
        Container pane = frame.getContentPane();
        pane.setLayout(new BorderLayout());

        // 控制水平旋转的滑块
        JSlider headingSlider = new JSlider(0, 360, 180);
        pane.add(headingSlider, BorderLayout.SOUTH);

        // 控制垂直旋转的滑块
        JSlider pitchSlider = new JSlider(SwingConstants.VERTICAL, -90, 90, 0);
        pane.add(pitchSlider, BorderLayout.EAST);

        // 显示渲染结果的面板

        JPanel renderpPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 一个四面体
                List<Triangle> tris = new ArrayList<>();
                tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100),
                        new Vertex(-100, 100, -100), Color.WHITE));
                tris.add(new Triangle(new Vertex(100, 100, 100), new Vertex(-100, -100, 100),
                        new Vertex(100, -100, -100), Color.RED));
                tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100),
                        new Vertex(100, 100, 100), Color.GREEN));
                tris.add(new Triangle(new Vertex(-100, 100, -100), new Vertex(100, -100, -100),
                        new Vertex(-100, -100, 100), Color.BLUE));

                // List<Triangle> sphere = inflate(inflate(inflate(inflate(tris))));

                // XZ 转换矩阵
                double heading = Math.toRadians(headingSlider.getValue());
                Matrix3 headingTransform = new Matrix3(new double[] { Math.cos(heading), 0, -Math.sin(heading), 0, 1, 0,
                        Math.sin(heading), 0, Math.cos(heading) }, 3, 3);

                // YZ 转换矩阵
                double pitch = Math.toRadians(pitchSlider.getValue());
                Matrix3 pitchTransform = new Matrix3(new double[] { 1, 0, 0, 0, Math.cos(pitch), Math.sin(pitch), 0,
                        -Math.sin(pitch), Math.cos(pitch) }, 3, 3);

                // 同时实现XZ 和 YZ 转换
                Matrix3 transform = headingTransform.multiply(pitchTransform);

                // g2.translate(getWidth() / 2, getHeight() / 2);
                // g2.setColor(Color.WHITE);

                BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
                // 存放三角形中最高的颜色的高度
                // 用于展示颜色，最高的颜色展示出来
                double[] zBuffer = new double[img.getWidth() * img.getHeight()];
                for (int q = 0; q < zBuffer.length; q++) {
                    zBuffer[q] = Double.NEGATIVE_INFINITY; // 初始化为最小
                }
                tris.forEach(t -> {
                    Vertex v1 = transform.transform(t.v1);
                    Vertex v2 = transform.transform(t.v2);
                    Vertex v3 = transform.transform(t.v3);

                    v1.x += getWidth() / 2;
                    v1.y += getHeight() / 2;
                    v2.x += getWidth() / 2;
                    v2.y += getHeight() / 2;
                    v3.x += getWidth() / 2;
                    v3.y += getHeight() / 2;

                    // 两条边
                    Vertex s1 = getSide(v1, v2);
                    Vertex s2 = getSide(v1, v3);

                    // 法向量
                    Vertex norm = new Vertex(s1.y * s2.z - s1.z * s2.y, s1.z * s2.x - s1.x * s2.z,
                            s1.x * s2.y - s2.y * s2.x);

                    // norm的模
                    double normalLength = Math.sqrt(norm.x * norm.x + norm.y * norm.y + norm.z * norm.z);
                    // 标准化为单位法向量
                    norm.x /= normalLength;
                    norm.y /= normalLength;
                    norm.z /= normalLength;

                    // 光源的向量规定为[0, 0, 1], 则余弦角为
                    double angleCos = Math.abs(norm.z);

                    // 计算三角形的矩形边界
                    int minX = (int) Math.max(0, Math.ceil(Math.min(v1.x, Math.min(v2.x, v3.x))));
                    int maxX = (int) Math.min(img.getWidth() - 1, Math.floor(Math.max(v1.x, Math.max(v2.x, v3.x))));

                    int minY = (int) Math.max(0, Math.ceil(Math.min(v1.y, Math.min(v2.y, v3.y))));
                    int maxY = (int) Math.min(img.getHeight() - 1, Math.floor(Math.max(v1.y, Math.max(v2.y, v3.y))));

                    double triangleArea = (v1.y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - v1.x);

                    for (int y = minY; y <= maxY; y++) {
                        for (int x = minX; x <= maxX; x++) {
                            double b1 = ((y - v3.y) * (v2.x - v3.x) + (v2.y - v3.y) * (v3.x - x)) / triangleArea;
                            double b2 = ((y - v1.y) * (v3.x - v1.x) + (v3.y - v1.y) * (v1.x - x)) / triangleArea;
                            double b3 = ((y - v2.y) * (v1.x - v2.x) + (v1.y - v2.y) * (v2.x - x)) / triangleArea;
                            double depth = b1 * v1.z + b2 * v2.z + b3 * v3.z;
                            int zIndex = y * img.getWidth() + x;

                            if (b1 >= 0 && b1 <= 1 && b2 >= 0 && b2 <= 1 && b3 >= 0 && b3 <= 1
                                    && zBuffer[zIndex] < depth) { // 判断是否在范围内，且覆盖其他颜色
                                img.setRGB(x, y, getShade(t.color, angleCos).getRGB());
                                zBuffer[zIndex] = depth;
                            }
                        }
                    }
                    g2.drawImage(img, 0, 0, null);
                });
            }
        };
        pane.add(renderpPanel, BorderLayout.CENTER);

        // 添加时间监听
        headingSlider.addChangeListener(e -> renderpPanel.repaint());
        pitchSlider.addChangeListener(e -> renderpPanel.repaint());

        frame.setSize(400, 400);
        frame.setVisible(true);
    }
}
