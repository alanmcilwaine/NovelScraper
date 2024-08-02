package com.alanjam.scraper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

/**
 * Tool to scrape a webpage.
 */
public class Scraper {
    private Document doc;
    public List<StringBuilder> chapters = new ArrayList<>();
    public Set<String> pagesDiscovered = new HashSet<>();
    public Stack<String> pagesToScrape = new Stack<>();

    /**
     * Add starting url to the pages to scrape list.
     * @param url Starting URL
     */
    public Scraper(String url){
        assert url != null && !url.isEmpty();
        pagesToScrape.add(url);
    }

    /**
     * Connect to a website.
     * @param url URL trying to connect to
     * @throws IOException Throws when cannot connect to URL
     */
    public void connectToSite(String url) throws IOException{
        doc = Jsoup.connect(url).userAgent("Mozilla").get();
    }

    /**
     * Fundamental method to scrape website.
     */
    public void scrape(){
        int maxChapters = 1000;
        while(!pagesToScrape.isEmpty() && maxChapters > 0){
            try{
                connectToSite(pagesToScrape.pop());
                System.out.println("Chapter: " + (chapters.size() + 1) + " ");
            } catch(IOException e){
                System.out.println("Failed to scrape " + e);
                return;
            }

            Element chapter = doc.selectFirst("h1.font-bold");
            Element chapterTitle = doc.selectFirst("div.font-medium.text-sm");
            Elements sentences = doc.select("div#chapter-body");
            assert chapter != null;
            assert chapterTitle != null;

            StringBuilder out = new StringBuilder();

            out.append("<center><h1> " + chapter.text() + "</h1></center>\n" );
            out.append("<center><h3> " + chapterTitle.text() + "</h3></center>\n\n\n\n");
            for (Element paragraph : sentences) {
                for (Element sentence : paragraph.children()) {
                    if (sentence.text().contains("\uFE0F")) {
                        continue;
                    }
                    out.append(sentence.text()).append("\n\n");
                }
            }

            chapters.add(out);
            findNextLink();
            maxChapters--;
        }
        System.out.println("Finished!\n\n\n");
    }

    /**
     * Searches the website for the next chapter page
     */
    public void findNextLink(){
        Elements links = doc.select("a[href]");
        for (Element link : links) {
            String url = link.attr("href");
            if (link.text().equals("Next Episode") && !pagesDiscovered.contains(url) && !pagesToScrape.contains(url)){
                pagesToScrape.add(link.attr("href"));
                break;
            }
            pagesDiscovered.add(url);
        }
    }

    /**
     * Converts the StringBuilder of Chapters to a markdown file. This can then be
     * converted into an epub online.
     */
    public void convertToMd(){
        try{
            FileWriter file = new FileWriter("out.md");
            for (StringBuilder chapter : chapters){
                file.write(chapter.toString());
            }
            file.close();
            System.out.println("Finished writing to file.");
        } catch (IOException e) {
            System.out.println("Failed to write file " + e);
        }

    }

    public static void main(String[] args) {
        String url = "";
        Scraper scraper = new Scraper(url);
        scraper.scrape();
        scraper.convertToMd();
    }
}