package com.example.documentanonymization.config;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfBrushes;
import com.spire.pdf.graphics.PdfFont;
import com.spire.pdf.graphics.PdfFontFamily;
import com.spire.pdf.texts.*;

import java.util.EnumSet;
import java.util.List;

public class replaceblanck {

    public static void main(String[] args) {

        // Create a PdfDocument object
        PdfDocument doc = new PdfDocument();

        // Load a PDF file
        doc.loadFromFile("C:\\Users\\Administrator\\Desktop\\Input.pdf");

        // Create a PdfTextReplaceOptions object
        PdfTextReplaceOptions textReplaceOptions = new PdfTextReplaceOptions();

        // Specify the options for text replacement
        textReplaceOptions.setReplaceType(EnumSet.of(ReplaceActionType.IgnoreCase));
        textReplaceOptions.setReplaceType(EnumSet.of(ReplaceActionType.WholeWord));

        // Get a specific page
        PdfPageBase page = doc.getPages().get(0);

        // Create a PdfTextReplacer object based on the page
        PdfTextReplacer textReplacer = new PdfTextReplacer(page);

        // Set the replace options
        textReplacer.setOptions(textReplaceOptions);

        // Replace all instances of target text with new text
        textReplacer.replaceAllText("MySQL", "mysql");

        // Save the document to a different PDF file
        doc.saveToFile("output/ReplaceTextInPage.pdf");

        // Dispose resources
        doc.dispose();
    }

    public void deneme(byte[] pdfBytes) {
        // PDF'i yükle
        PdfDocument doc = new PdfDocument();
        doc.loadFromBytes(pdfBytes);

        // Create a PdfTextReplaceOptions object
        PdfTextReplaceOptions textReplaceOptions = new PdfTextReplaceOptions();

        // Specify the options for text replacement
        textReplaceOptions.setReplaceType(EnumSet.of(ReplaceActionType.IgnoreCase));
        textReplaceOptions.setReplaceType(EnumSet.of(ReplaceActionType.WholeWord));

        // Get a specific page
        PdfPageBase page = doc.getPages().get(0);

        // Create a PdfTextReplacer object based on the page
        PdfTextReplacer textReplacer = new PdfTextReplacer(page);

        // Set the replace options
        textReplacer.setOptions(textReplaceOptions);

        // Replace all instances of target text with new text
        textReplacer.replaceAllText("work", "HELLO");


        // Kaydet ve kapat
        doc.saveToFile("C:\\Users\\berkc\\Downloads\\ReplaceFirstInstance.pdf");
        System.out.println("PDF başarıyla güncellendi: C:\\Users\\berkc\\Downloads\\ReplaceFirstInstance.pdf");
        doc.dispose();
    }
}
