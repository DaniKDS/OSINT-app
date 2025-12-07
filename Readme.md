# OSINT Web Application (PTBOX Challenge)

This project is an OSINT (Open-Source Intelligence) web application built as part of the **vPTBOX Challenge**.

The application allows users to run reconnaissance scans on domains using open-source tools such as **theHarvester** and **Amass**, aggregating information like:

- Subdomains
- IP addresses
- Email addresses
- Possible LinkedIn accounts
- Other public OSINT data

The system consists of:

- A **Kotlin backend** (server-side API + WebSocket updates)
- A **React + TypeScript frontend** (interactive web UI)
- **Dockerized services** for easy deployment and isolation of OSINT tools

---

## Core Capabilities

### 1. OSINT Scans

Users can:

- Input a **domain name** (e.g. `google.com`)
- Choose a **tool**: `theHarvester` or `Amass`
- Optionally provide additional tool-specific parameters (e.g. data sources, depth, etc.)
- Start a scan from the UI via a **“Scan”** button / modal

Each scan produces a **scan card** in the UI with at least:

- Domain
- Tool used
- Start time
- End time (when finished)
- Status (running / completed / failed)

If results are available, the card lets the user open a **details modal** showing:

- Discovered subdomains
- Additional OSINT information (emails, IPs, etc.)
- A clear indication when **no results** were found

---

## Backend Responsibilities (Kotlin)

The backend:

- Exposes REST endpoints to:
    - **Start a new scan** (domain + selected tool)
    - **Fetch past scans** (history)
- Spawns Docker containers to run:
    - **theHarvester**
    - **Amass**
- Collects and parses tool output for storage and display
- Persists scan data so that **history survives server restarts**
- Sends **live updates** to clients through:
    - WebSockets (e.g. scan started, scan progress, scan completed, scan failed)
---

## Frontend Responsibilities (React + TypeScript)

The frontend:

- Provides a responsive UI where users can:
    - Enter domains
    - Select tools
    - Trigger scans
- Shows **scan cards** in a grid / list with:
    - Domain, start & end time, tool, status
    - Actions to view detailed scan results in a modal
- Integrates with **WebSockets (or similar)** to:
    - Update scan status in real time
    - Optionally show alerts / notifications of updates

## Frontend Screenshots

### Scan Dashboard
![Scan Dashboard](<img width="654" height="629" alt="image" src="https://github.com/user-attachments/assets/8f01d304-495f-4282-b4f3-13e7d12de7f6" />
)

### Scan Results Modal
![Scan Results](<img width="753" height="494" alt="image" src="https://github.com/user-attachments/assets/298e8abc-bcf3-4e29-a643-df8df2e89d57" />
)


---

##  Containerization & Deployment

Both the **backend** and **frontend** are **Dockerized** to simplify:

- Local development
- CI/CD pipelines
- Production deployment

The project can be started locally by building and running the Docker images (or via `docker-compose`) so that a new user can:

1. Pull the images
2. Run the containers
3. Access the web UI in a browser
4. Start OSINT scans without extra manual setup


### Docker Run Instructions (Backend + Frontend)

You can run the application either by building each image manually or by using `docker-compose`.

##  Option 1: Run with Docker Compose (recommended)

Once started:

Backend runs on: http://localhost:8082

Frontend runs on: http://localhost:5173

From the project root:

```bash
docker-compose up --build


