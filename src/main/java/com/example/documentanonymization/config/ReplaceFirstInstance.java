package com.example.documentanonymization.config;

import com.spire.pdf.PdfDocument;
import com.spire.pdf.PdfPageBase;
import com.spire.pdf.graphics.PdfBrushes;
import com.spire.pdf.graphics.PdfFont;
import com.spire.pdf.graphics.PdfFontFamily;
import com.spire.pdf.texts.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class ReplaceFirstInstance {

    public static void main(String[] args) throws IOException {
        nlpBasedClassification();
    }

    public void deneme(byte[] pdfBytes,String anonymizedText) {
        // PDF'i yükle
        PdfDocument doc = new PdfDocument();
        doc.loadFromBytes(pdfBytes);

        // Tüm sayfalarda değiştirme yap
        for (int i = 0; i < doc.getPages().getCount(); i++) {
            PdfPageBase page = doc.getPages().get(i);

            // Metin bul
            PdfTextFinder finder = new PdfTextFinder(page);
            PdfTextFindOptions options = new PdfTextFindOptions();
            options.setTextFindParameter(EnumSet.of(TextFindParameter.WholeWord));

            List<PdfTextFragment> fragments = finder.find("Emotion", options);

            // Bulunan her metin için
            PdfFont font = new PdfFont(PdfFontFamily.Helvetica, 10);
            for (PdfTextFragment fragment : fragments) {
                // Metin koordinatlarını al
                Rectangle2D rec = fragment.getBounds()[0];

                // Eski metni beyaz dikdörtgenle kapla
                page.getCanvas().drawRectangle(PdfBrushes.getWhite(), rec);

                // Yeni metni aynı konuma yaz
                page.getCanvas().drawString("hello", font, PdfBrushes.getBlue(), rec.getX(), rec.getY()-1);
            }
        }

        // Kaydet ve kapat
        doc.saveToFile("C:\\Users\\berkc\\Downloads\\ReplaceFirstInstance.pdf");
        System.out.println("PDF başarıyla güncellendi: C:\\Users\\berkc\\Downloads\\ReplaceFirstInstance.pdf");
        doc.close();
    }

    public void deneme2(byte[] pdfBytes,String anonymizedText) {
        // Set up the pipeline with properties file
        StanfordCoreNLP pipeline = new StanfordCoreNLP("props.properties");

        // Create an empty Annotation
        Annotation document = new Annotation("Stanford University is located in California.");

        // Annotate the document
        pipeline.annotate(document);

        // Get the annotated sentences
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        // Iterate over each sentence
        for (CoreMap sentence : sentences) {
            System.out.println("Sentence: " + sentence);
            // Iterate over each token in the sentence
            sentence.get(CoreAnnotations.TokensAnnotation.class).forEach(token -> {
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                System.out.println("Word: " + word + ", POS: " + pos + ", NER: " + ne);
            });
        }
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

    public static void nlpBasedClassification() throws IOException {
        // PDF'i yükle
        File file = new File("C:\\Users\\berkc\\Downloads\\örnek_makale4.pdf");
        PDDocument document = PDDocument.load(file);
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

        System.out.println("En sık geçen kelimeler (ilk 20):");
        for (int i = 0; i < Math.min(20, sortedWords.size()); i++) {
            System.out.println(sortedWords.get(i).getKey() + ": " + sortedWords.get(i).getValue());
        }

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

        // En yüksek puanlı iki kategoriyi yazdır
        System.out.println("En yüksek puanlı alanlar:");
        for (int i = 0; i < Math.min(2, sortedCategoryScores.size()); i++) {
            String category = sortedCategoryScores.get(i).getKey();
            double score = sortedCategoryScores.get(i).getValue();
            System.out.println((i+1) + ". " + category + ": " + score);
        }

        System.out.println("Tüm NLP puanları: " + categoryScores);
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