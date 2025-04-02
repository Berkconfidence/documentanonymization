package com.example.documentanonymization.service;

import com.example.documentanonymization.dto.ArticleDto;
import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.repository.ArticleRepository;
import com.example.documentanonymization.repository.ReviewerRepository;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.*;
import com.spire.pdf.texts.PdfTextFindOptions;
import com.spire.pdf.texts.PdfTextFinder;
import com.spire.pdf.texts.PdfTextFragment;
import com.spire.pdf.texts.TextFindParameter;
import com.spire.pdf.utilities.PdfImageHelper;
import com.spire.pdf.utilities.PdfImageInfo;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private ReviewerRepository reviewerRepository;

    @Autowired
    private LogService logService;

    @Autowired
    private EncryptionService encryptionService;

    public ArticleService(ArticleRepository articleRepository, LogService logService) {
        this.articleRepository = articleRepository;
        this.logService = logService;
    }


    public ResponseEntity<List<ArticleDto>> getAllArticle() {
        List<Article> articles = articleRepository.findAll();

        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<ArticleDto> dtoList = articles.stream()
                .map(article -> {
                    ArticleDto dto = new ArticleDto();
                    dto.setFileName(article.getFileName());
                    dto.setAuthorEmail(article.getAuthorEmail());
                    dto.setStatus(article.getStatus());
                    dto.setTrackingNumber(article.getTrackingNumber());
                    dto.setSubmissionDate(article.getSubmissionDate());
                    dto.setReviewDate(article.getReviewDate());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    public ResponseEntity<ArticleDto> getArticleByTrackingNumber(String trackingNumber) {
        return articleRepository.findByTrackingNumber(trackingNumber)
                .map(article -> {
                    ArticleDto dto = new ArticleDto();
                    dto.setFileName(article.getFileName());
                    dto.setTrackingNumber(article.getTrackingNumber());
                    dto.setAuthorEmail(article.getAuthorEmail());
                    dto.setStatus(article.getStatus());
                    dto.setSubmissionDate(article.getSubmissionDate());
                    dto.setReviewDate(article.getReviewDate());
                    dto.setSpecializations(article.getSpecializations());

                    dto.setAssignedReviewer(article.getAssignedReviewer());
                    dto.setAssignedReviewerName(dto.getAssignedReviewerName());
                    return ResponseEntity.ok(dto);
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public ResponseEntity<List<ArticleDto>> getArticleByAuthorEmail(String email) {
        List<Article> articles = articleRepository.findByAuthorEmail(email);

        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<ArticleDto> dtoList = articles.stream()
                .map(article -> {
                    ArticleDto dto = new ArticleDto();
                    dto.setFileName(article.getFileName());
                    dto.setAuthorEmail(article.getAuthorEmail());
                    dto.setStatus(article.getStatus());
                    dto.setTrackingNumber(article.getTrackingNumber());
                    dto.setSubmissionDate(article.getSubmissionDate());
                    dto.setReviewDate(article.getReviewDate());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    public ResponseEntity<byte[]> downloadArticleFile(String trackingNumber) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty() || articleOpt.get().getFile() == null) {
            return ResponseEntity.notFound().build();
        }

        Article article = articleOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", article.getFileName() + ".pdf");

        // Şifreli dosyayı çöz
        byte[] decryptedFile = encryptionService.decryptFile(article.getFile());

        return new ResponseEntity<>(decryptedFile, headers, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> viewAnonimizeArticleFile(String trackingNumber) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty() || articleOpt.get().getAnonymizedFile() == null) {
            return ResponseEntity.notFound().build();
        }

        Article article = articleOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", article.getFileName() + ".pdf");

        // Şifreli anonimleştirilmiş dosyayı çöz
        byte[] decryptedFile = encryptionService.decryptFile(article.getAnonymizedFile());

        return new ResponseEntity<>(decryptedFile, headers, HttpStatus.OK);
    }

    public ResponseEntity<byte[]> viewReviewedArticleFile(String trackingNumber) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty() || articleOpt.get().getReviewedFile() == null) {
            return ResponseEntity.notFound().build();
        }

        Article article = articleOpt.get();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", article.getFileName() + ".pdf");

        // Şifreli incelenmiş dosyayı çöz
        byte[] decryptedFile = encryptionService.decryptFile(article.getReviewedFile());

        return new ResponseEntity<>(decryptedFile, headers, HttpStatus.OK);
    }

    public ResponseEntity<List<ArticleDto>> getArticlesByReviewerId(Long id) {
        Optional<Reviewer> reviewerOpt = reviewerRepository.findById(id);

        if (reviewerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Reviewer reviewer = reviewerOpt.get();
        List<Article> articles = articleRepository.findByAssignedReviewer(reviewer);

        if (articles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        List<ArticleDto> dtoList = articles.stream()
                .map(article -> {
                    ArticleDto dto = new ArticleDto();
                    dto.setFileName(article.getFileName());
                    dto.setAuthorEmail(article.getAuthorEmail());
                    dto.setStatus(article.getStatus());
                    dto.setTrackingNumber(article.getTrackingNumber());
                    dto.setSubmissionDate(article.getSubmissionDate());
                    dto.setReviewDate(article.getReviewDate());
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(dtoList);
    }

    public Article createArticle(MultipartFile file, String email) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Dosya boş olamaz");
        }

        if (!file.getContentType().equals("application/pdf")) {
            throw new IllegalArgumentException("Sadece PDF dosyaları kabul edilir");
        }

        // Extract original filename
        String originalFilename = file.getOriginalFilename();
        String fileName = originalFilename;
        if (originalFilename != null && originalFilename.contains(".")) {
            fileName = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
        }

        String trackingNumber = generateTrackingNumber();

        Article article = new Article();
        article.setFileName(fileName);

        // Dosyayı şifrele
        byte[] encryptedFile = encryptionService.encryptFile(file.getBytes());
        article.setFile(encryptedFile);

        article.setAuthorEmail(email);
        article.setStatus("Alındı");
        article.setTrackingNumber(trackingNumber);
        article.setSubmissionDate(new Date());

        try {
            String action = "Makale sisteme yüklendi";
            logService.createLog(trackingNumber, action, email, "Yazar");
        } catch (Exception e) {
            System.err.println("Log oluşturma sırasında hata: " + e.getMessage());
        }
        return articleRepository.save(article);
    }

    public Article assignReviewer(String trackingNumber, Reviewer reviewer) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("Makale bulunamadı");
        }

        Article article = articleOpt.get();
        article.setReviewDate(null);
        article.setAnonymizedFile(null);
        article.setReviewedFile(null);
        article.setReviewComment(null);
        article.setAssignedReviewer(reviewer);
        article.setStatus("Değerlendirmede");

        try {
            String action = "Hakem Makaleye Atandı";
            logService.createLog(trackingNumber, action, "admin@gmail.com", "Yazar");
        } catch (Exception e) {
            System.err.println("Log oluşturma sırasında hata: " + e.getMessage());
        }

        return articleRepository.save(article);
    }

    public Article reviewArticle(String reviewText, String trackingNumber) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("Makale bulunamadı");
        }

        Article article = articleOpt.get();
        article.setReviewComment(reviewText);
        article.setStatus("Değerlendirildi");
        article.setReviewDate(new Date());

        try {
            // Şifreli dosyayı çöz
            byte[] decryptedFile = encryptionService.decryptFile(article.getFile());

            PdfDocument pdfDoc = new PdfDocument();
            pdfDoc.loadFromBytes(decryptedFile);

            PdfPageBase newPage = pdfDoc.getPages().add();

            PdfTrueTypeFont titleFont = new PdfTrueTypeFont(new java.io.FileInputStream("C:\\Windows\\Fonts\\arial.ttf"), 16);
            PdfTrueTypeFont contentFont = new PdfTrueTypeFont(new java.io.FileInputStream("C:\\Windows\\Fonts\\arial.ttf"), 12);

            float startX = 50;
            float startY = 50;
            float lineHeight = 20;

            // Başlık ekle
            newPage.getCanvas().drawString("Hakem Değerlendirmesi", titleFont, PdfBrushes.getBlack(), startX, startY);
            startY += lineHeight * 2;

            String dateStr = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            newPage.getCanvas().drawString("Değerlendirme Tarihi: " + dateStr, contentFont, PdfBrushes.getBlack(), startX, startY);
            startY += lineHeight * 2;

            // Yorumu ekle - UTF-8 encoding kullanarak
            byte[] textBytes = reviewText.getBytes("UTF-8");
            String utf8Text = new String(textBytes, "UTF-8");
            String[] lines = utf8Text.split("\n");

            for (String line : lines) {
                List<String> wrappedText = wrapText(line, 500, contentFont);
                for (String wrappedLine : wrappedText) {
                    newPage.getCanvas().drawString(wrappedLine, contentFont, PdfBrushes.getBlack(), startX, startY);
                    startY += lineHeight;
                }
                startY += lineHeight / 2;
            }

            java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
            pdfDoc.saveToStream(output);

            // Değerlendirilmiş dosyayı şifrele ve kaydet
            byte[] encryptedReviewedFile = encryptionService.encryptFile(output.toByteArray());
            article.setReviewedFile(encryptedReviewedFile);

            pdfDoc.close();

            // Log kayıt işlemi
            Reviewer reviewer = article.getAssignedReviewer();
            if (reviewer != null) {
                String action = "Makale değerlendirildi";
                logService.createLog(trackingNumber, action, reviewer.getEmail(), "Hakem");
            }

        } catch (Exception e) {
            System.err.println("PDF yorumu eklenirken hata oluştu: " + e.getMessage());
            e.printStackTrace();

            try {
                // Hata durumunda orijinal dosyayı kopyala
                article.setReviewedFile(encryptionService.encryptFile(article.getFile()));
            } catch (Exception ex) {
                System.err.println("Şifreleme işleminde hata: " + ex.getMessage());
            }
        }

        return articleRepository.save(article);
    }

    // Metni sayfa genişliğine göre satırlara bölen yardımcı metod
    private java.util.List<String> wrapText(String text, float maxWidth, PdfTrueTypeFont font) {
        java.util.List<String> result = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }

        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine.toString() + (currentLine.length() > 0 ? " " : "") + word;
            if (font.measureString(testLine).getWidth() <= maxWidth) {
                if (currentLine.length() > 0) currentLine.append(" ");
                currentLine.append(word);
            } else {
                if (currentLine.length() > 0) {
                    result.add(currentLine.toString());
                    currentLine = new StringBuilder(word);
                } else {
                    // Tek bir kelime bile sığmıyorsa, zorla ekle
                    result.add(word);
                }
            }
        }

        if (currentLine.length() > 0) {
            result.add(currentLine.toString());
        }

        return result;
    }

    public Article updateStatus(String trackingNumber, String status) {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("Makale bulunamadı");
        }

        Article article = articleOpt.get();
        article.setStatus(status);

        try {
            String action = "Makale sisteme yüklendi";
            logService.createLog(trackingNumber, action, "admin@gmail.com", "Yazar");
        } catch (Exception e) {
            System.err.println("Log oluşturma sırasında hata: " + e.getMessage());
        }

        return articleRepository.save(article);
    }

    public Article anonimizeArticle(String trackingNumber) throws IOException {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("Makale bulunamadı");
        }

        Article article = articleOpt.get();

        // Şifreli dosyayı çöz
        byte[] decryptedFile = encryptionService.decryptFile(article.getFile());

        // 1. Metni ayıkla
        String extractedText = extractTextFromPDF(decryptedFile);

        // 2. Kelimeleri anonimleştir
        HashMap<String, String> anonymizedText = anonymizeText(extractedText);

        // 3. Kelimeleri değiştir ve PDF'i güncelle
        byte[] anonymizePdf = anonymizePdf(decryptedFile, anonymizedText);

        // 4. Resimleri ayıkla ve değiştir
        byte[] blurredPdf = extractBlurAndReplaceImages(anonymizePdf);

        // 5. Makalenin alanını bul
        List<String> specializations = findSpecializations(decryptedFile);

        // 6. İlgili Hakemi bul
        Reviewer reviewer = findReviewerbySpecializations(specializations);

        // 7. Anonimleştirilmiş dosyayı şifrele ve kaydet
        byte[] encryptedAnonymizedFile = encryptionService.encryptFile(blurredPdf);
        article.setAnonymizedFile(encryptedAnonymizedFile);

        article.setSpecializations(specializations);
        article.setAssignedReviewer(reviewer);
        article.setStatus("Değerlendirmede");

        try {
            String action = "Makale sisteme yüklendi";
            logService.createLog(trackingNumber, action, "admin@gmail.com", "Yazar");
        } catch (Exception e) {
            System.err.println("Log oluşturma sırasında hata: " + e.getMessage());
        }

        return articleRepository.save(article);
    }

    private String extractTextFromPDF(byte[] pdfBytes) throws IOException {
        // Parametre olarak gelen pdf zaten şifresi çözülmüş olduğundan direkt kullanabiliriz
        PDDocument document = PDDocument.load(pdfBytes);
        try {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } finally {
            document.close();
        }
    }

    private HashMap<String, String> anonymizeText(String text) {
        // Configure Stanford CoreNLP
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Process the text
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        // Create a mutable string for replacement
        StringBuilder anonymizedText = new StringBuilder(text);

        // Tanınan varlıklar için liste oluştur
        List<CoreEntityMention> entities = doc.entityMentions();
        Collections.reverse(entities);

        // Anonimleştirilecek varlık türlerini genişlet
        Set<String> allowedTypes = new HashSet<>(Arrays.asList(
                "PERSON", "ORGANIZATION", "LOCATION", "CITY",
                "COUNTRY", "EMAIL", "NATIONALITY", "SCHOOL", "UNIVERSITY"
        ));

        // Anonimleştirilmeyecek özel terimleri tanımla
        Set<String> protectedTerms = new HashSet<>(Arrays.asList(
                "EEG", "Fourier", "CNN", "deap", "DENS", "LSTM", "Valence"
        ));

        HashMap<String, String> replacements = new HashMap<>();

        for (CoreEntityMention entity : entities) {
            String entityType = entity.entityType();
            String entityText = entity.text();

            // Korunan terimlerden biriyse anonimleştirme yapma
            if (protectedTerms.contains(entityText)) {
                continue;
            }

            // Sadece istediğimiz türleri anonimleştir
            if (allowedTypes.contains(entityType)) {
                int start = entity.charOffsets().first();
                int end = entity.charOffsets().second();
                String replacement = "[" + entityType + "]";

                replacements.put(entityText, replacement);
                anonymizedText.replace(start, end, replacement);
            }
        }

        Pattern uniPattern = Pattern.compile("\\b(?:[A-Z][a-z]+(?:\\s(?:of|for|and|the|at|in)\\s)?)+University\\b|\\b[A-Z][a-z]+ Institute of Technology\\b");
        Matcher uniMatcher = uniPattern.matcher(text);

        while (uniMatcher.find()) {
            String match = uniMatcher.group();
            // Korunan terimlerden biriyse anonimleştirme yapma
            if (!protectedTerms.contains(match) && !replacements.containsKey(match)) {
                replacements.put(match, "[ORGANIZATION]");
            }
        }

        return replacements;
    }

    public byte[] anonymizePdf(byte[] pdfBytes,HashMap<String, String> anonymizedText) {
        // PDF'i yükle
        PdfDocument doc = new PdfDocument();
        doc.loadFromBytes(pdfBytes);

        HashMap<String,String> replacements = anonymizedText;

        // Tüm sayfalarda değiştirme yap
        for (int i = 0; i < doc.getPages().getCount(); i++) {
            PdfPageBase page = doc.getPages().get(i);

            // Metin bul
            PdfTextFinder finder = new PdfTextFinder(page);
            PdfTextFindOptions options = new PdfTextFindOptions();
            options.setTextFindParameter(EnumSet.of(TextFindParameter.WholeWord));

            // Bulunan her metin için
            PdfFont font = new PdfFont(PdfFontFamily.Helvetica, 10);
            for (Map.Entry<String, String> entry : replacements.entrySet()) {

                List<PdfTextFragment> fragments = finder.find(entry.getKey(), options);
                for (PdfTextFragment fragment : fragments) {
                    // Metin koordinatlarını al
                    Rectangle2D rec = fragment.getBounds()[0];

                    // Eski metni beyaz dikdörtgenle kapla
                    page.getCanvas().drawRectangle(PdfBrushes.getWhite(), rec);

                    // Yeni metni aynı konuma yaz
                    if(entry.getValue().equals("[PERSON]")) {
                        page.getCanvas().drawString("[PER]", font, PdfBrushes.getBlue(), rec.getX(), rec.getY()-1);
                    }
                    else if(entry.getValue().equals("[ORGANIZATION]")) {
                        page.getCanvas().drawString("[org]", font, PdfBrushes.getBlue(), rec.getX()-1, rec.getY()-1);
                    }
                    else if(entry.getValue().equals("[LOCATION]")) {
                        page.getCanvas().drawString("[LOC]", font, PdfBrushes.getBlue(), rec.getX()-1, rec.getY()-1);
                    }
                    else if(entry.getValue().equals("[NATIONALITY]")) {
                        page.getCanvas().drawString("[NATION]", font, PdfBrushes.getBlue(), rec.getX()-1, rec.getY()-1);
                    }
                    else
                        page.getCanvas().drawString(entry.getValue(), font, PdfBrushes.getBlue(), rec.getX(), rec.getY()-1);

                }
            }

        }
        // Bellek içinde byte dizisine dönüştür
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        doc.saveToStream(outputStream);
        byte[] result = outputStream.toByteArray();

        // Kaynakları serbest bırak
        doc.close();

        return result;
    }

    public byte[] extractBlurAndReplaceImages(byte[] pdfBytes) {
        try {
            // OpenCV kütüphanesini yükle
            nu.pattern.OpenCV.loadLocally();

            System.out.println("OpenCV Sürümü: " + Core.VERSION);

            // PDF dosyasını yükle
            PdfDocument doc = new PdfDocument();
            doc.loadFromBytes(pdfBytes);

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

            // Bellek içinde byte dizisine dönüştür
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            doc.saveToStream(outputStream, FileFormat.PDF);
            byte[] result = outputStream.toByteArray();

            // Kaynakları serbest bırak
            doc.close();

            return result;

        } catch (Exception e) {
            System.err.println("İşlem sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return pdfBytes; // Hata durumunda orijinal PDF'i döndür
        }
    }

    public List<String> findSpecializations(byte[] pdfBytes) throws IOException {
        try {
            // PDF'i yükle
            PDDocument document = PDDocument.load(pdfBytes);
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            document.close();

            // CoreNLP pipeline oluştur
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // Metni işle
            Annotation annotation = new Annotation(text);
            pipeline.annotate(annotation);

            // Stopwords listesi oluştur
            Set<String> stopwords = createStopwordsList();

            // Kelime sıklığı hesapla
            Map<String, Integer> wordFrequency = new HashMap<>();
            List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                    // Lemma kullanarak kelimenin kök formunu al
                    String lemma = token.get(CoreAnnotations.LemmaAnnotation.class).toLowerCase();
                    String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);

                    // Sadece anlamlı kelimeleri al ve stopwords'leri filtrele
                    if ((pos.startsWith("NN") || pos.startsWith("VB") || pos.startsWith("JJ")) &&
                            !stopwords.contains(lemma) && lemma.length() > 2) {
                        wordFrequency.put(lemma, wordFrequency.getOrDefault(lemma, 0) + 1);
                    }
                }
            }

            // En sık geçen kelimeleri bul
            List<Map.Entry<String, Integer>> sortedWords = new ArrayList<>(wordFrequency.entrySet());
            sortedWords.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // Kategori haritası
            Map<String, List<String>> categories = createCategoryKeywords();

            // Her kategori için puan hesapla
            Map<String, Double> categoryScores = new HashMap<>();

            for (Map.Entry<String, List<String>> category : categories.entrySet()) {
                String categoryName = category.getKey();
                List<String> keywords = category.getValue();

                double score = 0.0;

                // En sık geçen 100 kelime üzerinden analiz yap
                for (int i = 0; i < Math.min(100, sortedWords.size()); i++) {
                    String word = sortedWords.get(i).getKey();
                    int freq = sortedWords.get(i).getValue();

                    // Kelime bu kategoriye ait mi kontrol et
                    for (String keyword : keywords) {
                        if (word.equals(keyword) || keyword.contains(word)) {
                            score += freq;
                            break;
                        }
                    }
                }

                categoryScores.put(categoryName, score);
            }

            // Puanlara göre kategori sıralaması yap
            List<Map.Entry<String, Double>> sortedCategoryScores = new ArrayList<>(categoryScores.entrySet());
            sortedCategoryScores.sort((a, b) -> b.getValue().compareTo(a.getValue()));

            // En yüksek puanlı iki kategoriden oluşan listeyi döndür
            List<String> topSpecializations = new ArrayList<>();
            int numCategories = Math.min(2, sortedCategoryScores.size());

            for (int i = 0; i < numCategories; i++) {
                topSpecializations.add(sortedCategoryScores.get(i).getKey());
            }

            return topSpecializations;

        } catch (Exception e) {
            System.err.println("Metni işlerken hata oluştu: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Hata durumunda boş liste döndür
        }
    }

    public Reviewer findReviewerbySpecializations(List<String> specializations) {
        if (specializations == null || specializations.size() < 2) {
            return null;
        }

        List<Reviewer> reviewers = reviewerRepository.findAll();

        String firstSpecialization = specializations.get(0);
        String secondSpecialization = specializations.get(1);

        Reviewer bestMatch = null;

        for (Reviewer reviewer : reviewers) {
            List<String> reviewerSpecs = reviewer.getSpecializations();

            if (reviewerSpecs.contains(firstSpecialization) &&
                    reviewerSpecs.contains(secondSpecialization)) {
                return reviewer;
            }

            if (bestMatch == null && (reviewerSpecs.contains(firstSpecialization) ||
                    reviewerSpecs.contains(secondSpecialization))) {
                bestMatch = reviewer;
            }
        }

        return bestMatch;
    }

    public synchronized String generateTrackingNumber() {
        // Şu anki zamanı milisaniye olarak al ve ardından bir sayaç ekle
        long timestamp = System.currentTimeMillis();
        int counter = getAndIncrementCounter(); // Bir önceki değeri alır ve artırır

        // Sayının son 8 hanesini al
        String timeComponent = String.valueOf(timestamp).substring(5);
        String counterComponent = String.format("%03d", counter % 1000);

        return timeComponent + counterComponent;
    }

    private int counter = 0;

    private synchronized int getAndIncrementCounter() {
        int result = counter;
        counter = (counter + 1) % 1000; // 0-999 arası döner
        return result;
    }

    public int getReviewersActiveArticlesCount(Reviewer reviewer) {
        List<Article> articles = articleRepository.findByAssignedReviewer(reviewer);

        if (articles.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Article article : articles) {
            if (article.getStatus().equals("Değerlendirmede")) {
                count++;
            }
        }

        return count;
    }

    public int getReviewersCompletedArticlesCount(Reviewer reviewer) {
        List<Article> articles = articleRepository.findByAssignedReviewer(reviewer);

        if (articles.isEmpty()) {
            return 0;
        }

        int count = 0;
        for (Article article : articles) {
            if (article.getStatus().equals("Değerlendirildi") || article.getStatus().equals("Yazara İletildi")) {
                count++;
            }
        }

        return count;
    }

    public void deleteReviewerFromArticle(Reviewer reviewer) {
        List<Article> articles = articleRepository.findByAssignedReviewer(reviewer);

        if(articles.isEmpty()) {
            return;
        }

        for(Article article : articles) {
            article.setAssignedReviewer(null);
        }

        articleRepository.saveAll(articles);
    }

    private static Map<String, List<String>> createCategoryKeywords() {
        Map<String, List<String>> categories = new HashMap<>();

        // Artificial Intelligence and Machine Learning (AI/ML)
        categories.put("Deep Learning", Arrays.asList(
                "deep learning", "neural network", "cnn", "rnn", "lstm", "transformer",
                "backpropagation", "gradient descent", "activation function", "dropout",
                "batch normalization", "overfitting", "underfitting", "attention mechanism", "autoencoder",
                "generative adversarial networks", "gan", "reinforcement learning", "q-learning", "policy gradient",
                "supervised learning", "unsupervised learning", "semi-supervised learning", "self-supervised learning", "transfer learning"
        ));

        categories.put("Natural Language Processing", Arrays.asList(
                "nlp", "tokenization", "stemming", "lemmatization", "word embedding",
                "word2vec", "glove", "bert", "transformer", "pos tagging",
                "named entity recognition", "ner", "dependency parsing", "sentiment analysis", "language modeling",
                "seq2seq", "attention mechanism", "machine translation", "text classification", "word segmentation",
                "text-to-speech", "speech recognition", "zero-shot learning", "few-shot learning", "pretrained model"
        ));

        categories.put("Computer Vision", Arrays.asList(
                "computer vision", "image processing", "feature extraction", "image segmentation", "object detection",
                "yolo", "faster r-cnn", "ssd", "edge detection", "hough transform",
                "contour detection", "image classification", "face recognition", "pose estimation", "semantic segmentation",
                "instance segmentation", "optical flow", "stereo vision", "depth estimation", "gan for images",
                "ocr", "image super-resolution", "data augmentation", "visual attention", "self-supervised vision"
        ));

        categories.put("Generative AI", Arrays.asList(
                "generative ai", "gpt", "chatgpt", "text generation", "style transfer",
                "stable diffusion", "dalle", "midjourney", "latent diffusion models", "autoencoder",
                "vae", "gan", "cycle gan", "image synthesis", "neural rendering",
                "text-to-image", "text-to-video", "music generation", "data augmentation", "artificial creativity",
                "deepfake", "neural texture", "variational inference", "latent space", "transformer-based generation"
        ));

        // Human-Computer Interaction (HCI)
        categories.put("Brain-Computer Interfaces", Arrays.asList(
                "bci", "brain-computer interface", "eeg", "fmri", "brain signal processing",
                "neural decoding", "brainwave", "motor imagery", "neural implant", "non-invasive bci",
                "p300 speller", "steady-state visual evoked potential", "ssvep", "brain-to-text", "mind control interface",
                "neurofeedback", "brain state classification", "eye-tracking", "brain oscillation", "adaptive bci",
                "motor cortex interface", "cognitive load measurement", "closed-loop bci", "brain-inspired computing", "neuroprosthetics"
        ));

        categories.put("User Experience (UX) Design", Arrays.asList(
                "ux", "user experience", "usability testing", "heuristic evaluation", "wireframing",
                "prototyping", "a/b testing", "human factors", "cognitive load", "user journey mapping",
                "persona development", "affordance", "user engagement", "visual hierarchy", "interaction design",
                "gestalt principles", "hci", "emotion-driven design", "accessibility", "universal design",
                "responsive design", "microinteractions", "design thinking", "iterative design", "eye-tracking analysis"
        ));

        categories.put("Augmented and Virtual Reality", Arrays.asList(
                "augmented reality", "virtual reality", "mixed reality", "ar", "vr",
                "hmd", "spatial computing", "occlusion", "marker-based tracking", "inside-out tracking",
                "outside-in tracking", "3d reconstruction", "haptic feedback", "stereoscopic rendering", "pose tracking",
                "hand tracking", "metaverse", "vr locomotion", "digital twin", "immersive analytics",
                "foveated rendering", "motion sickness mitigation", "eye-tracking in vr", "gesture recognition", "virtual environment"
        ));

        // Big Data and Data Analytics
        categories.put("Data Mining", Arrays.asList(
                "data mining", "association rules", "apriori algorithm", "clustering", "k-means",
                "hierarchical clustering", "anomaly detection", "classification", "decision tree", "random forest",
                "feature selection", "dimensionality reduction", "principal component analysis", "pca", "latent dirichlet allocation",
                "lda", "data preprocessing", "outlier detection", "market basket analysis", "pattern recognition",
                "text mining", "web mining", "graph mining", "sequential pattern mining", "unsupervised learning"
        ));

        categories.put("Data Visualization", Arrays.asList(
                "data visualization", "charting", "bar chart", "line graph", "scatter plot",
                "heatmap", "treemap", "choropleth map", "geospatial visualization", "dashboards",
                "tableau", "power bi", "ggplot", "seaborn", "matplotlib",
                "d3.js", "plotly", "network graph", "parallel coordinates", "bubble chart",
                "streamgraph", "sankey diagram", "histogram", "data storytelling", "infographic design"
        ));

        categories.put("Data Processing Systems", Arrays.asList(
                "hadoop", "spark", "mapreduce", "yarn", "hdfs",
                "hive", "pig", "tez", "flink", "kafka",
                "storm", "beam", "presto", "trino", "bigquery",
                "distributed computing", "parallel processing", "real-time analytics", "batch processing", "etl",
                "nosql", "columnar storage", "key-value store", "graph processing", "spark streaming"
        ));

        categories.put("Time Series Analysis", Arrays.asList(
                "time series", "forecasting", "seasonality", "trend analysis", "exponential smoothing",
                "moving average", "autoregressive model", "arima", "sarima", "holt-winters",
                "stationarity", "differencing", "cointegration", "vector autoregression", "var",
                "granger causality", "anomaly detection", "kalman filter", "lstm for time series", "prophet",
                "longitudinal data", "wavelet transform", "dynamic time warping", "self-similarity", "time-dependent patterns"
        ));

        // Cybersecurity
        categories.put("Encryption Algorithms", Arrays.asList(
                "encryption", "symmetric encryption", "asymmetric encryption", "aes", "rsa",
                "elliptic curve cryptography", "ecc", "hash function", "sha-256", "md5",
                "hmac", "pbkdf2", "bcrypt", "argon2", "digital signature",
                "key exchange", "diffie-hellman", "homomorphic encryption", "quantum encryption", "zero-knowledge proof",
                "tls encryption", "end-to-end encryption", "cipher modes", "gcm", "cbc"
        ));

        categories.put("Secure Software Development", Arrays.asList(
                "secure coding", "owasp top 10", "input validation", "sql injection", "cross-site scripting",
                "xss", "csrf", "buffer overflow", "session hijacking", "secure authentication",
                "code review", "threat modeling", "secure design", "access control", "least privilege",
                "logging and monitoring", "dependency management", "secure api", "penetration testing", "secure software lifecycle",
                "security patches", "code obfuscation", "reverse engineering prevention", "security automation", "devsecops"
        ));

        categories.put("Network Security", Arrays.asList(
                "firewall", "intrusion detection system", "intrusion prevention system", "vpn", "zero trust security",
                "dos attack", "ddos attack", "port scanning", "packet sniffing", "tls",
                "ssl", "secure shell", "ssh", "radius", "ipsec",
                "dns security", "man-in-the-middle attack", "mitm", "honeypot", "security gateway",
                "zero-day exploit", "phishing protection", "endpoint security", "network segmentation", "siem"
        ));

        categories.put("Authentication Systems", Arrays.asList(
                "authentication", "authorization", "single sign-on", "sso", "multi-factor authentication",
                "mfa", "oauth", "openid connect", "biometric authentication", "fingerprint recognition",
                "facial recognition", "password hashing", "session management", "jwt", "hardware tokens",
                "fido2", "smart card authentication", "identity provider", "adaptive authentication", "time-based otp",
                "social login", "role-based access control", "rbac", "attribute-based access control", "abac", "privileged access management"
        ));

        categories.put("Digital Forensics", Arrays.asList(
                "digital forensics", "computer forensics", "mobile forensics", "malware analysis", "memory forensics",
                "disk forensics", "log analysis", "network forensics", "forensic imaging", "hash analysis",
                "metadata analysis", "volatility framework", "autopsy", "cybercrime investigation", "incident response",
                "evidence acquisition", "steganography detection", "rootkit analysis", "timeline analysis", "data carving",
                "malware reverse engineering", "forensic reporting", "threat intelligence", "legal compliance", "chain of custody"
        ));

        // Networking and Distributed Systems
        categories.put("5G and Next-Generation Networks", Arrays.asList(
                "5g", "network slicing", "massive mimo", "beamforming", "low latency communication",
                "private 5g", "edge computing", "network function virtualization", "nfv", "software-defined networking",
                "sdn", "orchestrator", "millimeter wave", "dynamic spectrum sharing", "openran",
                "carrier aggregation", "ultra-reliable low-latency communication", "urllc", "internet of things", "iot security",
                "backhaul network", "small cells", "cognitive radio", "6g research", "latency optimization"
        ));

        categories.put("Cloud Computing", Arrays.asList(
                "cloud computing", "iaas", "paas", "saas", "serverless computing",
                "virtual machines", "kubernetes", "docker", "microservices", "api gateway",
                "hybrid cloud", "multi-cloud", "cloud orchestration", "cloud security", "cloud cost optimization",
                "cloud-native applications", "autoscaling", "load balancing", "edge computing", "cloud storage",
                "object storage", "file system storage", "cloud backup", "disaster recovery", "content delivery network"
        ));

        categories.put("Blockchain Technology", Arrays.asList(
                "blockchain", "distributed ledger", "smart contract", "ethereum", "bitcoin",
                "consensus mechanism", "proof of work", "proof of stake", "decentralized finance", "defi",
                "layer 2 scaling", "rollups", "zk-snarks", "zk-starks", "merkle tree",
                "tokenization", "non-fungible tokens", "nft", "dao", "governance token",
                "crypto wallets", "cold storage", "51% attack", "interoperability", "cross-chain transactions"
        ));

        categories.put("Peer-to-Peer (P2P) and Decentralized Systems", Arrays.asList(
                "p2p", "peer-to-peer network", "torrenting", "distributed hash table", "dht",
                "gossip protocol", "content-addressable storage", "interplanetary file system", "ipfs", "blockchain nodes",
                "decentralized web", "dweb", "network resilience", "fault tolerance", "mesh networking",
                "edge computing", "distributed consensus", "federated learning", "zero-trust architecture", "self-sovereign identity",
                "peer validation", "anonymous communication", "darknet", "privacy-enhancing technologies", "p2p payments"
        ));

        return categories;
    }

    private static Set<String> createStopwordsList() {
        // İngilizce yaygın stopwords listesi (genişletilmiş)
        return new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "by", "in",
                "of", "with", "as", "is", "are", "was", "were", "be", "been", "being", "have", "has",
                "had", "do", "does", "did", "will", "would", "shall", "should", "may", "might",
                "must", "can", "could", "i", "you", "he", "she", "it", "we", "they", "this", "that",
                "these", "those", "my", "your", "his", "her", "its", "our", "their", "mine", "yours",
                "hers", "ours", "theirs", "who", "whom", "which", "what", "where", "when", "why", "how",
                "all", "any", "both", "each", "few", "more", "most", "some", "such", "no", "not", "only",
                "own", "same", "so", "than", "too", "very", "just", "also", "ever", "once", "under", "over",
                "again", "further", "then", "here", "there", "first", "second", "third", "one", "two", "three",
                "four", "five", "six", "seven", "eight", "nine", "ten", "about", "above", "across", "after",
                "against", "along", "among", "around", "before", "behind", "below", "beneath", "beside",
                "between", "beyond", "during", "except", "into", "through", "toward", "towards", "upon",
                "within", "without", "use", "used", "using", "show", "shown", "shows", "make", "made", "makes",
                "like", "likely", "different", "et", "al", "etc", "figure", "fig", "table", "section", "example",
                "paper", "papers", "however", "thus", "therefore", "hence", "since", "due", "because", "whereas",
                "while", "though", "although", "otherwise", "else", "instead", "unless", "whether", "moreover"
        ));
    }

}