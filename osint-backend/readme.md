# PTBOX OSINT Web Application
Small OSINT web app built with **Kotlin (Spring Boot)** on backend and **React** on frontend.  
It allows running domain scans using **Amass** or **theHarvester**, wrapped inside Docker containers.

The scan results (subdomains, IPs, emails etc.) are stored in a local database and the UI receives real-time updates through WebSockets.

---

## How the backend works
When the user starts a scan from the UI:

1. The backend saves a new `Scan` record in the DB.
2. It launches a short-lived Docker container using:
    - `caffix/amass`
    - `zricethezav/theharvester`
3. The command is executed with  
   **`docker run --rm <image> <args>`**,  
   meaning the container runs once and is deleted immediately after finishing.
4. The backend reads the tool output, parses it, stores the results, and pushes live updates to the frontend through WebSockets.

**Important:**  
Because of `--rm`, the containers finish very fast and DO NOT appear in Docker Desktop UI.  
This is expected and intentional to keep the environment clean.

---

## Requirements
Before running the app, install:

- **Docker Desktop** (mandatory, backend uses `docker run`)
- **Java 17**

