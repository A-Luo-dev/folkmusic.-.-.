package com.example.yin.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaUtils {

    private static final String CODE_STRING = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 去掉容易混淆的字符
    private static final int WIDTH = 120;
    private static final int HEIGHT = 40;

    /**
     * 生成随机验证码
     */
    public static String generateCode(int length) {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CODE_STRING.charAt(random.nextInt(CODE_STRING.length())));
        }
        return code.toString();
    }

    /**
     * 生成验证码图片并转为 Base64
     */
    public static String createBase64Image(String code) throws IOException {
        // 创建图片
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // 背景填充
        g.setColor(getRandomColor(200, 250));
        g.fillRect(0, 0, WIDTH, HEIGHT);

        // 设置字体
        g.setFont(new Font("Arial", Font.BOLD, 28));

        // 干扰线
        Random random = new Random();
        g.setColor(getRandomColor(160, 200));
        for (int i = 0; i < 15; i++) {
            int x = random.nextInt(WIDTH);
            int y = random.nextInt(HEIGHT);
            int xl = random.nextInt(20);
            int yl = random.nextInt(20);
            g.drawLine(x, y, x + xl, y + yl);
        }

        // 画验证码文字
        for (int i = 0; i < code.length(); i++) {
            g.setColor(new Color(20 + random.nextInt(110),
                    20 + random.nextInt(110),
                    20 + random.nextInt(110)));
            g.drawString(String.valueOf(code.charAt(i)), 20 * i + 15, 30);
        }

        g.dispose();

        // 转为 Base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * 获取随机颜色
     */
    private static Color getRandomColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }
}
