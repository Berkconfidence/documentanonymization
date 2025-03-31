package com.example.documentanonymization.config;

import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfImage;
import com.spire.pdf.utilities.PdfImageHelper;
import com.spire.pdf.utilities.PdfImageInfo;
import org.opencv.core.*;
import org.opencv.highgui.HighGui;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class NEREexample {

    public static void main(String[] args) throws IOException {
        extractBlurAndReplaceImages("C:\\Users\\berkc\\Downloads\\örnek_makale4.pdf");
    }

    public static void anonymizeTextWithPlaceholders() {
        //Create a PdfDocument instance
        PdfDocument doc = new PdfDocument();
        //Load a PDF document
        doc.loadFromFile("C:\\Users\\berkc\\Downloads\\örnek_makale1.pdf");

        //Get the first page
        PdfPageBase page = doc.getPages().get(0);

        //Load an image
        PdfImage image = PdfImage.fromFile("C:\\Users\\berkc\\Downloads\\foto.jpg");

        // Get the image information from the page
        PdfImageHelper imageHelper = new PdfImageHelper();
        PdfImageInfo[] imageInfos = imageHelper.getImagesInfo(page);

        // Replace Image
        imageHelper.replaceImage(imageInfos[0], image);

        //Save the result document
        doc.saveToFile("C:\\Users\\berkc\\Downloads\\örnek_makale1.pdf", FileFormat.PDF);
        System.out.println("PDF updated successfully: C:\\Users\\berkc\\Downloads\\cv1.pdf");
        //Dispose the document
        doc.dispose();
    }

    public static void blurimage() {
        try {
            // OpenCV kütüphanesini yükle - bu satır kritik
            nu.pattern.OpenCV.loadLocally();

            // Path'i kontrol et
            System.out.println("Java Library Path: " + System.getProperty("java.library.path"));

            String imagePath = "C:\\Users\\berkc\\Downloads\\foto.jpg";

            // OpenCV operasyonlarına geçmeden önce kütüphanenin yüklendiğinden emin ol
            System.out.println("OpenCV Sürümü: " + Core.VERSION);

            // Görüntüyü oku
            Mat source = Imgcodecs.imread(imagePath);
            if (source.empty()) {
                System.out.println("Görüntü yüklenemedi: " + imagePath);
                return;
            }

            // Bulanıklaştırma işlemleri
            Mat destination = new Mat(source.rows(), source.cols(), source.type());
            int kernelSize = 45;
            Imgproc.GaussianBlur(source, destination, new Size(kernelSize, kernelSize), 0);

            for (int i = 0; i < 3; i++) {
                Imgproc.GaussianBlur(destination, destination, new Size(kernelSize, kernelSize), 8);
            }

            // Bulanıklaştırılmış görüntüyü kaydet
            String outputPath = imagePath.replace(".jpg", "_blurred.jpg");
            boolean success = Imgcodecs.imwrite(outputPath, destination);

            if (success) {
                System.out.println("Bulanıklaştırılmış görüntü kaydedildi: " + outputPath);
            } else {
                System.out.println("Görüntü kaydedilemedi!");
            }

            source.release();
            destination.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getImages() throws IOException {
        // Create a PdfDocument object
        PdfDocument doc = new PdfDocument();

        // Load a PDF document
        doc.loadFromFile("C:\\Users\\berkc\\Downloads\\örnek_makale1.pdf");

        // Son sayfayı al
        int lastPageIndex = doc.getPages().getCount() - 1;
        System.out.println("Son sayfa bulundu: " + (lastPageIndex + 1));

        // Get a specific page
        PdfPageBase page = doc.getPages().get(lastPageIndex);

        // Create a PdfImageHelper object
        PdfImageHelper imageHelper = new PdfImageHelper();

        // Get all image information from the page
        PdfImageInfo[] imageInfos = imageHelper.getImagesInfo(page);

        // Iterate through the image information
        for (int i = 0; i < imageInfos.length; i++)
        {
            // Get a specific piece of image information
            PdfImageInfo imageInfo = imageInfos[i];

            // Get the image
            BufferedImage image = imageInfo.getImage();
            File file = new File(String.format("C:\\Users\\berkc\\Downloads\\\\Image-%d.png",i));

            // Save the image file in PNG format
            ImageIO.write(image, "PNG", file);
        }

        // Dispose resources
        doc.dispose();
    }

    public static void replaceImage() {
        //Create a PdfDocument instance
        PdfDocument doc = new PdfDocument();

        //Orijinal dosya yolu ve kayıt dosya yolu
        String originalFilePath = "C:\\Users\\berkc\\Downloads\\örnek_makale4.pdf";
        String outputFilePath = "C:\\Users\\berkc\\Downloads\\replaceimage.pdf";

        //Load a PDF document
        doc.loadFromFile(originalFilePath);

        //Orijinal sayfa sayısını kontrol et
        int originalPageCount = doc.getPages().getCount();
        System.out.println("Orijinal PDF'deki sayfa sayısı: " + originalPageCount);

        // Son sayfayı al
        int lastPageIndex = originalPageCount - 1;
        PdfPageBase page = doc.getPages().get(lastPageIndex);

        //Load an image
        PdfImage image = PdfImage.fromFile("C:\\Users\\berkc\\Downloads\\Image-4.png");

        // Get the image information from the page
        PdfImageHelper imageHelper = new PdfImageHelper();
        PdfImageInfo[] imageInfos = imageHelper.getImagesInfo(page);

        System.out.println("Son sayfada " + imageInfos.length + " resim bulundu");

        // Her resmi tek tek değiştir
        for (int i = 0; i < imageInfos.length; i++) {
            try {
                imageHelper.replaceImage(imageInfos[i], image);
                System.out.println((i+1) + ". resim değiştirildi");
            } catch (Exception e) {
                System.err.println((i+1) + ". resim değiştirilirken hata oluştu: " + e.getMessage());
            }
        }

        // PDF değişikliklerini kaydet
        try {
            // PDF'i yüksek koruma modu ile kaydet
            doc.saveToFile(outputFilePath, FileFormat.PDF);

            // Yeni PDF'in sayfa sayısını kontrol et
            PdfDocument checkDoc = new PdfDocument();
            checkDoc.loadFromFile(outputFilePath);
            System.out.println("Kaydedilen PDF'deki sayfa sayısı: " + checkDoc.getPages().getCount());
            checkDoc.dispose();
        } catch (Exception e) {
            System.err.println("PDF kaydedilirken hata: " + e.getMessage());
            e.printStackTrace();
        }

        //Kaynakları serbest bırak
        doc.dispose();
    }

    public static void extractBlurAndReplaceImages(String pdfPath) {
        try {
            // OpenCV kütüphanesini yükle
            nu.pattern.OpenCV.loadLocally();

            System.out.println("OpenCV Sürümü: " + Core.VERSION);

            // PDF dosyasını yükle
            PdfDocument doc = new PdfDocument();
            doc.loadFromFile(pdfPath);

            // Son sayfayı al
            int lastPageIndex = doc.getPages().getCount() - 1;
            System.out.println("Son sayfa bulundu: " + (lastPageIndex + 1));
            PdfPageBase page = doc.getPages().get(lastPageIndex);

            // Resimleri al
            PdfImageHelper imageHelper = new PdfImageHelper();
            PdfImageInfo[] imageInfos = imageHelper.getImagesInfo(page);
            System.out.println("Son sayfada " + imageInfos.length + " resim bulundu");

            // Her resim için işlem yap
            for (int i = 0; i < imageInfos.length; i++) {
                try {
                    // Resmi al
                    BufferedImage originalImage = imageInfos[i].getImage();

                    // Geçici bir dosyaya kaydet
                    String tempImagePath = "C:\\Users\\berkc\\Downloads\\temp_image_" + i + ".png";
                    ImageIO.write(originalImage, "PNG", new File(tempImagePath));

                    // OpenCV ile bulanıklaştır
                    Mat source = Imgcodecs.imread(tempImagePath);
                    if (source.empty()) {
                        System.out.println("Görüntü yüklenemedi: " + tempImagePath);
                        continue;
                    }

                    // Bulanıklaştırma işlemleri
                    Mat destination = new Mat(source.rows(), source.cols(), source.type());
                    int kernelSize = 45;
                    Imgproc.GaussianBlur(source, destination, new Size(kernelSize, kernelSize), 0);

                    for (int j = 0; j < 3; j++) {
                        Imgproc.GaussianBlur(destination, destination, new Size(kernelSize, kernelSize), 8);
                    }

                    // Bulanıklaştırılmış görüntüyü kaydet
                    String blurredImagePath = "C:\\Users\\berkc\\Downloads\\temp_blurred_" + i + ".png";
                    boolean success = Imgcodecs.imwrite(blurredImagePath, destination);

                    if (success) {
                        System.out.println("Bulanıklaştırılmış görüntü kaydedildi: " + blurredImagePath);

                        // Bulanıklaştırılmış görüntüyü PDF'e yerleştir
                        PdfImage blurredImage = PdfImage.fromFile(blurredImagePath);
                        imageHelper.replaceImage(imageInfos[i], blurredImage);
                        System.out.println((i+1) + ". resim bulanıklaştırılıp değiştirildi");
                    } else {
                        System.out.println("Görüntü kaydedilemedi!");
                    }

                    // Kaynakları serbest bırak
                    source.release();
                    destination.release();

                    // Geçici dosyaları temizle
                    new File(tempImagePath).delete();
                    new File(blurredImagePath).delete();

                } catch (Exception e) {
                    System.err.println((i+1) + ". resim işlenirken hata oluştu: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Değişiklikleri kaydet
            String outputPath = pdfPath.replace(".pdf", "_blurred.pdf");
            doc.saveToFile(outputPath, FileFormat.PDF);
            System.out.println("Bulanıklaştırılmış resimli PDF kaydedildi: " + outputPath);

            // Kaynakları serbest bırak
            doc.dispose();

        } catch (Exception e) {
            System.err.println("İşlem sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
