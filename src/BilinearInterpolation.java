import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

class Pixel {
    public int[] ARGB = new int[4];

    Pixel(int ARGB) {
        for (int i = 0; i < this.ARGB.length; i++) {
            this.ARGB[i] = (ARGB >> (8 * i)) & 0xFF;
        }
    }

    public int getRGB() {
        int RGB = 0;
        for (int i = 0; i < this.ARGB.length; i++) {
            RGB |= this.ARGB[i] << (8 * i);
        }
        return RGB;
    }
}

public class BilinearInterpolation {
    private static double lerp(double a, double b, double t) {
        return (1 - t) * a + t * b;
    }

    private static BufferedImage scale(BufferedImage src, double scale) {
        int dstWidth = (int)(src.getWidth() * scale);
        int dstHeight = (int)(src.getHeight() * scale);

        BufferedImage dst = new BufferedImage(dstWidth, dstHeight, src.getType());

        for (int y = 0; y < dstHeight; y++) {
            for (int x = 0; x < dstWidth; x++) {
                // zero indexed real position in the src image
                double ySrcReal = (double)y / dstHeight * (src.getHeight() - 1);
                double xSrcReal = (double)x / dstWidth * (src.getWidth() - 1);

                // valid position in src
                int ySrcInt = (int)ySrcReal;
                int xSrcInt = (int)xSrcReal;

                // the t values for lerp
                // the weighting between pixels N and N + 1
                double xt = xSrcReal - xSrcInt;
                double yt = ySrcReal - ySrcInt;

                Pixel q12 = new Pixel(src.getRGB(xSrcInt, ySrcInt));
                Pixel q22 = new Pixel(src.getRGB(xSrcInt + 1, ySrcInt));
                Pixel q11 = new Pixel(src.getRGB(xSrcInt, ySrcInt + 1));
                Pixel q21 = new Pixel(src.getRGB(xSrcInt + 1, ySrcInt + 1));

                // pixel value will be built up from 0
                Pixel p = new Pixel(0);

                // ARGB
                for (int i = 0; i < 4; i++) {
                    int i12 = q12.ARGB[i];
                    int i22 = q22.ARGB[i];
                    int i11 = q11.ARGB[i];
                    int i21 = q21.ARGB[i];

                    double r1 = lerp(i11, i21, xt);
                    double r2 = lerp(i12, i22, xt);

                    p.ARGB[i] = (int)lerp(r2, r1, yt);
                }

                dst.setRGB(x, y, p.getRGB());
            }
        }

        return dst;
    }
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: java BilinearInterpolation <input> <relative scale> <output>");
            System.err.println("Example: java BilinearInterpolation i.jpg 1.5 o.jpg ");
            System.exit(1);
        }

        String srcPath = args[0];
        String scaleArg = args[1];
        String dstPath = args[2];

        double scale = 1.0;
        try {
            scale = Double.parseDouble(scaleArg);
        } catch (Exception e) {
            System.err.println("Error: Invalid relative scale value: " + scaleArg);
            System.exit(1);
        }

        BufferedImage src = null;
        try {
            src = ImageIO.read(new File(srcPath));
        } catch (Exception e) {
            System.err.println("Error: Unable to open input file: " + srcPath);
            System.exit(1);
        }

        BufferedImage dst = scale(src, scale);

        try {
            ImageIO.write(dst, "jpg", new File(dstPath));
        } catch (Exception e) {
            System.err.println("Error: Unable to open output file: " + dstPath);
        }
    }
}
