import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

const WS_BASE = "http://localhost:8082/ws";

export function connectToScan(
    scanId: string,
    onUpdate: (msg: any) => void
) {
    const socket = new SockJS(WS_BASE);

    const client = new Client({
        webSocketFactory: () => socket as any,
        reconnectDelay: 5000,
        debug: () => {},
    });

    client.onConnect = () => {
        client.subscribe(`/topic/scans/${scanId}`, (frame) => {
            const body = JSON.parse(frame.body);
            onUpdate(body);
        });
    };

    client.activate();
    return client;
}
