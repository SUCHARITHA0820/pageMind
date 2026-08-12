import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { BookOpen, Bot, User, LogOut, LogIn, Globe, Search } from 'lucide-react';

export default function Navbar() {
  const { t, i18n } = useTranslation();
  const { user, logout, language, updateLanguage } = useAuth();
  const navigate = useNavigate();

  const handleLanguageToggle = (e) => {
    const newLang = e.target.value;
    updateLanguage(newLang);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <nav style={{
      background: 'rgba(17, 20, 37, 0.85)',
      backdropFilter: 'blur(12px)',
      borderBottom: '1px solid var(--border-subtle)',
      position: 'sticky',
      top: 0,
      zIndex: 100
    }}>
      <div className="container" style={{
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        height: '70px'
      }}>
        {/* Brand Logo */}
        <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <div style={{
            background: 'linear-gradient(135deg, var(--primary-violet), var(--accent-cyan))',
            padding: '8px',
            borderRadius: '10px',
            display: 'flex'
          }}>
            <BookOpen size={22} color="#fff" />
          </div>
          <span style={{ fontSize: '1.4rem', fontWeight: 800 }} className="gradient-text">
            {t('nav.brand')}
          </span>
        </Link>

        {/* Navigation Links */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '24px' }}>
          <Link to="/" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.95rem', fontWeight: 500 }}>
            <span>{t('nav.home')}</span>
          </Link>

          <Link to="/search" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.95rem', fontWeight: 500 }}>
            <Search size={16} />
            <span>Search</span>
          </Link>

          <Link to="/chatbot" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.95rem', fontWeight: 500 }}>
            <Bot size={16} color="var(--accent-cyan)" />
            <span>{t('nav.chatbot')}</span>
          </Link>

          <Link to="/profile" style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.95rem', fontWeight: 500 }}>
            <User size={16} color="#a5b4fc" />
            <span>{t('nav.profile', 'Profile')}</span>
          </Link>

          {/* Language Selector Dropdown */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: 'rgba(255, 255, 255, 0.05)', padding: '4px 10px', borderRadius: '8px', border: '1px solid var(--card-border)' }}>
            <Globe size={16} color="#a5b4fc" />
            <select
              value={language}
              onChange={handleLanguageToggle}
              style={{
                background: 'transparent',
                border: 'none',
                color: 'var(--text-main)',
                fontSize: '0.85rem',
                fontWeight: 600,
                outline: 'none',
                cursor: 'pointer'
              }}
            >
              <option value="en" style={{ background: 'var(--bg-surface)' }}>EN 🇬🇧</option>
              <option value="te" style={{ background: 'var(--bg-surface)' }}>TE 🇮🇳</option>
            </select>
          </div>

          {/* Auth Action Buttons */}
          {user ? (
            <button onClick={handleLogout} className="btn btn-secondary" style={{ padding: '8px 16px', fontSize: '0.88rem' }}>
              <LogOut size={16} />
              <span>{t('nav.logout')}</span>
            </button>
          ) : (
            <div style={{ display: 'flex', gap: '12px' }}>
              <Link to="/login" className="btn btn-secondary" style={{ padding: '8px 16px', fontSize: '0.88rem' }}>
                <LogIn size={16} />
                <span>{t('nav.login')}</span>
              </Link>
              <Link to="/signup" className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '0.88rem' }}>
                <span>{t('nav.signup')}</span>
              </Link>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}
