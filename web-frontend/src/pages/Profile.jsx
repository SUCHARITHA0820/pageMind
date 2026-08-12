import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { User as UserIcon, Globe, Heart, Save, Edit3, BookOpen, LogIn, UserPlus, Sparkles } from 'lucide-react';
import BookCover from '../components/BookCover';

export default function Profile() {
  const { t } = useTranslation();
  const { user, token, language, updateProfile } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState(user?.name || '');
  const [selectedLang, setSelectedLang] = useState(language || 'en');
  const [likedBooks, setLikedBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (user?.name) {
      setName(user.name);
    }
  }, [user]);

  useEffect(() => {
    if (token) {
      fetchUserProfile();
      fetchLikedBooks();
    }
  }, [token]);

  const fetchUserProfile = async () => {
    try {
      const res = await fetch('/api/user/profile', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        if (data.name) setName(data.name);
        if (data.preferredLanguage) setSelectedLang(data.preferredLanguage);
      }
    } catch (e) {
      // Retain local state
    }
  };

  const fetchLikedBooks = async () => {
    try {
      console.log('[Profile] Fetching liked books with token:', token ? '[PRESENT]' : '[NULL]');
      const res = await fetch('/api/user/likes', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      console.log(`[Profile] GET /api/user/likes status: ${res.status}`);
      if (res.ok) {
        const data = await res.json();
        console.log('[Profile] GET /api/user/likes raw response:', data);
        
        let booksList = [];
        if (Array.isArray(data)) {
          booksList = data;
        } else if (Array.isArray(data?.books)) {
          booksList = data.books;
        } else if (Array.isArray(data?.likedBooks)) {
          booksList = data.likedBooks;
        } else if (Array.isArray(data?.data)) {
          booksList = data.data;
        }
        
        console.log('[Profile] Parsed liked books count:', booksList.length, booksList);
        setLikedBooks(booksList);
      } else {
        const err = await res.json().catch(() => null);
        console.error(`[Profile] GET /api/user/likes failed with status ${res.status}:`, err);
        setLikedBooks([]);
      }
    } catch (e) {
      console.error("[Profile] Error fetching liked books:", e);
      setLikedBooks([]);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSavedSuccess(false);

    try {
      await updateProfile({ name, preferredLanguage: selectedLang });
      setSavedSuccess(true);
      setTimeout(() => setSavedSuccess(false), 3000);
    } catch (e) {
      // Ignore
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-container" style={{ maxWidth: '820px' }}>
      <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '24px' }} className="gradient-text">
        {t('profile.title', 'User Profile')}
      </h1>

      {!user && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '24px', borderLeft: '4px solid var(--accent-cyan)', display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '16px' }}>
          <div>
            <h3 style={{ fontSize: '1.1rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
              <Sparkles size={18} color="var(--accent-cyan)" />
              <span>Browsing as Guest Reader</span>
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.88rem', margin: 0 }}>
              Log in or create an account to sync your liked books and personalized AI recommendations across devices.
            </p>
          </div>
          <div style={{ display: 'flex', gap: '12px' }}>
            <Link to="/login" className="btn btn-secondary" style={{ padding: '8px 16px', fontSize: '0.85rem' }}>
              <LogIn size={15} />
              <span>Log In</span>
            </Link>
            <Link to="/signup" className="btn btn-primary" style={{ padding: '8px 16px', fontSize: '0.85rem' }}>
              <UserPlus size={15} />
              <span>Sign Up</span>
            </Link>
          </div>
        </div>
      )}

      {savedSuccess && (
        <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#6ee7b7', padding: '12px', borderRadius: '10px', marginBottom: '24px', fontSize: '0.9rem' }}>
          Profile and language preferences updated successfully!
        </div>
      )}

      {/* Profile Edit Form Card */}
      <div className="glass-card" style={{ padding: '32px', marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '28px' }}>
          <div style={{
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0
          }}>
            <UserIcon size={32} color="#fff" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 700 }}>
              {user?.name || name || 'PageMind Reader'}
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem' }}>
              {user?.email || 'Guest Mode'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSaveProfile}>
          <div className="input-group">
            <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Edit3 size={16} />
              <span>Full Name</span>
            </label>
            <input
              type="text"
              required
              className="input-field"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="Enter your name"
            />
          </div>

          <div className="input-group">
            <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Globe size={16} />
              <span>{t('profile.preferred_language')}</span>
            </label>
            <div style={{ display: 'flex', gap: '16px', marginTop: '4px' }}>
              <button
                type="button"
                onClick={() => setSelectedLang('en')}
                className={`btn ${selectedLang === 'en' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '10px 20px', flex: 1 }}
              >
                English 🇬🇧
              </button>
              <button
                type="button"
                onClick={() => setSelectedLang('te')}
                className={`btn ${selectedLang === 'te' ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '10px 20px', flex: 1 }}
              >
                తెలుగు 🇮🇳
              </button>
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '16px', padding: '12px' }}>
            <Save size={18} />
            <span>{loading ? 'Saving...' : t('profile.save_changes')}</span>
          </button>
        </form>
      </div>

      {/* Liked Books Section */}
      <div className="glass-card" style={{ padding: '32px' }}>
        <h3 style={{ fontSize: '1.3rem', fontWeight: 700, marginBottom: '20px', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Heart size={22} color="#ec4899" fill="#ec4899" />
          <span>{t('profile.liked_books')} ({likedBooks.length})</span>
        </h3>

        {likedBooks.length === 0 ? (
          <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem' }}>
            {t('profile.no_liked_books')}
          </p>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))', gap: '20px' }}>
            {likedBooks.map(b => {
              const bookId = b.id || b.bookId;
              const cover = b.coverUrl || b.cover_url;
              return (
                <div
                  key={bookId}
                  onClick={() => navigate(`/books/${bookId}`)}
                  className="glass-card"
                  style={{
                    padding: '16px',
                    borderRadius: '16px',
                    cursor: 'pointer',
                    transition: 'all 0.25s ease',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'space-between',
                    background: 'rgba(255, 255, 255, 0.03)',
                    border: '1px solid var(--border-subtle)'
                  }}
                >
                  <div style={{ marginBottom: '12px' }}>
                    <BookCover
                      coverUrl={cover}
                      title={b.title}
                      author={b.author}
                      genre={b.genre}
                      width="100%"
                      height="200px"
                      borderRadius="12px"
                    />
                  </div>
                  <div>
                    <h4 style={{ fontWeight: 700, fontSize: '1.05rem', marginBottom: '4px', color: 'var(--text-main)' }}>
                      {b.title || 'Untitled Book'}
                    </h4>
                    <p style={{ color: 'var(--accent-cyan)', fontSize: '0.85rem', marginBottom: '8px' }}>
                      by {b.author || 'Unknown Author'}
                    </p>
                    {b.description && (
                      <p style={{
                        color: 'var(--text-muted)',
                        fontSize: '0.8rem',
                        lineHeight: '1.3',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                        marginBottom: '12px'
                      }}>
                        {b.description}
                      </p>
                    )}
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto', paddingTop: '8px' }}>
                    <span style={{
                      background: 'rgba(99, 102, 241, 0.15)',
                      color: '#a5b4fc',
                      border: '1px solid rgba(99, 102, 241, 0.3)',
                      padding: '4px 10px',
                      borderRadius: '12px',
                      fontSize: '0.75rem',
                      fontWeight: 600
                    }}>
                      {b.genre || 'General'}
                    </span>
                    {b.rating && (
                      <span style={{ fontSize: '0.8rem', color: '#f59e0b', fontWeight: 600 }}>
                        ★ {b.rating}
                      </span>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
