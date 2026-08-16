import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { BookOpen, Bot, User, LogOut, LogIn, Search } from 'lucide-react';

export default function Navbar() {
  const { t } = useTranslation();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

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

          <Link to="/profile" style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.95rem', fontWeight: 500 }}>
            {user?.profilePicUrl ? (
              <img
                src={user.profilePicUrl}
                alt="Profile Avatar"
                style={{
                  width: '26px',
                  height: '26px',
                  borderRadius: '50%',
                  objectFit: 'cover',
                  border: '1.5px solid var(--accent-cyan)'
                }}
                onError={(e) => {
                  e.target.style.display = 'none';
                  e.target.nextSibling.style.display = 'inline-block';
                }}
              />
            ) : null}
            <span
              style={{
                display: user?.profilePicUrl ? 'none' : 'inline-block',
                fontSize: '1.1rem',
                lineHeight: 1
              }}
              title="Contact Emoji Avatar"
            >
              👤
            </span>
            <span>{t('nav.profile', 'Profile')}</span>
          </Link>

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
