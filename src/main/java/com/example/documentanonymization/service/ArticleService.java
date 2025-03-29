package com.example.documentanonymization.service;

import com.example.documentanonymization.dto.ArticleDto;
import com.example.documentanonymization.entity.Article;
import com.example.documentanonymization.entity.Reviewer;
import com.example.documentanonymization.repository.ArticleRepository;
import com.example.documentanonymization.repository.ReviewerRepository;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
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
        String anonymizedText = anonymizeText(extractedText);

        // 3. Create a new PDF with anonymized text
        byte[] anonymizedPdfBytes = createAnonymizedPDF(anonymizedText, article.getFile());

        // 4. Update the article with anonymized file
        article.setAnonymizedFile(anonymizedPdfBytes);
        article.setStatus("Anonimleştirildi");

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

    private String anonymizeText(String text) {
        // Configure Stanford CoreNLP
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Process the text
        CoreDocument doc = new CoreDocument(text);
        pipeline.annotate(doc);

        // Create a mutable string for replacement
        StringBuilder anonymizedText = new StringBuilder(text);

        // Process all entity mentions for anonymization
        // Tersten işleme yapılmasının nedeni: Önceki değişikliklerin sonraki varlıkların pozisyonlarını etkilememesi için
        List<CoreEntityMention> entities = doc.entityMentions();
        Collections.reverse(entities);

        for (CoreEntityMention entity : entities) {
            String entityType = entity.entityType();
            int start = entity.charOffsets().first();
            int end = entity.charOffsets().second();

            // Replace with generic placeholder based on entity type
            String replacement = "[" + entityType + "]";
            anonymizedText.replace(start, end, replacement);
        }

        return anonymizedText.toString();
    }

    private byte[] createAnonymizedPDF(String anonymizedText, byte[] pdfBytes) throws IOException {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 12);
            contentStream.newLineAtOffset(50, 700);

            float yPosition = 700;

            // Split text into lines and sanitize each line
            String[] lines = anonymizedText.split("\n");
            for (String line : lines) {
                // Sanitize the text by removing problematic characters
                String sanitizedLine = sanitizeText(line);

                // Skip empty lines
                if (sanitizedLine.trim().isEmpty()) {
                    continue;
                }

                // Check if we need a new page
                if (yPosition < 50) {
                    contentStream.endText();
                    contentStream.close();

                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);

                    // Create new content stream and reassign it to the variable
                    contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    yPosition = 700;
                    contentStream.newLineAtOffset(50, yPosition);
                }

                // Process line text in chunks
                int chunkSize = 80;
                for (int i = 0; i < sanitizedLine.length(); i += chunkSize) {
                    int end = Math.min(i + chunkSize, sanitizedLine.length());
                    String chunk = sanitizedLine.substring(i, end);

                    contentStream.showText(chunk);

                    if (i + chunkSize < sanitizedLine.length()) {
                        contentStream.newLineAtOffset(0, -15);
                        yPosition -= 15;
                    }
                }

                contentStream.newLineAtOffset(0, -15);
                yPosition -= 15;
            }

            contentStream.endText();
            contentStream.close();

            document.save(outputStream);
            return outputStream.toByteArray();
        }
    }

    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }

        // Remove control characters and other problematic characters
        StringBuilder sanitized = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            // Skip control characters (0-31) and other problematic ones
            if (c > 31 && c < 127) {
                sanitized.append(c);
            } else if (c == '\t') {
                // Replace tabs with spaces
                sanitized.append("    ");
            }
            // Skip all other control characters
        }

        return sanitized.toString();
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