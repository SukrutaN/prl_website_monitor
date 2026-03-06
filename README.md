# Site Monitor

A lightweight Java tool that watches a specific piece of text on a webpage and emails you the moment it changes.

Built as a personal notifier for the [PRL Summer Internship page](https://www.prl.res.in/prl-eng/opportunities/summer_internship_program), but can be adapted to monitor any webpage.

---

## How It Works

1. Fetches the target webpage using **Jsoup**
2. Extracts a specific HTML element (the Duration field)
3. Compares it against the last known value saved in `last_known_text.txt`
4. If it changed — sends an email alert via **Jakarta Mail** and updates the saved value
5. Scheduled to run automatically via **Windows Task Scheduler**

---

## Prerequisites

- Java 17+
- Maven
- A Gmail account with [2-Step Verification](https://myaccount.google.com/security) enabled

---

## Setup

**1. Clone or download the project**

**2. Fill in your details in `Monitor.java`:**
```java
static final String SENDER_EMAIL    = "your_email@gmail.com";
static final String SENDER_PASSWORD = "your_app_password_here";
static final String RECEIVER_EMAIL  = "your_email@gmail.com";
```

**3. Generate a Gmail App Password**
- Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords)
- Create one named anything (e.g. "site monitor")
- Paste the 16-character password into `SENDER_PASSWORD`

**4. Build the project**
```bash
cd my-app
mvn package
```

**5. Run it once to create the baseline**
```bash
java -jar target/site-monitor-1.0-jar-with-dependencies.jar
```
You should see:
```
📝 First run. Saved baseline text: → Duration: To be Announced for 2026.
```

---

## Scheduling (Windows Task Scheduler)

1. Open **Task Scheduler** → Create Basic Task
2. Set trigger: **Daily**, repeat every **30 minutes**
3. Set action → Start a program:
   - **Program:** `java`
   - **Arguments:** `-jar D:\Projects\site_monitor\my-app\target\site-monitor-1.0-jar-with-dependencies.jar`
   - **Start in:** `D:\Projects\site_monitor\my-app`

---

## Project Structure

```
site_monitor/
└── my-app/
    ├── pom.xml
    ├── last_known_text.txt        ← auto-created on first run
    └── src/main/java/com/sukruta/app/
        └── Monitor.java
```

---

## Dependencies

| Library | Purpose |
|---|---|
| [Jsoup](https://jsoup.org/) | Fetches and parses the webpage |
| [Jakarta Mail](https://eclipse-ee4j.github.io/mail/) | Sends email alerts via SMTP |
