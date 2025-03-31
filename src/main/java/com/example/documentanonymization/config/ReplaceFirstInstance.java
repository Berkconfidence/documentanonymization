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

    public static void advancedTextClassification() throws IOException {
        // PDF'i yükle
        File file = new File("C:\\Users\\berkc\\Downloads\\örnek_makale1.pdf");
        PDDocument document = PDDocument.load(file);

        try {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            // Metni temizle ve kelime dizisine dönüştür
            String[] words = preprocessText(text);

            // Kategori haritası
            Map<String, List<String>> categories = createCategoryKeywords();

            // TF-IDF hesapla
            Map<String, Double> scores = calculateTFIDF(words, categories);

            // En yüksek puanlı kategoriyi bul
            String dominantCategory = findDominantCategoryByTFIDF(scores);
            System.out.println("Makalenin muhtemel alanı (TF-IDF): " + dominantCategory);
            System.out.println("TF-IDF puanları: " + scores);

        } finally {
            document.close();
        }
    }

    private static Map<String, List<String>> createCategoryKeywords() {
        Map<String, List<String>> categories = new HashMap<>();

        categories.put("Artificial Intelligence and Machine Learning", Arrays.asList(
                "artificial intelligence", "machine learning", "deep learning", "neural network",
                "training data", "algorithm", "supervised learning", "unsupervised learning",
                "classification", "clustering", "regression", "decision tree", "random forest",
                "natural language processing", "nlp", "computer vision", "generative ai",
                "gpt", "chatgpt", "transformer", "reinforcement learning", "ai",

                // Eklenenler (100 yeni kelime)
                "activation function", "adversarial learning", "ai ethics", "ai safety",
                "attention mechanism", "autoencoder", "backpropagation", "bayesian networks",
                "beam search", "bias-variance tradeoff", "bidirectional lstm", "biggan",
                "capsule network", "character-level modeling", "cnn", "contrastive learning",
                "convolutional neural network", "cost function", "cross-entropy loss", "cyclegan",
                "data augmentation", "dataset balancing", "deep belief network", "deep generative model",
                "deep reinforcement learning", "differentiable programming", "dimensionality reduction",
                "discriminator", "dropout", "dynamic computation graphs", "embedding",
                "encoder-decoder", "epoch", "error function", "explainable ai",
                "feature extraction", "feature selection", "feedforward neural network", "fine-tuning",
                "forward propagation", "fuzzy logic", "gan", "generalization",
                "gradient boosting", "gradient clipping", "gradient descent", "graph neural network",
                "gru", "hallucination", "hierarchical clustering", "image segmentation",
                "imitation learning", "in-context learning", "inductive bias", "instance-based learning",
                "inverse reinforcement learning", "jax", "k-means clustering", "kernel method",
                "latent variable", "learning rate", "lstm", "markov decision process",
                "meta-learning", "mixture of experts", "mode collapse", "monte carlo tree search",
                "multi-modal learning", "multi-task learning", "nas", "neuro-symbolic ai",
                "next-token prediction", "nlp pipeline", "object detection", "one-shot learning",
                "optical character recognition", "optimizers", "parameter tuning", "perceptron",
                "policy gradient", "pos tagging", "pre-trained model", "predictive modeling",
                "q-learning", "quantization", "reinforcement signal", "residual networks",
                "self-attention", "self-supervised learning", "semi-supervised learning", "sequence modeling",
                "sgd", "speech recognition", "stable diffusion", "stylegan",
                "subword tokenization", "syntactic parsing", "temporal difference learning", "tensorflow",
                "text-to-image", "text-to-speech", "turing test", "unsupervised pretraining",
                "variational autoencoder", "vector embeddings", "word2vec", "zero-shot learning"
        ));

        categories.put("Human-Computer Interaction", Arrays.asList(
                "human-computer interaction", "hci", "user interface", "ui", "user experience",
                "ux", "usability", "brain-computer interface", "bci", "augmented reality",
                "virtual reality", "ar", "vr", "mixed reality", "gesture recognition",
                "interaction design", "user-centered design", "human factors",

                // Eklenenler (100 yeni kelime)
                "adaptive interfaces", "affective computing", "biometrics", "brain-machine interface",
                "cognitive ergonomics", "cognitive load", "computer-mediated communication", "context-aware computing",
                "crossmodal interaction", "digital accessibility", "digital ergonomics", "embodied interaction",
                "emotion recognition", "ergonomics", "eye gaze tracking", "eye movement analysis",
                "facial expression recognition", "fitts law", "force feedback", "gaze-based interaction",
                "gaze estimation", "gesture-based interaction", "haptic feedback", "haptic interfaces",
                "head-mounted display", "hmd", "human augmentation", "human-centered ai",
                "immersive computing", "immersive interaction", "invisible computing", "kinesthetic feedback",
                "kinesthetic learning", "kinaesthetic interaction", "locomotion interfaces", "man-machine interaction",
                "mental workload", "metaverse", "mocap", "motion capture",
                "multimodal interaction", "multisensory feedback", "neural interface", "neuromorphic computing",
                "non-verbal communication", "perception engineering", "perception-based computing", "pervasive computing",
                "physiological computing", "psychophysiology", "qualitative hci", "quantified self",
                "reality-virtuality continuum", "remote presence", "sensory augmentation", "sensory substitution",
                "social computing", "social presence", "spatial computing", "speech interaction",
                "speech processing", "subliminal interaction", "synchronous collaboration", "tangible computing",
                "tangible interfaces", "telepresence", "touchless interaction", "ubiquitous computing",
                "user adaptation", "user cognition", "user emotion", "user engagement",
                "user modeling", "user perception", "user personalization", "user profiling",
                "user state detection", "user trust", "user-centered ai", "visual ergonomics",
                "wearable computing", "wearable hci", "wearable sensors", "xr",
                "zero-ui", "brain signal processing", "neuroergonomics", "brainwave interaction",
                "eeg", "ecg", "fnirs", "meg",
                "tms", "brain decoding", "brain stimulation", "neurofeedback",
                "neural signal processing", "cognitive neuroscience", "brain-inspired computing", "affective hci"
        ));

        categories.put("Big Data and Data Analytics", Arrays.asList(
                "big data", "data analytics", "data mining", "data visualization", "hadoop",
                "spark", "data warehouse", "time series", "data processing", "data science",
                "predictive analytics", "business intelligence", "bi", "etl", "data lake",
                "nosql", "data stream", "tableau", "power bi",

                // Eklenenler (100 yeni kelime)
                "apache flink", "apache kafka", "apache storm", "aws glue",
                "batch processing", "beam", "benchmarking", "business analytics",
                "churn analysis", "clickstream data", "columnar storage", "complex event processing",
                "confluent kafka", "cross-validation", "customer segmentation", "cyber analytics",
                "data aggregation", "data anomaly detection", "data blending", "data catalog",
                "data cleaning", "data cube", "data engineering", "data ethics",
                "data governance", "data imputation", "data integration", "data lakehouse",
                "data lineage", "data mart", "data monetization", "data observability",
                "data ops", "data pipeline", "data quality", "data replication",
                "data silos", "data storytelling", "data transformation", "data wrangling",
                "decision support system", "delta lake", "descriptive analytics", "distributed computing",
                "druid", "edge analytics", "elasticsearch", "event-driven architecture",
                "exabyte", "feature engineering", "forecasting", "fuzzy clustering",
                "geospatial analytics", "google bigquery", "graph analytics", "greenplum",
                "hbase", "high-dimensional data", "hot data", "ibm cognos",
                "in-memory computing", "iot analytics", "jupyter notebook", "k-means clustering",
                "key-value store", "kpi tracking", "lambda architecture", "live data processing",
                "log analysis", "machine data", "mapreduce", "mlflow",
                "mongodb", "multi-tenant analytics", "olap", "oltp",
                "pandas", "parquet", "petabyte", "phishing detection analytics",
                "prescriptive analytics", "python for data science", "quantitative analytics", "query optimization",
                "real-time analytics", "recommendation systems", "redshift", "revenue forecasting",
                "schema evolution", "semi-structured data", "sentiment analysis", "snowflake",
                "spatial data", "sql analytics", "stream processing", "teradata",
                "time-series forecasting", "variance analysis", "vector database", "zookeeper"
        ));

        categories.put("Cybersecurity", Arrays.asList(
                "cybersecurity", "security", "encryption", "cryptography", "firewall",
                "malware", "virus", "ransomware", "phishing", "authentication", "authorization",
                "vpn", "secure coding", "penetration testing", "vulnerability", "exploit",
                "zero-day", "data breach", "digital forensics", "intrusion detection",

                // Eklenenler (100 yeni kelime)
                "access control", "adversarial attack", "aes", "anti-virus",
                "api security", "attack vector", "authentication protocol", "biometric authentication",
                "blue team", "botnet", "brute force attack", "buffer overflow",
                "bug bounty", "certificate authority", "cissp", "cloud security",
                "command injection", "compliance", "countermeasure", "credential stuffing",
                "cross-site scripting", "cyber threat intelligence", "cyber warfare", "cyber hygiene",
                "data encryption standard", "data leakage prevention", "ddos attack", "deep packet inspection",
                "defense in depth", "disk encryption", "dns poisoning", "domain hijacking",
                "dos attack", "eavesdropping", "elliptic curve cryptography", "ethical hacking",
                "event correlation", "federated identity", "fingerprinting", "forensic analysis",
                "gpg", "hash function", "honeypot", "hsts",
                "identity and access management", "incident response", "infosec", "intrusion prevention system",
                "iot security", "ipsec", "iso 27001", "key exchange",
                "kerberos", "least privilege principle", "mac spoofing", "man-in-the-middle attack",
                "multi-factor authentication", "nist", "obfuscation", "oauth",
                "open redirect", "owasp", "pass-the-hash attack", "password cracking",
                "password hashing", "patch management", "pgp", "physical security",
                "pii protection", "privilege escalation", "public key infrastructure", "quantum cryptography",
                "rce (remote code execution)", "red team", "reverse engineering", "risk assessment",
                "rootkit", "rsa", "saml", "sandboxing",
                "sensitive data exposure", "session hijacking", "side-channel attack", "signature-based detection",
                "social engineering", "software security", "spam filtering", "spyware",
                "sql injection", "ssh", "ssl/tls", "sso",
                "steganography", "supply chain attack", "symmetric encryption", "tamper detection",
                "threat hunting", "threat modeling", "two-factor authentication", "usb malware",
                "vishing", "vpn tunneling", "web application firewall", "white hat hacking",
                "worm", "zero trust security", "zero-knowledge proof", "zombie computer"
        ));

        categories.put("Networking and Distributed Systems", Arrays.asList(
                "networking", "distributed system", "blockchain", "5g", "cloud computing",
                "peer-to-peer", "p2p", "decentralized", "smart contract", "consensus algorithm",
                "proof of work", "proof of stake", "ethereum", "bitcoin", "distributed ledger",
                "network protocol", "tcp/ip", "dns", "cdn", "sdn", "network security",

                // Eklenenler (100 yeni kelime)
                "6g", "adaptive streaming", "autonomous network", "bandwidth", "beaconing",
                "bft (byzantine fault tolerance)", "broadband", "byzantine consensus", "cache",
                "cellular network", "centralized network", "cloud-native networking", "content delivery network",
                "data center", "decentralized finance", "dht (distributed hash table)", "dlt (distributed ledger technology)",
                "edge computing", "elastic network", "fog computing", "gateway",
                "grid computing", "handover", "hyperledger", "hypervisor",
                "interoperability", "ip address", "ipv4", "ipv6",
                "kubernetes networking", "latency", "load balancing", "lora (long range)",
                "lte", "manet (mobile ad hoc network)", "mesh network", "mimo (multiple input multiple output)",
                "mobile edge computing", "multicast", "name resolution", "nfc (near field communication)",
                "network function virtualization", "network slicing", "network topology", "nft (non-fungible token)",
                "node", "off-chain", "on-chain", "openflow",
                "overlay network", "packet switching", "peer discovery", "peer synchronization",
                "permissioned blockchain", "permissionless blockchain", "private key", "public key",
                "qos (quality of service)", "quic (quick udp internet connections)", "radio access network", "redundancy",
                "relay node", "ripple", "roaming", "routing table",
                "satellite internet", "scalability", "secure multiparty computation", "self-healing network",
                "sharding", "sidechain", "smart grid", "softwarized network",
                "software-defined perimeter", "state channel", "stochastic network", "subnet",
                "swarm intelligence", "tcp congestion control", "tls handshake", "tokenization",
                "tor network", "traffic shaping", "transaction throughput", "trustless network",
                "udp (user datagram protocol)", "underlay network", "validator node", "vanet (vehicular ad hoc network)",
                "vlan", "vpn", "wan (wide area network)", "web3",
                "wireless mesh", "zero trust networking", "zk-snark (zero-knowledge succinct non-interactive argument of knowledge)"
        ));

        return categories;
    }

    private static String[] preprocessText(String text) {
        // Metni küçük harfe çevir
        text = text.toLowerCase();

        // Gereksiz karakterleri kaldır
        text = text.replaceAll("[^a-zA-Z0-9\\s]", " ");

        // Fazla boşlukları temizle
        text = text.replaceAll("\\s+", " ").trim();

        // Kelime dizisine çevir
        String[] words = text.split(" ");

        // Stopwords listesi
        Set<String> stopwords = new HashSet<>(Arrays.asList(
                "a", "an", "the", "and", "but", "or", "for", "nor", "on", "at", "to", "by", "in",
                "of", "with", "as", "is", "are", "was", "were", "be", "been", "being", "have", "has",
                "had", "do", "does", "did", "will", "would", "shall", "should", "may", "might",
                "must", "can", "could", "i", "you", "he", "she", "it", "we", "they", "this", "that"
        ));

        // Stopwords'leri filtrele
        return Arrays.stream(words)
                .filter(word -> !stopwords.contains(word) && word.length() > 1)
                .toArray(String[]::new);
    }

    private static Map<String, Double> calculateTFIDF(String[] words, Map<String, List<String>> categories) {
        Map<String, Double> scores = new HashMap<>();

        // Metindeki toplam kelime sayısı
        int totalWords = words.length;

        // Her kategori için
        for (Map.Entry<String, List<String>> category : categories.entrySet()) {
            String categoryName = category.getKey();
            List<String> keywords = category.getValue();

            double categoryScore = 0.0;

            // Her anahtar kelime için
            for (String keyword : keywords) {
                // TF: Kelimenin metinde geçme sıklığı
                int termFrequency = 0;
                if (keyword.contains(" ")) {
                    // Çok kelimeli anahtar kelimeler için
                    String documentText = String.join(" ", words);
                    int lastIndex = 0;
                    while (lastIndex != -1) {
                        lastIndex = documentText.indexOf(keyword, lastIndex);
                        if (lastIndex != -1) {
                            termFrequency++;
                            lastIndex += keyword.length();
                        }
                    }
                } else {
                    // Tek kelimeli anahtar kelimeler için
                    for (String word : words) {
                        if (word.equals(keyword)) {
                            termFrequency++;
                        }
                    }
                }

                // TF hesaplama
                double tf = (double) termFrequency / totalWords;

                // IDF hesaplama (basitleştirilmiş)
                // Kelimenin nadir geçmesi durumunda daha yüksek değer atar
                double idf = Math.log((double) categories.size() /
                        (1 + countCategoriesContainingKeyword(categories, keyword)));

                // TF-IDF puanı
                double tfidf = tf * idf;

                // Kategori puanına ekle
                categoryScore += tfidf;
            }

            scores.put(categoryName, categoryScore);
        }

        return scores;
    }

    private static int countCategoriesContainingKeyword(Map<String, List<String>> categories, String keyword) {
        int count = 0;
        for (List<String> keywords : categories.values()) {
            if (keywords.contains(keyword)) {
                count++;
            }
        }
        return count;
    }

    private static String findDominantCategoryByTFIDF(Map<String, Double> scores) {
        return scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Sınıflandırılamadı");
    }

    public static void nlpBasedClassification() throws IOException {
        // PDF'i yükle
        File file = new File("C:\\Users\\berkc\\Downloads\\örnek_makale1.pdf");
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

        // En yüksek puanlı kategoriyi bul
        String dominantCategory = findDominantCategoryByTFIDF(categoryScores);
        System.out.println("Makalenin muhtemel alanı (NLP): " + dominantCategory);
        System.out.println("NLP puanları: " + categoryScores);
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