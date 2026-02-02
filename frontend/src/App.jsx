import { useState } from 'react'
import { BarChart, Bar, PieChart, Pie, Cell, 
         XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import { useMembers, useDashboard, useCurrentTime } from './hooks'
import { SkeletonCard, SkeletonChart, SkeletonList } from './components/Skeleton'
import { ErrorMessage, EmptyState } from './components/ErrorMessage'
import './App.css'

const COLORS = ['#10b981', '#3b82f6', '#8b5cf6', '#ec4899', '#f97316', '#eab308', '#06b6d4', '#14b8a6'];

const formatMinutes = (mins) => {
  if (!mins) return "0m";
  const hours = Math.floor(mins / 60);
  const minutes = mins % 60;
  if (hours > 0) return `${hours}h ${minutes}m`;
  return `${minutes}m`;
};

const formatMinutesDetailed = (mins, seconds = 0) => {
  if (mins == null) return "0m";
  // allow secs passed separately (for topApplications)
  let totalSeconds = Math.floor(mins) * 60 + Math.floor(seconds || 0);
  if (totalSeconds < 0) totalSeconds = 0;
  const h = Math.floor(totalSeconds / 3600);
  totalSeconds = totalSeconds % 3600;
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${h}h ${m}m ${s}s`;
};

function App() {
  const [selectedMember, setSelectedMember] = useState(null);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split('T')[0]);
  const [activeTab, setActiveTab] = useState('summary');
  const [isSyncing, setIsSyncing] = useState(false);
  
  const currentTime = useCurrentTime();
  const { members, loading: membersLoading, error: membersError, refetch: refetchMembers } = useMembers(selectedDate);
  const { dashboard, loading: dashboardLoading, error: dashboardError, refetch: refetchDashboard } = useDashboard(selectedMember, selectedDate);

  const handleSync = async () => {
    setIsSyncing(true);
    try {
      if (selectedMember) {
        await refetchDashboard();
      } else {
        await refetchMembers();
      }
      // Keep syncing state visible for smooth UX
      await new Promise(resolve => setTimeout(resolve, 600));
    } catch (error) {
      console.error('Sync failed:', error);
    } finally {
      setIsSyncing(false);
    }
  };

  const getTimeOfDay = () => {
    const hour = currentTime.getHours();
    if (hour < 12) return "Good morning";
    if (hour < 18) return "Good afternoon";
    return "Good evening";
  };

  const getActiveCount = () => members.filter(m => m.isActive).length;

  return (
    <div className="app">
      {/* Animated Background Orbs */}
      <div className="bg-orb orb-1"></div>
      <div className="bg-orb orb-2"></div>
      <div className="bg-orb orb-3"></div>

      {/* Header */}
      <header className="header">
        <div className="header-left">
          <h1>⚡ WorkTracker</h1>
          <nav className="nav-tabs">
            <button className={activeTab === 'summary' ? 'active' : ''} onClick={() => { setActiveTab('summary'); setSelectedMember(null); }}>
              📊 Activity
            </button>
            <button className={activeTab === 'timeline' ? 'active' : ''} onClick={() => setActiveTab('timeline')}>
              📈 Timeline
            </button>
          </nav>
        </div>
        <div className="header-right">
          <div className="live-clock">
            <span className="pulse"></span>
            <span>{currentTime.toLocaleTimeString()}</span>
          </div>
          <input 
            type="date" 
            value={selectedDate} 
            onChange={(e) => setSelectedDate(e.target.value)}
            className="date-picker"
          />
          <button 
            onClick={handleSync} 
            className={`refresh-btn ${isSyncing ? 'syncing' : ''}`}
            disabled={isSyncing}
          >
            <span className="sync-icon">⟳</span>
            <span>{isSyncing ? 'Syncing...' : 'Sync'}</span>
          </button>
        </div>
      </header>

      <main className="main">
        {activeTab === 'timeline' ? (
          /* Timeline View */
          <TimelineView members={members} loading={membersLoading} error={membersError} selectedDate={selectedDate} refetch={refetchMembers} />
        ) : !selectedMember ? (
          /* Team Overview */
          <div className="team-overview">
            <div className="page-header">
              <h2>{getTimeOfDay()} 👋</h2>
              <p className="subtitle">
                {new Date(selectedDate).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
                {!membersLoading && ` • ${getActiveCount()} of ${members.length || 5} team members active`}
              </p>
            </div>

            {membersError ? (
              <ErrorMessage message={membersError} onRetry={refetchMembers} />
            ) : membersLoading ? (
              <div className="members-grid">
                {[...Array(5)].map((_, i) => <SkeletonCard key={i} />)}
              </div>
            ) : members.length === 0 ? (
              <EmptyState 
                icon="👥" 
                title="No team members found" 
                message="Activity will appear here once team members start working"
              />
            ) : (
              <div className="members-grid">
                {members.map((member) => (
                  <div 
                    key={member.username} 
                    className={`member-card ${member.isActive ? 'active' : ''}`}
                    onClick={() => setSelectedMember(member.username)}
                  >
                    <div className="member-header">
                      <span className={`status-indicator ${member.isActive ? 'online' : 'offline'}`}></span>
                      <span className="member-status">{member.isActive ? '● Online' : '○ Offline'}</span>
                    </div>
                    <h3>{member.fullName}</h3>
                    <div className="time-display">
                      <span className="time-value">{formatMinutesDetailed(member.totalActiveMinutes)}</span>
                      <span className="time-label">of 24h</span>
                    </div>
                    {member.isActive && member.currentApplication && (
                      <div className="current-activity">
                        <span className="app-badge">🔵 {member.currentApplication}</span>
                      </div>
                    )}
                    {member.topApp && !member.isActive && (
                      <p className="top-app">🏆 Top: {member.topApp}</p>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : (
          /* Individual Dashboard */
          <div className="dashboard">
            <div className="dashboard-header">
              <button className="back-btn" onClick={() => setSelectedMember(null)}>← Back to Team</button>
              <div>
                <h2>📊 {dashboard?.fullName || selectedMember}</h2>
                <p className="subtitle">
                  {new Date(selectedDate).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric' })} · 
                  Total time: <strong>{formatMinutes(dashboard?.totalActiveMinutes || 0)}</strong>
                </p>
              </div>
            </div>

            {dashboardError ? (
              <ErrorMessage message={dashboardError} onRetry={refetchDashboard} />
            ) : dashboardLoading ? (
              <>
                <SkeletonChart />
                <div className="dashboard-grid">
                  <div className="section"><SkeletonList items={6} /></div>
                  <div className="section"><SkeletonList items={6} /></div>
                </div>
              </>
            ) : (
              <>
                {/* Hourly Activity Chart */}
                {dashboard?.hourlyActivity && (
                  <div className="section chart-section">
                    <h3>⏰ Hourly Activity</h3>
                    <ResponsiveContainer width="100%" height={200}>
                      <BarChart data={dashboard.hourlyActivity}>
                        <defs>
                          <linearGradient id="barGradient" x1="0" y1="0" x2="0" y2="1">
                            <stop offset="0%" stopColor="#667eea"/>
                            <stop offset="100%" stopColor="#764ba2"/>
                          </linearGradient>
                        </defs>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(102, 126, 234, 0.2)" />
                        <XAxis 
                          dataKey="label" 
                          stroke="#718096" 
                          tick={{ fill: '#4a5568', fontSize: 11, fontWeight: 500 }} 
                          interval={2} 
                        />
                        <YAxis 
                          stroke="#718096" 
                          tick={{ fill: '#4a5568', fontSize: 11, fontWeight: 500 }} 
                        />
                        <Tooltip 
                          contentStyle={{ 
                            background: 'rgba(255, 255, 255, 0.95)', 
                            backdropFilter: 'blur(10px)',
                            border: '1px solid rgba(102, 126, 234, 0.3)', 
                            borderRadius: '12px', 
                            boxShadow: '0 8px 32px rgba(102, 126, 234, 0.3)',
                            padding: '12px'
                          }} 
                          labelStyle={{ color: '#1a202c', fontWeight: 600, marginBottom: '4px' }} 
                          itemStyle={{ color: '#4a5568', fontWeight: 500 }}
                          formatter={(value) => [formatMinutesDetailed(value), 'Activity']} 
                        />
                        <Bar dataKey="minutes" radius={[8, 8, 0, 0]} maxBarSize={40}>
                          {dashboard.hourlyActivity.map((entry, index) => (
                            <Cell 
                              key={`cell-${index}`} 
                              fill={entry.minutes > 0 ? 'url(#barGradient)' : 'rgba(102, 126, 234, 0.1)'} 
                            />
                          ))}
                        </Bar>
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                )}

                {/* App Summary */}
                <SummaryTab dashboard={dashboard} />

                {/* Category Tree */}
                <CategoryTree categories={dashboard?.categories} />
              </>
            )}
          </div>
        )}
      </main>

      <footer className="footer">
        <p>⚡ <strong>WorkTracker</strong> · Built with 💜 · Team: Tanmay, Yash, Nidhish, Atharva, Parth</p>
      </footer>
    </div>
  )
}

function SummaryTab({ dashboard }) {
  return (
    <div className="dashboard-grid">
      {/* Top Applications */}
      <div className="section">
        <h3>🚀 Top Applications</h3>
        {dashboard?.topApplications && dashboard.topApplications.length > 0 ? (
          <>
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={dashboard.topApplications.slice(0, 6)} cx="50%" cy="50%" innerRadius={40} outerRadius={70} paddingAngle={2} dataKey="percentage" nameKey="name">
                    {dashboard.topApplications.slice(0, 6).map((entry, index) => (<Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />))}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} formatter={(value, name) => [`${value.toFixed(1)}%`, name]} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="list">
              {dashboard.topApplications.slice(0, 6).map((app, idx) => (
                <div key={idx} className="list-item" style={{ '--color': COLORS[idx % COLORS.length] }}>
                  <span className="item-name">{getAppIcon(app.name)} {app.name}</span>
                  <span className="item-time">{formatMinutesDetailed(app.minutes, app.seconds)}</span>
                  <div className="item-bar" style={{ width: `${app.percentage}%` }}></div>
                </div>
              ))}
            </div>
          </>
        ) : (
          <EmptyState icon="📱" title="No activity" message="No applications recorded yet" />
        )}
      </div>

      {/* Categories */}
      <div className="section">
        <h3>🏷️ Categories</h3>
        {dashboard?.categories?.categories && dashboard.categories.categories.length > 0 ? (
          <>
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={180}>
                <PieChart>
                  <Pie data={dashboard.categories.categories} cx="50%" cy="50%" innerRadius={40} outerRadius={70} paddingAngle={2} dataKey="percentage" nameKey="name">
                    {dashboard.categories.categories.map((entry, index) => (<Cell key={`cell-${index}`} fill={entry.color} />))}
                  </Pie>
                  <Tooltip contentStyle={{ background: '#ffffff', border: '1px solid #e2e8f0', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} formatter={(value, name) => [`${value.toFixed(1)}%`, name]} />
                </PieChart>
              </ResponsiveContainer>
            </div>
            <div className="list">
              {dashboard.categories.categories.map((cat, idx) => (
                <div key={idx} className="list-item" style={{ '--color': cat.color }}>
                  <span className="item-name">{getCategoryIcon(cat.name)} {cat.name}</span>
                  <span className="item-time">{formatMinutesDetailed(cat.minutes)}</span>
                  <div className="item-bar" style={{ width: `${cat.percentage}%`, background: cat.color }}></div>
                </div>
              ))}
            </div>
          </>
        ) : (
          <EmptyState icon="🏷️" title="No categories" message="Categories will appear here" />
        )}
      </div>
    </div>
  );
}

function CategoryTree({ categories }) {
  if (!categories?.tree || categories.tree.length === 0) return null;

  return (
    <div className="section">
      <h3>🌳 Category Tree</h3>
      <div className="tree">
        {categories.tree.map((cat, idx) => (
          <div key={idx} className="tree-category" style={{ '--color': cat.color }}>
            <div className="tree-header">
              <span>{getCategoryIcon(cat.category)} {cat.category}</span>
              <span className="tree-time">{formatMinutesDetailed(cat.totalMinutes)}</span>
            </div>
            <div className="tree-apps">
              {cat.applications?.slice(0, 5).map((app, appIdx) => (
                <div key={appIdx} className="tree-app">
                  <span>└ {app.name}</span>
                  <span>{formatMinutesDetailed(app.minutes)}</span>
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function getAppIcon(appName) {
  if (!appName) return '📱';
  const name = appName.toLowerCase();
  if (name.includes('code') || name.includes('vs code')) return '💻';
  if (name.includes('intellij') || name.includes('idea')) return '🧠';
  if (name.includes('chrome')) return '🌐';
  if (name.includes('firefox')) return '🦊';
  if (name.includes('edge')) return '🌊';
  if (name.includes('terminal') || name.includes('powershell')) return '⬛';
  if (name.includes('zoom')) return '📹';
  if (name.includes('slack')) return '💬';
  if (name.includes('discord')) return '🎮';
  if (name.includes('explorer')) return '📁';
  if (name.includes('spotify')) return '🎵';
  return '📱';
}

function getCategoryIcon(category) {
  const icons = {
    'Programming': '💻',
    'Browsing': '🌐',
    'Communication': '💬',
    'Productivity': '📊',
    'Entertainment': '🎬',
    'Email': '📧',
    'File Management': '📁',
    'Games': '🎮',
    'Media': '🎵',
    'Other': '📦',
  };
  return icons[category] || '📦';
}

function TimelineView({ members, loading, error, selectedDate, refetch }) {
  if (error) {
    return <ErrorMessage message={error} onRetry={refetch} />;
  }

  if (loading) {
    return (
      <div className="timeline-container">
        <div className="page-header">
          <h2>📈 Team Timeline</h2>
          <p className="subtitle">Loading timeline data...</p>
        </div>
        <div className="timeline-grid">
          {[...Array(5)].map((_, i) => <SkeletonCard key={i} />)}
        </div>
      </div>
    );
  }

  if (members.length === 0) {
    return (
      <div className="timeline-container">
        <div className="page-header">
          <h2>📈 Team Timeline</h2>
          <p className="subtitle">
            {new Date(selectedDate).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
          </p>
        </div>
        <EmptyState 
          icon="📈" 
          title="No timeline data available" 
          message="Timeline will show team activity patterns throughout the day"
        />
      </div>
    );
  }

  // Sort members by total active time (minutes-based)
  const sortedMembers = [...members].sort((a, b) => {
    const aMins = parseFloat(a.totalActiveMinutes) || 0;
    const bMins = parseFloat(b.totalActiveMinutes) || 0;
    return bMins - aMins;
  });

  const totalTeamMinutes = sortedMembers.reduce((sum, member) => 
    sum + (parseFloat(member.totalActiveMinutes) || 0), 0
  );

  return (
    <div className="timeline-container">
      <div className="page-header">
        <h2>📈 Team Timeline</h2>
        <p className="subtitle">
          {new Date(selectedDate).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
          {` • Total team activity: ${formatMinutesDetailed(totalTeamMinutes)}`}
        </p>
      </div>

      <div className="timeline-stats">
        <div className="stat-card-mini">
          <div className="stat-icon">👥</div>
          <div className="stat-content">
            <div className="stat-label">Team Members</div>
            <div className="stat-value">{members.length}</div>
          </div>
        </div>
        <div className="stat-card-mini">
          <div className="stat-icon">🟢</div>
          <div className="stat-content">
            <div className="stat-label">Active Now</div>
            <div className="stat-value">{members.filter(m => m.isActive).length}</div>
          </div>
        </div>
        <div className="stat-card-mini">
          <div className="stat-icon">⏱️</div>
          <div className="stat-content">
            <div className="stat-label">Total Activity</div>
            <div className="stat-value">{formatMinutesDetailed(totalTeamMinutes)}</div>
          </div>
        </div>
        <div className="stat-card-mini">
          <div className="stat-icon">📊</div>
          <div className="stat-content">
            <div className="stat-label">Avg per Member</div>
            <div className="stat-value">{formatMinutesDetailed(totalTeamMinutes / Math.max(members.length, 1))}</div>
          </div>
        </div>
      </div>

      <div className="timeline-grid">
        {sortedMembers.map((member, index) => (
          <div key={member.username} className={`timeline-card ${member.isActive ? 'active' : ''}`}>
            <div className="timeline-rank">#{index + 1}</div>
            <div className="timeline-card-header">
              <div className="timeline-member-info">
                <h3>{member.fullName}</h3>
                <span className={`timeline-status ${member.isActive ? 'online' : 'offline'}`}>
                  {member.isActive ? '🟢 Online' : '⚪ Offline'}
                </span>
              </div>
            </div>
            
            <div className="timeline-progress-section">
              <div className="timeline-hours">
                <span className="hours-value">{formatMinutesDetailed(member.totalActiveMinutes)}</span>
                <span className="hours-label">today</span>
              </div>
              <div className="timeline-progress-bar">
                <div 
                  className="timeline-progress-fill" 
                  style={{ width: `${Math.min(((parseFloat(member.totalActiveMinutes) || 0) / 60) * 10, 100)}%` }}
                ></div>
              </div>
            </div>

            {member.isActive && member.currentApplication && (
              <div className="timeline-current-app">
                <span className="app-icon">🔵</span>
                <span className="app-name">Currently: {member.currentApplication}</span>
              </div>
            )}

            {member.topApp && (
              <div className="timeline-top-app">
                <span className="trophy-icon">🏆</span>
                <span className="app-name">Most used: {member.topApp}</span>
              </div>
            )}
          </div>
        ))}
      </div>
    </div>
  );
}

export default App
