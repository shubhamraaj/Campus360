import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { MapContainer, TileLayer, Polygon, Marker, Popup, Polyline, useMapEvents } from 'react-leaflet';
import L from 'leaflet';
import 'leaflet/dist/leaflet.css'; // <-- Required for Leaflet map tiling in React 19/Vite
import { ShieldCheck, MapPin, Camera, History, Navigation, CheckCircle2, XCircle } from 'lucide-react';

const API_BASE = 'http://localhost:8080/api/v1';

// Fix missing Leaflet marker icons under Vite asset loader
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon-2x.png',
  iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
});

// Hardcoded coordinates for campus building polygon boundaries (Main Library & Science Block)
const CAMPUS_BUILDINGS = [
  {
    id: '430a5431-d0d4-4911-bef9-ddb7d8d7940d',
    name: 'Main Library',
    code: 'LIB-01',
    center: [19.105, 72.855],
    polygon: [
      [19.1055, 72.8545],
      [19.1055, 72.8555],
      [19.1045, 72.8555],
      [19.1045, 72.8545]
    ]
  },
  {
    id: '550b5431-d0d4-4911-bef9-ddb7d8d7940e',
    name: 'Science Block',
    code: 'SCI-02',
    center: [19.108, 72.859],
    polygon: [
      [19.1085, 72.8585],
      [19.1085, 72.8595],
      [19.1075, 72.8595],
      [19.1075, 72.8585]
    ]
  }
];

// Helper to capture map clicks for GPS relocation
function LocationMarker({ position, setLocation }) {
  useMapEvents({
    click(e) {
      setLocation({ lat: e.latlng.lat, lon: e.latlng.lng });
    },
  });

  return position ? (
    <Marker position={[position.lat, position.lon]}>
      <Popup>Student Verification Location</Popup>
    </Marker>
  ) : null;
}

export default function App() {
  const [activeTab, setActiveTab] = useState('attendance');
  const [studentEmail, setStudentEmail] = useState('docker_student@campus360.edu');
  const [selectedBuilding, setSelectedBuilding] = useState(CAMPUS_BUILDINGS[0].id);
  const [selectedFile, setSelectedFile] = useState(null);
  const [location, setLocation] = useState({ lat: 19.105, lon: 72.855 });
  const [verificationResult, setVerificationResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState([]);

  // Route Engine State
  const [startBuilding, setStartBuilding] = useState(CAMPUS_BUILDINGS[0].id);
  const [destBuilding, setDestBuilding] = useState(CAMPUS_BUILDINGS[1].id);
  const [routeInfo, setRouteInfo] = useState(null);
  const [routeLine, setRouteLine] = useState([]);

  const handleMarkAttendance = async (e) => {
    e.preventDefault();
    if (!selectedFile) {
      alert('Please select or upload a face photo first!');
      return;
    }

    setLoading(true);
    setVerificationResult(null);

    const formData = new FormData();
    formData.append('file', selectedFile);
    formData.append('studentEmail', studentEmail);
    formData.append('buildingId', selectedBuilding);
    formData.append('latitude', location.lat);
    formData.append('longitude', location.lon);

    try {
      const res = await axios.post(`${API_BASE}/attendance/verify-and-mark`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
      });
      setVerificationResult(res.data);
      fetchHistory();
    } catch (err) {
      alert('Attendance processing failed. Check backend connection.');
    } finally {
      setLoading(false);
    }
  };

  const fetchHistory = async () => {
    try {
      const res = await axios.get(`${API_BASE}/attendance/history?studentEmail=${studentEmail}`);
      setHistory(res.data);
    } catch (err) {
      console.error('Error fetching history:', err);
    }
  };

  const handleCalculateRoute = async () => {
    try {
      const res = await axios.post(`${API_BASE}/gis/route`, {
        startBuildingId: startBuilding,
        destinationBuildingId: destBuilding,
      });
      setRouteInfo(res.data);

      // Extract coordinates to plot path polyline
      const startBld = CAMPUS_BUILDINGS.find(b => b.id === startBuilding);
      const destBld = CAMPUS_BUILDINGS.find(b => b.id === destBuilding);
      if (startBld && destBld) {
        setRouteLine([startBld.center, destBld.center]);
      }
    } catch (err) {
      alert('Failed to calculate route.');
    }
  };

  return (
    <div style={{ fontFamily: 'sans-serif', maxWidth: '1000px', margin: '20px auto', padding: '20px', backgroundColor: '#ffffff', color: '#0f172a', borderRadius: '12px', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }}>
      <header style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', borderBottom: '2px solid #e2e8f0', paddingBottom: '15px' }}>
        <h1 style={{ color: '#0f172a', display: 'flex', alignItems: 'center', gap: '10px', margin: 0, fontSize: '28px' }}>
          <ShieldCheck color="#2563eb" size={32} /> Campus360 GIS Portal
        </h1>
        <div style={{ fontSize: '14px', color: '#475569' }}>Student: <strong>{studentEmail}</strong></div>
      </header>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '10px', margin: '20px 0' }}>
        <button onClick={() => setActiveTab('attendance')} style={tabStyle(activeTab === 'attendance')}>
          <Camera size={18} /> Interactive Attendance Map
        </button>
        <button onClick={() => { setActiveTab('history'); fetchHistory(); }} style={tabStyle(activeTab === 'history')}>
          <History size={18} /> Attendance History
        </button>
        <button onClick={() => setActiveTab('navigation')} style={tabStyle(activeTab === 'navigation')}>
          <Navigation size={18} /> Spatial Route Engine
        </button>
      </div>

      {/* TAB 1: ATTENDANCE MAP */}
      {activeTab === 'attendance' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Select Target Building on Map</h3>
            <p style={{ fontSize: '13px', color: '#64748b' }}>Click on a blue polygon boundary to set target building, or click anywhere on map to set GPS coordinates.</p>
            
            {/* LEAFLET MAP CONTAINER */}
            <div style={{ height: '350px', width: '100%', borderRadius: '8px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
              <MapContainer center={[19.1065, 72.857]} zoom={15} style={{ height: '100%', width: '100%' }}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" attribution="&copy; OpenStreetMap" />
                <LocationMarker position={location} setLocation={setLocation} />
                {CAMPUS_BUILDINGS.map((bld) => (
                  <Polygon
                    key={bld.id}
                    positions={bld.polygon}
                    pathOptions={{
                      color: selectedBuilding === bld.id ? '#2563eb' : '#64748b',
                      fillColor: selectedBuilding === bld.id ? '#3b82f6' : '#94a3b8',
                      fillOpacity: 0.5
                    }}
                    eventHandlers={{
                      click: () => setSelectedBuilding(bld.id)
                    }}
                  >
                    <Popup><strong>{bld.name}</strong> ({bld.code})</Popup>
                  </Polygon>
                ))}
              </MapContainer>
            </div>
          </div>

          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Mark Attendance</h3>
            <form onSubmit={handleMarkAttendance}>
              <div style={{ marginBottom: '15px' }}>
                <label style={labelStyle}>Selected Building:</label>
                <select value={selectedBuilding} onChange={(e) => setSelectedBuilding(e.target.value)} style={selectStyle}>
                  {CAMPUS_BUILDINGS.map((b) => (
                    <option key={b.id} value={b.id}>{b.name} ({b.code})</option>
                  ))}
                </select>
              </div>

              <div style={{ marginBottom: '15px' }}>
                <label style={labelStyle}>GPS Verification Coordinates:</label>
                <input type="text" readOnly value={`Lat: ${location.lat.toFixed(4)}, Lon: ${location.lon.toFixed(4)}`} style={inputStyle} />
              </div>

              <div style={{ marginBottom: '20px' }}>
                <label style={labelStyle}>Upload Verification Photo:</label>
                <input type="file" accept="image/*" onChange={(e) => setSelectedFile(e.target.files[0])} style={inputStyle} />
              </div>

              <button type="submit" disabled={loading} style={btnPrimary}>
                {loading ? 'Verifying AI & Spatial Geofence...' : 'Verify & Mark Attendance'}
              </button>
            </form>

            {verificationResult && (
              <div style={{ marginTop: '20px', padding: '15px', borderRadius: '8px', border: `2px solid ${verificationResult.verified ? '#22c55e' : '#ef4444'}`, backgroundColor: verificationResult.verified ? '#f0fdf4' : '#fef2f2' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 'bold', color: verificationResult.verified ? '#15803d' : '#b91c1c' }}>
                  {verificationResult.verified ? <CheckCircle2 size={20} /> : <XCircle size={20} />}
                  {verificationResult.statusMessage}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {/* TAB 2: HISTORY */}
      {activeTab === 'history' && (
        <div style={cardStyle}>
          <h2 style={{ marginTop: 0 }}>Historical Attendance Logs (PostgreSQL)</h2>
          {history.length === 0 ? (
            <p style={{ color: '#64748b' }}>No records found. Mark attendance to view logs.</p>
          ) : (
            <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '15px' }}>
              <thead>
                <tr style={{ backgroundColor: '#f1f5f9', textAlign: 'left' }}>
                  <th style={thStyle}>Building</th>
                  <th style={thStyle}>Status</th>
                  <th style={thStyle}>AI Score</th>
                  <th style={thStyle}>Timestamp</th>
                </tr>
              </thead>
              <tbody>
                {history.map((row) => (
                  <tr key={row.id} style={{ borderBottom: '1px solid #cbd5e1' }}>
                    <td style={tdStyle}>{row.buildingName}</td>
                    <td style={{ ...tdStyle, color: row.verified ? '#16a34a' : '#dc2626', fontWeight: 'bold' }}>
                      {row.verified ? 'Verified' : 'Failed'}
                    </td>
                    <td style={tdStyle}>{(row.confidenceScore * 100).toFixed(1)}%</td>
                    <td style={tdStyle}>{new Date(row.timestamp).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {/* TAB 3: NAVIGATION */}
      {activeTab === 'navigation' && (
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Campus Walking Route Map</h3>
            <div style={{ height: '350px', width: '100%', borderRadius: '8px', overflow: 'hidden', border: '1px solid #cbd5e1' }}>
              <MapContainer center={[19.1065, 72.857]} zoom={15} style={{ height: '100%', width: '100%' }}>
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
                {CAMPUS_BUILDINGS.map((bld) => (
                  <Polygon key={bld.id} positions={bld.polygon} pathOptions={{ color: '#3b82f6', fillColor: '#93c5fd', fillOpacity: 0.4 }}>
                    <Popup>{bld.name}</Popup>
                  </Polygon>
                ))}
                {routeLine.length > 0 && <Polyline positions={routeLine} color="#dc2626" weight={4} dashArray="5, 10" />}
              </MapContainer>
            </div>
          </div>

          <div style={cardStyle}>
            <h3 style={{ marginTop: 0 }}>Route Calculator</h3>
            <div style={{ marginBottom: '15px' }}>
              <label style={labelStyle}>Start Location:</label>
              <select value={startBuilding} onChange={(e) => setStartBuilding(e.target.value)} style={selectStyle}>
                {CAMPUS_BUILDINGS.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
            </div>
            <div style={{ marginBottom: '15px' }}>
              <label style={labelStyle}>Destination:</label>
              <select value={destBuilding} onChange={(e) => setDestBuilding(e.target.value)} style={selectStyle}>
                {CAMPUS_BUILDINGS.map((b) => <option key={b.id} value={b.id}>{b.name}</option>)}
              </select>
            </div>
            <button onClick={handleCalculateRoute} style={btnPrimary}>Calculate Path on Map</button>

            {routeInfo && (
              <div style={{ marginTop: '20px', padding: '15px', backgroundColor: '#eff6ff', borderRadius: '8px', border: '1px solid #bfdbfe' }}>
                <h4 style={{ margin: '0 0 10px 0', color: '#1e40af' }}>PostGIS Route Result</h4>
                <p>Distance: <strong>{routeInfo.distanceMeters} meters</strong></p>
                <p>Est. Walking Time: <strong>{routeInfo.estimatedWalkTimeMinutes} mins</strong></p>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// Inline Styling
const cardStyle = { backgroundColor: '#f8fafc', padding: '20px', borderRadius: '8px', border: '1px solid #e2e8f0' };
const labelStyle = { display: 'block', marginBottom: '6px', fontWeight: 'bold', color: '#0f172a' };
const inputStyle = { width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #94a3b8', backgroundColor: '#ffffff', color: '#0f172a', fontSize: '14px', boxSizing: 'border-box' };
const selectStyle = { width: '100%', padding: '10px', borderRadius: '6px', border: '1px solid #94a3b8', backgroundColor: '#ffffff', color: '#0f172a', fontSize: '14px', boxSizing: 'border-box', cursor: 'pointer' };
const btnPrimary = { width: '100%', backgroundColor: '#2563eb', color: '#ffffff', padding: '12px', border: 'none', borderRadius: '6px', fontWeight: 'bold', cursor: 'pointer' };
const tabStyle = (active) => ({ flex: 1, padding: '10px', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: 'bold', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px', backgroundColor: active ? '#2563eb' : '#e2e8f0', color: active ? '#ffffff' : '#334155' });
const thStyle = { padding: '10px', color: '#0f172a', fontWeight: 'bold' };
const tdStyle = { padding: '10px', color: '#334155' };