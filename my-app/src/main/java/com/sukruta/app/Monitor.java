package com.sukruta.app;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import jakarta.mail.*;
import jakarta.mail.internet.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.Properties;

public class Monitor {

    // ─────────────────────────────────────────────
    // CONFIGURATION — fill these in before running
    // ─────────────────────────────────────────────

    static final String URL = "https://www.prl.res.in/prl-eng/opportunities/summer_internship_program";

    // Local file that stores the last seen text between runs
    static final String STORAGE_FILE = "last_known_text.txt";

    // ── Email settings ──
    static final String SENDER_EMAIL    = "sukrutnad1@gmail.com";
    static final String SENDER_PASSWORD = "naqz zhiv ubvw iznr"; // Gmail App Password, NOT your real password
                                                                     // Generate at: myaccount.google.com/apppasswords
    static final String RECEIVER_EMAIL  = "sukrutnad1@gmail.com";   // can be the same address
    static final String SMTP_HOST       = "smtp.gmail.com";
    static final String SMTP_PORT       = "587";


    // ─────────────────────────────────────────────
    // STEP 1 — Fetch the target text from the page
    // ─────────────────────────────────────────────

    static String getCurrentText() {
        try {
            // Jsoup fetches the page and parses the HTML in one call.
            // userAgent() mimics a real browser so the server doesn't block us.
            Document doc = Jsoup.connect(URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000) // 10 second timeout
                    .get();

            // Select all <strong> elements inside <p> tags,
            // then find the one that mentions "Duration"
            for (Element el : doc.select("p strong")) {
                if (el.text().contains("Duration")) {
                    return el.text().trim();
                }
            }

            System.out.println("⚠️  Target element not found. The page layout may have changed.");
            return null;

        } catch (IOException e) {
            System.out.println("❌ Error fetching the page: " + e.getMessage());
            return null;
        }
    }


    // ─────────────────────────────────────────────
    // STEP 2 — Load & save the stored text
    // ─────────────────────────────────────────────

    static String loadSavedText() {
        Path path = Path.of(STORAGE_FILE);
        if (!Files.exists(path)) {
            return null; // first run — no baseline yet
        }
        try {
            return Files.readString(path).trim();
        } catch (IOException e) {
            System.out.println("❌ Could not read storage file: " + e.getMessage());
            return null;
        }
    }

    static void saveText(String text) {
        try {
            Files.writeString(Path.of(STORAGE_FILE), text);
        } catch (IOException e) {
            System.out.println("❌ Could not write storage file: " + e.getMessage());
        }
    }


    // ─────────────────────────────────────────────
    // STEP 3 — Send an email alert
    // ─────────────────────────────────────────────

    static void sendEmail(String oldText, String newText) {
        // Configure the SMTP connection properties
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // encrypts the connection
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Authenticator supplies the login credentials when Jakarta Mail connects
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(RECEIVER_EMAIL));
            message.setSubject("🔔 PRL Internship Page Changed!");
            message.setText(
                "The monitored text on the PRL internship page has changed.\n\n" +
                "OLD:  " + oldText + "\n" +
                "NEW:  " + newText + "\n\n" +
                "Visit the page: " + URL
            );

            Transport.send(message);
            System.out.println("✅ Email alert sent successfully!");

        } catch (MessagingException e) {
            System.out.println("❌ Failed to send email: " + e.getMessage());
        }
    }


    // ─────────────────────────────────────────────
    // STEP 4 — Main logic: compare and act
    // ─────────────────────────────────────────────

    public static void main(String[] args) {
        System.out.println("🔍 Checking: " + URL);

        String currentText = getCurrentText();
        if (currentText == null) return; // something went wrong; skip this run

        String savedText = loadSavedText();

        if (savedText == null) {
            // Very first run — save the baseline and exit without alerting
            saveText(currentText);
            System.out.println("📝 First run. Saved baseline text:\n   → " + currentText);
            return;
        }

        if (!currentText.equals(savedText)) {
            // The text changed — alert and update the stored value
            System.out.println("🚨 Change detected!\n   WAS: " + savedText + "\n   NOW: " + currentText);
            sendEmail(savedText, currentText);
            saveText(currentText); // update baseline so we don't re-alert for the same change
        } else {
            System.out.println("✅ No change detected.\n   Text is still: " + currentText);
        }
    }
}