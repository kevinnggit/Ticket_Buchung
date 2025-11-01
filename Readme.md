# Ticket_Buchung - Impftermin-Buchungssystem

Ein webbasiertes Buchungssystem für Impftermine in verschiedenen Impfzentren.

## Projektbeschreibung

Diese Anwendung ermöglicht es Benutzern, Impftermine in verschiedenen Impfzentren zu buchen. Das System bietet:
- Benutzerregistrierung und -anmeldung mit sicherer Passwortverschlüsselung (PBKDF2)
- Auswahl von Impfzentren und verfügbaren Zeitslots
- Buchung von bis zu 4 Terminen pro Benutzer
- Verwaltung gebuchter Termine mit Löschfunktion
- E-Mail-Benachrichtigungen bei Terminbuchung
- Admin-Bereich für Systemverwaltung

## Technologiestack

- **Backend**: Java mit Jakarta Servlets
- **Frontend**: HTML, CSS, JavaScript
- **Datenbank**: MariaDB/MySQL
- **Build-System**: Shell-Skripte
- **Zusätzliche Bibliotheken**: 
  - Jakarta Servlet API
  - ZXing (für QR-Code-Generierung)
  - JDBC für Datenbankzugriff

## Verfügbare Impfzentren

1. **CHU** - 1 Kabine, 1 Impfstoffoption
2. **Clinique Bertoua** - 2 Kabinen, 2 Impfstoffoptionen
3. **Clinique de Dschang** - 3 Kabinen, 3 Impfstoffoptionen
4. **Hopital central** - 4 Kabinen, 4 Impfstoffoptionen

## Installation und Einrichtung

### Voraussetzungen

- Java Development Kit (JDK) 8 oder höher
- MariaDB/MySQL Datenbankserver
- Apache Tomcat oder ähnlicher Servlet-Container

### Schritt 1: Konfiguration

Führen Sie eines der folgenden Skripte aus, um die Konfigurationsdaten zu erstellen:

```bash
bin/configure.sh
```
Um Konfigurationsdaten von Hopper auf local zu holen.

Oder:

```bash
bin/configure-work.sh
```
Um eine Konfiguration für das lokale Netzwerk in einer Docker-Umgebung zu erstellen.

### Schritt 2: Bibliotheken herunterladen

```bash
bin/download-libs.sh
```
Lädt alle notwendigen Java-Bibliotheken herunter.

### Schritt 3: Build-Prozess

```bash
bin/build.sh
```

Dieser Befehl führt folgende Schritte aus:
- **prepare**: Vorbereitung der Build-Umgebung
- **compile**: Kompilierung der Java-Quellcode
- **assemble**: Zusammenstellung der Anwendung
- **deploy**: Deployment der Anwendung
- **check**: Überprüfung der Anwendung

## Build-Management

### Projekt bereinigen

```bash
bin/clean.sh
```
Löscht die Verzeichnisse `build` und `target`.

```bash
bin/clean-all.sh
```
Löscht `build`, `target`, `lib` und `app/WEB-INF/lib`.

### Weitere Build-Befehle

- `bin/prepare.sh` - Vorbereitung
- `bin/compile.sh` - Nur Kompilierung
- `bin/assemble.sh` - Nur Assemblierung
- `bin/deploy.sh` - Nur Deployment
- `bin/check.sh` - Nur Überprüfung

## Datenbankstruktur

Die Anwendung verwendet folgende Haupttabellen:

- **users**: Benutzerdaten (id, name, vorname, alter, email, passwort, benutzername, adresse, postleitzahl, stadt)
- **vaccination_center**: Informationen zu Impfzentren (center_id, name, cabins)
- **appointmentsApp**: Gebuchte Termine
- **time_slot**: Verfügbare Zeitslots
- **center_time_status**: Status der gebuchten Slots pro Zentrum
- **center_vaccine**: Verfügbare Impfstoffe pro Zentrum

## Funktionalitäten

### Für Benutzer:
- Registrierung mit vollständigen persönlichen Daten
- Anmeldung mit Benutzername und Passwort
- Auswahl eines Impfzentrums
- Buchung von Terminen mit Datum, Uhrzeit und Impfstoff
- Anzeige aller gebuchten Termine
- Stornierung von Terminen
- E-Mail-Bestätigung nach Buchung

### Für Administratoren:
- Admin-Login (Benutzername: `Admin`, Passwort: `Adminpwd123`)
- Dashboard-Zugriff
- Benutzerverwaltung
- Systemstatus-Überwachung

## Sicherheitsmerkmale

- Passwörter werden mit PBKDF2 (210.000 Iterationen, HMAC-SHA512) gehasht
- Zufällige Salt-Generierung für jeden Benutzer
- Session-basierte Authentifizierung
- Schutz vor SQL-Injection durch PreparedStatements

## Entwicklung

### Code-Struktur

```
src/hbv/web/
├── BuchungServlet.java      - Terminbuchungslogik
├── RegisterServlet.java     - Benutzerregistrierung
├── LoginServlet.java        - Benutzeranmeldung
├── LogoutServlet.java       - Abmeldelogik
├── TerminA.java             - Anzeige der Termine
├── DeleteT.java             - Löschen von Terminen
├── DatabaseConnection.java  - Datenbankverbindung
└── SendMail.java            - E-Mail-Versand

app/
├── login.html              - Login-Seite
├── register.html           - Registrierungsseite
├── willkommen.html         - Startseite nach Login
├── zentren.html            - Zentrumsauswahl und Buchungsformular
├── termine.html            - Übersicht gebuchter Termine
├── admin.html              - Admin-Dashboard
├── script.css              - Stylesheet
└── script.js               - JavaScript-Funktionen
```

### QR-Code-Generierung

Die Anwendung unterstützt QR-Code-Generierung mit der ZXing-Bibliothek:

```java
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
```

## Lizenz

Dieses Projekt ist für akademische und Bildungszwecke entwickelt worden.
