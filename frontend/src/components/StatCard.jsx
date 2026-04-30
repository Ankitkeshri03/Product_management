export default function StatCard({ label, value, accent }) {
  return (
    <div className="stat-card">
      <span className="stat-accent" style={{ background: accent }} />
      <p>{label}</p>
      <strong>{value}</strong>
    </div>
  );
}
