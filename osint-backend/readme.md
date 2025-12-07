# PTBOX OSINT Web Application
Small OSINT web app built with **Kotlin (Spring Boot)** on backend and **React** on frontend.  
It allows running domain scans using **Amass** or **theHarvester**, wrapped inside Docker containers.

The scan results (subdomains, IPs, emails etc.) are stored in a local database and the UI receives real-time updates through WebSockets.

