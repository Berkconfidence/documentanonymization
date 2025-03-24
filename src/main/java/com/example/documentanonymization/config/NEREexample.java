package com.example.documentanonymization.config;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.CoreDocument;
import edu.stanford.nlp.pipeline.CoreEntityMention;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;

import java.util.List;
import java.util.Properties;

public class NEREexample {

    public static void main(String[] args) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        CoreDocument doc = new CoreDocument("Hey! This is John Doe. I work at Google. I live in America. I have a dog.");
        pipeline.annotate(doc);

        for (CoreEntityMention em : doc.entityMentions()) {
            System.out.println("Entity: " + em.text() + " (" + em.entityType() + ")");
        }



    }
}
