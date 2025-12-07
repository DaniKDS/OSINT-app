import { useEffect, useState } from "react";
import { connectToScan } from "./ws";
import "./App.css";

type ScanStatus = "PENDING" | "RUNNING" | "COMPLETED" | "FAILED";
type ScanTool = "AMASS" | "THE_HARVESTER";

interface ScanDto {
    id: string;
    domain: string;
    tool: ScanTool;
    status: ScanStatus;
    startTime: string;
    endTime?: string | null;
    progress: number;
}

interface ScanResultDto {
    subdomains: string[];
    ips: string[];
    emails: string[];
    linkedInProfiles: string[];
}

interface ScanWithResultDto {
    scan: ScanDto;
    result?: ScanResultDto | null;
}

const API_BASE = "http://localhost:8082/api";

interface ModalProps {
    title: string;
    onClose: () => void;
    children: React.ReactNode;
}

function Modal({ title, onClose, children }: ModalProps) {
    return (
        <div className="modal-backdrop">
            <div className="modal">
                <div className="modal-header">
                    <h2>{title}</h2>
                    <button className="btn-secondary" onClick={onClose}>
                        ✕
                    </button>
                </div>
                <div className="modal-body">{children}</div>
            </div>
        </div>
    );
}

//main app
function App() {
    const [scans, setScans] = useState<ScanDto[]>([]);

    // state pentru formularul de "new scan"
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [domain, setDomain] = useState("");
    const [tool, setTool] = useState<ScanTool>("AMASS");
    const [isCreating, setIsCreating] = useState(false);

    const [selectedScan, setSelectedScan] = useState<ScanWithResultDto | null>(
        null
    );
    const [isLoadingDetails, setIsLoadingDetails] = useState(false);

    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        loadScans();
    }, []);

    async function loadScans() {
        try {
            setError(null);
            const res = await fetch(`${API_BASE}/scans`);
            if (!res.ok) {
                throw new Error("Failed to load scans");
            }
            const data: ScanDto[] = await res.json();
            setScans(data);
        } catch (e: any) {
            console.error(e);
            setError(e.message ?? "Unknown error while loading scans");
        }
    }

    async function handleCreateScan(e: React.FormEvent) {
        e.preventDefault();
        if (!domain.trim()) {
            setError("Please enter a domain");
            return;
        }

        try {
            setIsCreating(true);
            setError(null);

            const res = await fetch(`${API_BASE}/scans`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ domain, tool }),
            });

            if (!res.ok) {
                throw new Error("Failed to create scan");
            }

            const created: ScanDto = await res.json();

            connectToScan(created.id, (update) => {
                setScans((prev) =>
                    prev.map((s) => (s.id === update.id ? { ...s, ...update } : s))
                );
            });

            setScans((prev) => [created, ...prev]);

            setDomain("");
            setTool("AMASS");
            setIsCreateOpen(false);

        } catch (e: any) {
            console.error(e);
            setError(e.message ?? "Unknown error while creating scan");
        } finally {
            setIsCreating(false);
        }
    }

    async function openScanDetails(scanId: string) {
        try {
            setIsLoadingDetails(true);
            setError(null);

            const res = await fetch(`${API_BASE}/scans/${scanId}`);
            if (!res.ok) {
                throw new Error("Failed to load scan details");
            }

            const data: ScanWithResultDto = await res.json();
            setSelectedScan(data);
        } catch (e: any) {
            console.error(e);
            setError(e.message ?? "Unknown error while loading scan details");
        } finally {
            setIsLoadingDetails(false);
        }
    }

    function formatDate(value?: string | null) {
        if (!value) return "-";
        return new Date(value).toLocaleString();
    }

    return (
        <div className="app">
            <header className="app-header">
                <div>
                    <h1>PTBOX OSINT scans</h1>
                    <p className="subtitle">
                        Run Amass / theHarvester against a domain and see live progress.
                    </p>
                </div>
                <button
                    className="btn-primary"
                    onClick={() => setIsCreateOpen(true)}
                >
                    + New scan
                </button>
            </header>

            {error && <div className="alert-error">{error}</div>}

            {/* cards list */}
            <div className="scan-grid">
                {scans.length === 0 && (
                    <div className="empty">No scans yet. Start one from the button above.</div>
                )}

                {scans.map((scan) => (
                    <div key={scan.id} className="scan-card">
                        <div className="scan-header">
                            <h3>{scan.domain}</h3>
                            <span className="badge">{scan.tool}</span>
                        </div>

                        <div className="scan-row">
                            <span>Status:</span>
                            <span className={`status status-${scan.status.toLowerCase()}`}>
                {scan.status}
              </span>
                        </div>

                        <div className="scan-row">
                            <span>Start:</span>
                            <span>{formatDate(scan.startTime)}</span>
                        </div>

                        <div className="scan-row">
                            <span>End:</span>
                            <span>{formatDate(scan.endTime)}</span>
                        </div>

                        {/* progress bar */}
                        <div className="progress-wrapper">
                            <div className="progress-bar">
                                <div
                                    className="progress-fill"
                                    style={{ width: `${scan.progress}%` }}
                                />
                            </div>
                            <span className="progress-label">{scan.progress}%</span>
                        </div>

                        <div className="scan-footer">
                            <button
                                className="btn-secondary"
                                onClick={() => openScanDetails(scan.id)}
                            >
                                View results
                            </button>
                        </div>
                    </div>
                ))}
            </div>

            {/* modal for creating scan */}
            {isCreateOpen && (
                <Modal title="New scan" onClose={() => setIsCreateOpen(false)}>
                    <form className="form" onSubmit={handleCreateScan}>
                        <label className="form-label">
                            Domain
                            <input
                                type="text"
                                value={domain}
                                onChange={(e) => setDomain(e.target.value)}
                                placeholder="ex: google.com"
                            />
                        </label>

                        <label className="form-label">
                            Tool
                            <select
                                value={tool}
                                onChange={(e) => setTool(e.target.value as ScanTool)}
                            >
                                <option value="AMASS">Amass</option>
                                <option value="THE_HARVESTER">theHarvester</option>
                            </select>
                        </label>

                        <div className="form-actions">
                            <button
                                type="button"
                                className="btn-secondary"
                                onClick={() => setIsCreateOpen(false)}
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                className="btn-primary"
                                disabled={isCreating}
                            >
                                {isCreating ? "Starting..." : "Start scan"}
                            </button>
                        </div>
                    </form>
                </Modal>
            )}

            {/* results modal*/}
            {selectedScan && (
                <Modal
                    title={`Results for ${selectedScan.scan.domain}`}
                    onClose={() => setSelectedScan(null)}
                >
                    {isLoadingDetails ? (
                        <p>Loading...</p>
                    ) : selectedScan.result ? (
                        <div className="results">
                            <h4>Subdomains</h4>
                            {selectedScan.result.subdomains.length ? (
                                <ul>
                                    {selectedScan.result.subdomains.map((s) => (
                                        <li key={s}>{s}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No subdomains found.</p>
                            )}

                            <h4>IPs</h4>
                            {selectedScan.result.ips.length ? (
                                <ul>
                                    {selectedScan.result.ips.map((ip) => (
                                        <li key={ip}>{ip}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No IPs found.</p>
                            )}

                            <h4>Emails</h4>
                            {selectedScan.result.emails.length ? (
                                <ul>
                                    {selectedScan.result.emails.map((e) => (
                                        <li key={e}>{e}</li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No emails found.</p>
                            )}

                            <h4>LinkedIn profiles</h4>
                            {selectedScan.result.linkedInProfiles.length ? (
                                <ul>
                                    {selectedScan.result.linkedInProfiles.map((l) => (
                                        <li key={l}>
                                            <a href={l} target="_blank" rel="noreferrer">
                                                {l}
                                            </a>
                                        </li>
                                    ))}
                                </ul>
                            ) : (
                                <p>No LinkedIn profiles found.</p>
                            )}
                        </div>
                    ) : (
                        <p>No results stored for this scan yet.</p>
                    )}
                </Modal>
            )}
        </div>
    );
}

export default App;
