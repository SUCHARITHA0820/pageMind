import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { Globe, Check, ArrowRight } from 'lucide-react';

export default function LanguageSelect() {
  const { t } = useTranslation();
  const { language, updateLanguage } = useAuth();
  const navigate = useNavigate();
  const [selectedLang, setSelectedLang] = useState(language || 'en');

  const handleSelectLanguage = (lang) => {
    setSelectedLang(lang);
    updateLanguage(lang);
  };

  const handleSaveAndContinue = async () => {
    await updateLanguage(selectedLang);
    navigate('/');
  };

  return (
    <div className="container page-container" style={{ alignItems: 'center', justifyContent: 'center' }}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '520px', padding: '40px', textAlign: 'center' }}>
        <div style={{
          width: '64px',
          height: '64px',
          borderRadius: '50%',
          background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(6, 182, 212, 0.2))',
          border: '1px solid var(--primary-violet)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          margin: '0 auto 20px'
        }}>
          <Globe size={32} color="#6366f1" />
        </div>

        <h2 style={{ fontSize: '1.8rem', fontWeight: 700, marginBottom: '10px' }}>
          {t('language_select.title')}
        </h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '32px', fontSize: '0.94rem', lineHeight: '1.5' }}>
          {t('language_select.subtitle')}
        </p>

        {/* Language Options Grid */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px', marginBottom: '32px' }}>
          {/* English Card */}
          <div
            onClick={() => handleSelectLanguage('en')}
            style={{
              padding: '20px',
              borderRadius: '12px',
              border: selectedLang === 'en' ? '2px solid var(--primary-violet)' : '1px solid var(--card-border)',
              background: selectedLang === 'en' ? 'rgba(99, 102, 241, 0.15)' : 'rgba(255, 255, 255, 0.03)',
              cursor: 'pointer',
              transition: 'all 0.25s ease',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '10px'
            }}
          >
            <span style={{ fontSize: '2rem' }}>🇬🇧</span>
            <span style={{ fontWeight: 600, fontSize: '1.05rem', color: selectedLang === 'en' ? '#a5b4fc' : 'var(--text-main)' }}>
              {t('language_select.english')}
            </span>
            {selectedLang === 'en' && <Check size={18} color="#6366f1" />}
          </div>

          {/* Telugu Card */}
          <div
            onClick={() => handleSelectLanguage('te')}
            style={{
              padding: '20px',
              borderRadius: '12px',
              border: selectedLang === 'te' ? '2px solid var(--accent-cyan)' : '1px solid var(--card-border)',
              background: selectedLang === 'te' ? 'rgba(6, 182, 212, 0.15)' : 'rgba(255, 255, 255, 0.03)',
              cursor: 'pointer',
              transition: 'all 0.25s ease',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '10px'
            }}
          >
            <span style={{ fontSize: '2rem' }}>🇮🇳</span>
            <span style={{ fontWeight: 600, fontSize: '1.05rem', color: selectedLang === 'te' ? '#38bdf8' : 'var(--text-main)' }}>
              {t('language_select.telugu')}
            </span>
            {selectedLang === 'te' && <Check size={18} color="#06b6d4" />}
          </div>
        </div>

        <button onClick={handleSaveAndContinue} className="btn btn-primary" style={{ width: '100%', padding: '14px', fontSize: '1rem' }}>
          <span>{t('language_select.save_continue')}</span>
          <ArrowRight size={18} />
        </button>
      </div>
    </div>
  );
}
