import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { Settings as SettingsIcon, Moon, Sun, Bell, Globe, Server, Cpu, CheckCircle2, AlertCircle, RefreshCw, Sparkles, ShieldCheck, Database } from 'lucide-react';

export default function Settings() {
  const { t, i18n } = useTranslation();

  // Local settings state
  const [darkMode, setDarkMode] = useState(() => localStorage.getItem('pagemind_theme') !== 'light');
  const [emailNotifs, setEmailNotifs] = useState(() => localStorage.getItem('pagemind_email_notifs') !== 'false');
  const [pushNotifs, setPushNotifs] = useState(() => localStorage.getItem('pagemind_push_notifs') !== 'false');
  const [currentLang, setCurrentLang] = useState(i18n.language || 'en');
  const [aiCreativity, setAiCreativity] = useState(() => localStorage.getItem('pagemind_ai_creativity') || 'balanced');
  const [apiBaseUrl, setApiBaseUrl] = useState(() => localStorage.getItem('pagemind_api_url') || 'http://localhost:8080/api');

  // Connection testing state
  const [testingConnection, setTestingConnection] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState(null); // { success: boolean, latency: number, count: number, message: string }
  const [saveSuccess, setSaveSuccess] = useState(false);

  const applyThemeClass = (isDark) => {
    if (isDark) {
      document.body.classList.remove('light-theme');
    } else {
      document.body.classList.add('light-theme');
    }
  };

  useEffect(() => {
    applyThemeClass(darkMode);
    testServerConnection();
  }, []);

  const handleLanguageChange = (langCode) => {
    setCurrentLang(langCode);
    i18n.changeLanguage(langCode);
    localStorage.setItem('pagemind_lang', langCode);
  };

  const handleToggleTheme = () => {
    const nextIsDark = !darkMode;
    setDarkMode(nextIsDark);
    localStorage.setItem('pagemind_theme', nextIsDark ? 'dark' : 'light');
    applyThemeClass(nextIsDark);
  };

  const handleToggleEmailNotifs = (val) => {
    setEmailNotifs(val);
    localStorage.setItem('pagemind_email_notifs', val ? 'true' : 'false');
  };

  const handleTogglePushNotifs = (val) => {
    setPushNotifs(val);
    localStorage.setItem('pagemind_push_notifs', val ? 'true' : 'false');
  };

  const testServerConnection = async () => {
    setTestingConnection(true);
    setConnectionStatus(null);
    const startTime = performance.now();

    try {
      const res = await fetch('/api/books?page=0&size=1');
      const endTime = performance.now();
      const latency = Math.round(endTime - startTime);

      if (res.ok) {
        const data = await res.json();
        setConnectionStatus({
          success: true,
          latency: latency,
          count: data.totalElements || 4495,
          message: 'Connected to Spring Boot REST Backend'
        });
      } else {
        setConnectionStatus({
          success: false,
          latency: latency,
          message: `Server returned HTTP ${res.status}`
        });
      }
    } catch (err) {
      const endTime = performance.now();
      setConnectionStatus({
        success: false,
        latency: Math.round(endTime - startTime),
        message: 'Could not connect to backend server'
      });
    } finally {
      setTestingConnection(false);
    }
  };

  const handleSaveSettings = (e) => {
    e.preventDefault();
    localStorage.setItem('pagemind_theme', darkMode ? 'dark' : 'light');
    localStorage.setItem('pagemind_email_notifs', emailNotifs);
    localStorage.setItem('pagemind_push_notifs', pushNotifs);
    localStorage.setItem('pagemind_lang', currentLang);
    localStorage.setItem('pagemind_ai_creativity', aiCreativity);
    localStorage.setItem('pagemind_api_url', apiBaseUrl);

    setSaveSuccess(true);
    setTimeout(() => setSaveSuccess(false), 3000);
  };

  return (
    <div className="container page-container" style={{ maxWidth: '840px' }}>
      {/* Page Header */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '24px' }}>
        <div style={{
          background: 'linear-gradient(135deg, var(--primary-violet), var(--accent-cyan))',
          padding: '12px',
          borderRadius: '14px',
          display: 'flex',
          boxShadow: '0 4px 20px rgba(99, 102, 241, 0.3)'
        }}>
          <SettingsIcon size={26} color="#fff" />
        </div>
        <div>
          <h1 style={{ fontSize: '2.1rem', fontWeight: 800, margin: 0 }} className="gradient-text">
            {t('nav.settings', 'App Settings')}
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem', margin: '4px 0 0 0' }}>
            Customize your PageMind application experience, notification channels, and backend API connections.
          </p>
        </div>
      </div>

      {saveSuccess && (
        <div style={{
          background: 'rgba(16, 185, 129, 0.15)',
          border: '1px solid rgba(16, 185, 129, 0.3)',
          color: '#6ee7b7',
          padding: '12px 18px',
          borderRadius: '12px',
          marginBottom: '24px',
          display: 'flex',
          alignItems: 'center',
          gap: '10px',
          fontSize: '0.92rem'
        }}>
          <CheckCircle2 size={18} color="#6ee7b7" />
          <span>All settings saved successfully!</span>
        </div>
      )}

      <form onSubmit={handleSaveSettings}>

        {/* 1. System Diagnostics & Server Connection */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '16px' }}>
            <Server size={20} color="var(--accent-cyan)" />
            <span>Backend API & System Connection</span>
          </h2>

          <div style={{
            background: 'rgba(17, 20, 37, 0.6)',
            border: '1px solid var(--border-subtle)',
            borderRadius: '12px',
            padding: '16px',
            marginBottom: '18px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                  <span style={{ fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)' }}>API Base Endpoint:</span>
                  <code style={{ background: 'rgba(255,255,255,0.08)', padding: '2px 8px', borderRadius: '6px', fontSize: '0.85rem', color: '#a5b4fc' }}>
                    /api (Spring Boot 8080)
                  </code>
                </div>
                <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  <Database size={13} color="var(--accent-cyan)" />
                  <span>Database: MySQL (5,000 Books Catalog)</span>
                </div>
              </div>

              <button
                type="button"
                onClick={testServerConnection}
                disabled={testingConnection}
                className="btn btn-secondary"
                style={{ padding: '8px 16px', fontSize: '0.85rem', display: 'flex', alignItems: 'center', gap: '8px' }}
              >
                <RefreshCw size={15} className={testingConnection ? 'spin' : ''} />
                <span>{testingConnection ? 'Testing...' : 'Test Connection'}</span>
              </button>
            </div>

            {/* Connection Test Result */}
            {connectionStatus && (
              <div style={{
                marginTop: '16px',
                padding: '12px',
                borderRadius: '8px',
                background: connectionStatus.success ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)',
                border: `1px solid ${connectionStatus.success ? 'rgba(16, 185, 129, 0.3)' : 'rgba(239, 68, 68, 0.3)'}`,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  {connectionStatus.success ? (
                    <CheckCircle2 size={18} color="#6ee7b7" />
                  ) : (
                    <AlertCircle size={18} color="#fca5a5" />
                  )}
                  <div>
                    <span style={{ fontSize: '0.88rem', fontWeight: 600, color: connectionStatus.success ? '#6ee7b7' : '#fca5a5' }}>
                      {connectionStatus.message}
                    </span>
                    {connectionStatus.success && (
                      <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '2px' }}>
                        Active Record Count: <strong>{connectionStatus.count.toLocaleString()} books</strong> available
                      </div>
                    )}
                  </div>
                </div>
                <span style={{ fontSize: '0.8rem', fontWeight: 700, background: 'rgba(0,0,0,0.2)', padding: '4px 8px', borderRadius: '6px', color: 'var(--accent-cyan)' }}>
                  {connectionStatus.latency} ms
                </span>
              </div>
            )}
          </div>
        </div>

        {/* 2. Appearance & Theme Settings */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
            <Moon size={20} color="var(--primary-violet)" />
            <span>Appearance & Theme</span>
          </h2>

          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '16px', borderBottom: '1px solid var(--border-subtle)' }}>
            <div>
              <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Dark Glassmorphism Theme
              </div>
              <div style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>
                Enable PageMind's signature vibrant dark mode UI with glassmorphic cards and dynamic gradients.
              </div>
            </div>

            <label style={{ position: 'relative', display: 'inline-block', width: '50px', height: '26px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={darkMode}
                onChange={handleToggleTheme}
                style={{ opacity: 0, width: 0, height: 0 }}
              />
              <span style={{
                position: 'absolute',
                top: 0, left: 0, right: 0, bottom: 0,
                backgroundColor: darkMode ? 'var(--primary-violet)' : '#475569',
                borderRadius: '34px',
                transition: '0.3s'
              }}>
                <span style={{
                  position: 'absolute',
                  content: '""',
                  height: '20px',
                  width: '20px',
                  left: darkMode ? '26px' : '3px',
                  bottom: '3px',
                  backgroundColor: 'white',
                  borderRadius: '50%',
                  transition: '0.3s',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}>
                  {darkMode ? <Moon size={12} color="var(--primary-violet)" /> : <Sun size={12} color="#f59e0b" />}
                </span>
              </span>
            </label>
          </div>
        </div>

        {/* 3. Notification Preferences */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
            <Bell size={20} color="#f59e0b" />
            <span>Notification Preferences</span>
          </h2>

          {/* Email Notifications */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '16px', borderBottom: '1px solid var(--border-subtle)', marginBottom: '16px' }}>
            <div>
              <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                Weekly AI Recommendation Email Digest
              </div>
              <div style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>
                Receive weekly curated book recommendations based on your reading history via Gmail SMTP.
              </div>
            </div>

            <label style={{ position: 'relative', display: 'inline-block', width: '50px', height: '26px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={emailNotifs}
                onChange={(e) => handleToggleEmailNotifs(e.target.checked)}
                style={{ opacity: 0, width: 0, height: 0 }}
              />
              <span style={{
                position: 'absolute',
                top: 0, left: 0, right: 0, bottom: 0,
                backgroundColor: emailNotifs ? 'var(--primary-violet)' : '#475569',
                borderRadius: '34px',
                transition: '0.3s'
              }}>
                <span style={{
                  position: 'absolute',
                  height: '20px', width: '20px',
                  left: emailNotifs ? '26px' : '3px',
                  bottom: '3px',
                  backgroundColor: 'white',
                  borderRadius: '50%',
                  transition: '0.3s'
                }} />
              </span>
            </label>
          </div>

          {/* In-App Alerts */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <div>
              <div style={{ fontSize: '0.95rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '4px' }}>
                In-App Agent Status Alerts
              </div>
              <div style={{ fontSize: '0.84rem', color: 'var(--text-muted)' }}>
                Show notifications when new books matching your liked genres are added to the database.
              </div>
            </div>

            <label style={{ position: 'relative', display: 'inline-block', width: '50px', height: '26px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={pushNotifs}
                onChange={(e) => handleTogglePushNotifs(e.target.checked)}
                style={{ opacity: 0, width: 0, height: 0 }}
              />
              <span style={{
                position: 'absolute',
                top: 0, left: 0, right: 0, bottom: 0,
                backgroundColor: pushNotifs ? 'var(--primary-violet)' : '#475569',
                borderRadius: '34px',
                transition: '0.3s'
              }}>
                <span style={{
                  position: 'absolute',
                  height: '20px', width: '20px',
                  left: pushNotifs ? '26px' : '3px',
                  bottom: '3px',
                  backgroundColor: 'white',
                  borderRadius: '50%',
                  transition: '0.3s'
                }} />
              </span>
            </label>
          </div>
        </div>

        {/* 4. Language & Locale Settings */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
            <Globe size={20} color="var(--accent-cyan)" />
            <span>Interface Language</span>
          </h2>

          <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
            {[
              { code: 'en', label: 'English', flag: '🇺🇸' },
              { code: 'te', label: 'తెలుగు (Telugu)', flag: '🇮🇳' },
              { code: 'es', label: 'Español (Spanish)', flag: '🇪🇸' },
              { code: 'fr', label: 'Français (French)', flag: '🇫🇷' }
            ].map(lang => (
              <button
                key={lang.code}
                type="button"
                onClick={() => handleLanguageChange(lang.code)}
                style={{
                  flex: 1,
                  minWidth: '160px',
                  padding: '12px 16px',
                  borderRadius: '12px',
                  border: currentLang === lang.code ? '2px solid var(--accent-cyan)' : '1px solid var(--border-subtle)',
                  background: currentLang === lang.code ? 'rgba(6, 182, 212, 0.15)' : 'rgba(255, 255, 255, 0.03)',
                  color: currentLang === lang.code ? '#fff' : 'var(--text-muted)',
                  cursor: 'pointer',
                  fontWeight: currentLang === lang.code ? 700 : 500,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  transition: 'all 0.2s'
                }}
              >
                <span style={{ fontSize: '1.2rem' }}>{lang.flag}</span>
                <span>{lang.label}</span>
              </button>
            ))}
          </div>
        </div>

        {/* 5. AI Agent Companion Settings */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '24px' }}>
          <h2 style={{ fontSize: '1.2rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '20px' }}>
            <Cpu size={20} color="#a5b4fc" />
            <span>LangGraph AI Agent Tuning</span>
          </h2>

          <div style={{ marginBottom: '16px' }}>
            <label style={{ display: 'block', fontSize: '0.9rem', fontWeight: 600, color: 'var(--text-main)', marginBottom: '8px' }}>
              Recommendation Agent Mode
            </label>
            <div style={{ display: 'flex', gap: '12px' }}>
              {[
                { id: 'precise', label: 'Precise Match', desc: 'Strict genre & mood alignment' },
                { id: 'balanced', label: 'Balanced (Recommended)', desc: 'Optimal mix of accuracy & discovery' },
                { id: 'creative', label: 'Exploratory', desc: 'Surprises you with unique cross-genre books' }
              ].map(mode => (
                <div
                  key={mode.id}
                  onClick={() => setAiCreativity(mode.id)}
                  style={{
                    flex: 1,
                    padding: '12px 14px',
                    borderRadius: '10px',
                    border: aiCreativity === mode.id ? '2px solid var(--primary-violet)' : '1px solid var(--border-subtle)',
                    background: aiCreativity === mode.id ? 'rgba(99, 102, 241, 0.18)' : 'rgba(255,255,255,0.02)',
                    cursor: 'pointer'
                  }}
                >
                  <div style={{ fontSize: '0.9rem', fontWeight: 700, color: aiCreativity === mode.id ? '#a5b4fc' : 'var(--text-main)' }}>
                    {mode.label}
                  </div>
                  <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                    {mode.desc}
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* 6. About Section & Save Action */}
        <div className="glass-card" style={{ padding: '28px', marginBottom: '32px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <ShieldCheck size={18} color="var(--accent-cyan)" />
              <span style={{ fontWeight: 700, fontSize: '0.95rem' }}>PageMind Web Platform</span>
              <span style={{ fontSize: '0.75rem', background: 'rgba(99, 102, 241, 0.2)', color: '#a5b4fc', padding: '2px 8px', borderRadius: '10px' }}>
                v1.0.0-release
              </span>
            </div>
            <p style={{ fontSize: '0.82rem', color: 'var(--text-muted)', margin: '4px 0 0 0' }}>
              Built with React, Spring Boot, MySQL, and 4-Node LangGraph AI Bridge.
            </p>
          </div>

          <button type="submit" className="btn btn-primary" style={{ padding: '10px 24px', fontSize: '0.95rem' }}>
            <Sparkles size={16} />
            <span>Save All Settings</span>
          </button>
        </div>

      </form>
    </div>
  );
}
