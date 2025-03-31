package com.example.documentanonymization.service;

import com.example.documentanonymization.dto.ArticleDto;
import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.repository.ArticleRepository;
import com.example.documentanonymization.repository.ReviewerRepository;
import com.spire.pdf.FileFormat;
import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfBrushes;
import com.spire.pdf.graphics.PdfFont;
import com.spire.pdf.graphics.PdfFontFamily;
import com.spire.pdf.graphics.PdfImage;
import com.spire.pdf.texts.PdfTextFindOptions;
import com.spire.pdf.texts.PdfTextFinder;
import com.spire.pdf.texts.PdfTextFragment;
import com.spire.pdf.texts.TextFindParameter;
import com.spire.pdf.utilities.PdfImageHelper;
import com.spire.pdf.utilities.PdfImageInfo;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
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

        return new ResponseEntity<>(article.getFile(), headers, HttpStatus.OK);
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

        return new ResponseEntity<>(article.getAnonymizedFile(), headers, HttpStatus.OK);
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
        article.setFile(file.getBytes());
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
        article.setAssignedReviewer(reviewer);
        article.setStatus("Değerlendirmede");

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
        article.setReviewedFile(article.getFile()); //commenti mevcut pdf'in sonuna ekle
        return articleRepository.save(article);
    }

    public Article anonimizeArticle(String trackingNumber) throws IOException {
        Optional<Article> articleOpt = articleRepository.findByTrackingNumber(trackingNumber);

        if (articleOpt.isEmpty()) {
            throw new IllegalArgumentException("Makale bulunamadı");
        }

        Article article = articleOpt.get();
        byte[] pdfBytes = article.getFile();


        // 1. Extract text from PDF
        String extractedText = extractTextFromPDF(pdfBytes);

        // 2. Anonymize text using Stanford NLP
        HashMap<String, String> anonymizedText = anonymizeText(extractedText);

        // 3. Create a new PDF with anonymized text
        byte[] anonymizePdf = anonymizePdf(article.getFile(), anonymizedText);

        //4. Extract and replace images
        byte[] blurredPdf = extractBlurAndReplaceImages(anonymizePdf);

        // 5. Update the article with anonymized file
        article.setAnonymizedFile(blurredPdf);
        article.setStatus("Anonimleştirildi");
        //

        return articleRepository.save(article);
    }

    private String extractTextFromPDF(byte[] pdfBytes) throws IOException {
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

        HashMap<String, String> replacements = new HashMap<>();

        for (CoreEntityMention entity : entities) {
            String entityType = entity.entityType();

            // Sadece istediğimiz türleri anonimleştir
            if (allowedTypes.contains(entityType)) {
                int start = entity.charOffsets().first();
                int end = entity.charOffsets().second();
                String replacement = "[" + entityType + "]";

                replacements.put(entity.text(), replacement);
                anonymizedText.replace(start, end, replacement);
            }
        }

        Pattern uniPattern = Pattern.compile("\\b(?:[A-Z][a-z]+(?:\\s(?:of|for|and|the|at|in)\\s)?)+University\\b|\\b[A-Z][a-z]+ Institute of Technology\\b");
        Matcher uniMatcher = uniPattern.matcher(text);

        while (uniMatcher.find()) {
            String match = uniMatcher.group();
            if (!replacements.containsKey(match)) {
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


            // Kaydet ve kapat
            doc.saveToFile("C:\\Users\\berkc\\Downloads\\bitti.pdf");
            doc.close();
            return null;

        } catch (Exception e) {
            System.err.println("İşlem sırasında hata oluştu: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
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
            if (article.getStatus().equals("Değerlendirildi")) {
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
}