FROM kalilinux/kali-rolling

RUN apt update && apt install -y theharvester && \
    apt clean && rm -rf /var/lib/apt/lists/*

ENTRYPOINT ["theHarvester"]
