package com.boombet.realtime_service.parser;

import com.boombet.realtime_service.dto.MarketDTO;
import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.dto.OutcomeDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class SportsDataParser {

    private static final Logger log = LoggerFactory.getLogger(SportsDataParser.class);
    private static final String FLASHSCORE_URL = "https://www.flashscore.com.ua/";

    public List<MatchUpdateDTO> fetchLiveMatches() {
        List<MatchUpdateDTO> matches = new ArrayList<>();
        log.info("Attempting to fetch data from {} using Selenium with Firefox...", FLASHSCORE_URL);

        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER);
        options.addArguments("--headless");
        
        FirefoxProfile profile = new FirefoxProfile();
        profile.setPreference("dom.webdriver.enabled", false);
        profile.setPreference("useAutomationExtension", false);
        options.setProfile(profile);
        options.addPreference("general.useragent.override", "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/115.0");

        WebDriver driver = null;
        try {
            driver = new FirefoxDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.get(FLASHSCORE_URL);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            
            handleCookieBanner(driver);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.className("event__match")));
            
            List<String> matchIds = driver.findElements(By.cssSelector("div.event__match"))
                                          .stream()
                                          .map(e -> e.getAttribute("id"))
                                          .filter(id -> id != null && !id.isEmpty() && id.startsWith("g_1_"))
                                          .toList();

            log.info("Found {} matches on the main page. Parsing each one...", matchIds.size());
            
            for (String matchId : matchIds) {
                try {
                    String matchUrl = String.format("https://www.flashscore.com.ua/match/%s/#/match-summary", matchId.substring(4));
                    driver.get(matchUrl);
                    
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.className("duelParticipant")));
                    Document matchDoc = Jsoup.parse(driver.getPageSource());

                    String homeTeam = getTextOrEmpty(matchDoc, ".duelParticipant__home .participant__participantName");
                    String awayTeam = getTextOrEmpty(matchDoc, ".duelParticipant__away .participant__participantName");
                    String status = getTextOrEmpty(matchDoc, ".eventTime");
                    if (status.isEmpty()) {
                        status = getTextOrEmpty(matchDoc, ".duelParticipant__startTime div");
                    }
                    if (status.isEmpty()) {
                         status = "Finished";
                    }
                    String homeScore = getTextOrEmpty(matchDoc, ".detailScore__wrapper > span:nth-child(1)");
                    String awayScore = getTextOrEmpty(matchDoc, ".detailScore__wrapper > span:nth-child(3)");

                    List<MarketDTO> markets = new ArrayList<>();
                    try {
                        WebElement oddsTab = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a[data-analytics-alias='odds-comparison']")));
                        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", oddsTab);
                        
                        markets = parseOdds(driver);
                    } catch (Exception e) {
                        log.warn("Could not find or click odds tab for match {}. Skipping odds. Reason: {}", matchId, e.getMessage());
                    }

                    MatchUpdateDTO dto = new MatchUpdateDTO(matchId, status, homeTeam, awayTeam, homeScore, awayScore, markets);
                    matches.add(dto);
                    log.info("Successfully processed match: {}", dto);

                } catch (Exception e) {
                    log.error("Failed to process individual match with id {}: {}", matchId, e.getMessage());
                    if (driver != null) {
                        try {
                            String errorPageSource = driver.getPageSource();
                            String fileName = "error_page_" + matchId + ".html";
                            Files.writeString(Paths.get(fileName), errorPageSource);
                            log.info("Saved problematic HTML to {}", fileName);
                        } catch (Exception fileException) {
                            log.error("Could not save error page HTML", fileException);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("A critical error occurred during the main parsing loop: {}", e.getMessage(), e);
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        return matches;
    }

    private void handleCookieBanner(WebDriver driver) {
        try {
            WebDriverWait cookieWait = new WebDriverWait(driver, Duration.ofSeconds(3));
            WebElement acceptButton = cookieWait.until(ExpectedConditions.elementToBeClickable(By.id("onetrust-accept-btn-handler")));
            log.info("Cookie banner found. Clicking 'Accept All'.");
            acceptButton.click();
            Thread.sleep(1000);
        } catch (Exception e) {
            log.info("Cookie banner not found or already closed.");
        }
    }

    private String getTextOrEmpty(Element parent, String selector) {
        if (parent == null) return "";
        Element element = parent.selectFirst(selector);
        return (element != null) ? element.text().trim() : "";
    }

    private List<MarketDTO> parseOdds(WebDriver driver) {
        List<MarketDTO> markets = new ArrayList<>();
        try {
            WebDriverWait oddsWait = new WebDriverWait(driver, Duration.ofSeconds(5));
            oddsWait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".oddsTab__tableWrapper .ui-table__row")));

            Document matchDoc = Jsoup.parse(driver.getPageSource());

            Elements bookmakerRows = matchDoc.select(".ui-table__row");
            if (!bookmakerRows.isEmpty()) {
                Element firstBookmakerRow = bookmakerRows.first();
                
                List<OutcomeDTO> outcomes1x2 = new ArrayList<>();
                String odd1 = getTextOrEmpty(firstBookmakerRow, "a[data-analytics-element=ODDS_COMPARIONS_ODD_CELL_1] span");
                String oddX = getTextOrEmpty(firstBookmakerRow, "a[data-analytics-element=ODDS_COMPARIONS_ODD_CELL_2] span");
                String odd2 = getTextOrEmpty(firstBookmakerRow, "a[data-analytics-element=ODDS_COMPARIONS_ODD_CELL_3] span");
                
                if (odd1 != null && !odd1.isEmpty() && oddX != null && !oddX.isEmpty() && odd2 != null && !odd2.isEmpty()) {
                    outcomes1x2.add(new OutcomeDTO("1", new BigDecimal(odd1)));
                    outcomes1x2.add(new OutcomeDTO("X", new BigDecimal(oddX)));
                    outcomes1x2.add(new OutcomeDTO("2", new BigDecimal(odd2)));
                    markets.add(new MarketDTO("Исход матча", outcomes1x2));
                }
            }
        } catch (Exception e) {
            log.warn("Could not find or parse odds. Skipping odds parsing. Reason: {}", e.getMessage());
        }
        return markets;
    }
}
