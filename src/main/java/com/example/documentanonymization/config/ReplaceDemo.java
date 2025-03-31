package com.example.documentanonymization.config;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfBrushes;
import com.spire.pdf.graphics.PdfFont;
import com.spire.pdf.graphics.PdfFontFamily;
import com.spire.pdf.texts.*;

import java.awt.geom.Rectangle2D;
import java.util.EnumSet;
import java.util.List;

public class ReplaceDemo {

    public static void main(String[] args) {

        // Yeni bir PdfDocument nesnesi oluşturuyoruz
        PdfDocument pdf = new PdfDocument();

        // Belirtilen dosya yolundan PDF'i yüklüyoruz
        pdf.loadFromFile("C:\\Users\\berkc\\Downloads\\prolab1.pdf");

        // PDF'in ilk sayfasını alıyoruz (indeks 0)
        PdfPageBase page = pdf.getPages().get(0);

        // Sayfada metin bulmak için bir PdfTextFinder nesnesi oluşturuyoruz
        PdfTextFinder finder = new PdfTextFinder(page);

        // Metin arama seçeneklerini yapılandırıyoruz
        PdfTextFindOptions options = new PdfTextFindOptions();

        // Sadece tam kelimeleri eşleştirmek için WholeWord parametresini ayarlıyoruz
        options.setTextFindParameter(EnumSet.of(TextFindParameter.WholeWord));

        // "proje" kelimesinin tüm örneklerini buluyoruz ve bulunan metin parçalarını bir liste olarak alıyoruz
        List<PdfTextFragment> fragments = finder.find("proje", options);

        // Yeni metin "hello" olarak belirleniyor
        String newText = "hello";

        // Helvetica font ailesinden 10 punto büyüklüğünde bir font oluşturuyoruz
        PdfFont font = new PdfFont(PdfFontFamily.Helvetica, 10);

        // Bulunan her metin parçası için döngü oluşturuyoruz
        for(PdfTextFragment fragment : fragments) {
            // Metin parçasının koordinatlarını ve boyutlarını içeren dikdörtgeni alıyoruz
            Rectangle2D rec = fragment.getBounds()[0];

            // Orijinal metni beyaz bir dikdörtgenle örtüyoruz (üzerini kapatıyoruz)
            page.getCanvas().drawRectangle(PdfBrushes.getWhite(), rec);

            // Yeni metni siyah renkte aynı konuma (küçük bir dikey ayarlamayla) yazıyoruz
            page.getCanvas().drawString(newText, font, PdfBrushes.getBlack(), rec.getX(), rec.getY()-1);
        }

        // Değiştirilmiş PDF'i yeni bir dosya olarak kaydediyoruz
        pdf.saveToFile("C:\\Users\\berkc\\Downloads\\yeni.pdf");

        // İşlemin tamamlandığını gösteren bir mesaj yazdırıyoruz
        System.out.println("PDF başarıyla güncellendi: C:\\Users\\berkc\\Downloads\\yeni.pdf");

        // PDF belgesini kapatıp, kaynakları temizliyoruz
        pdf.close();
    }
}