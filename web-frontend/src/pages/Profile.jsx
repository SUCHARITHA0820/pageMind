import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { User as UserIcon, Heart, Save, Edit3, BookOpen, LogIn, UserPlus, Sparkles, Mail, Phone, Calendar, UserCheck, Camera, X } from 'lucide-react';
import BookCover from '../components/BookCover';

export default function Profile() {
  const { t } = useTranslation();
  const { user, token, updateProfile } = useAuth();
  const navigate = useNavigate();

  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  const [dob, setDob] = useState(user?.dob || '');
  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber || '');
  const [gender, setGender] = useState(user?.gender || '');
  const [profilePicUrl, setProfilePicUrl] = useState(user?.profilePicUrl || '');
  const [imgLoadFailed, setImgLoadFailed] = useState(false);
  const [likedBooks, setLikedBooks] = useState([]);
  const [loading, setLoading] = useState(false);
  const [savedSuccess, setSavedSuccess] = useState(false);

  useEffect(() => {
    if (user) {
      if (user.name) setName(user.name);
      if (user.email) setEmail(user.email);
      if (user.dob) setDob(user.dob);
      if (user.phoneNumber) setPhoneNumber(user.phoneNumber);
      if (user.gender) setGender(user.gender);
      if (user.profilePicUrl !== undefined) {
        setProfilePicUrl(user.profilePicUrl || '');
        setImgLoadFailed(false);
      }
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
        if (data.email) setEmail(data.email);
        if (data.dob) setDob(data.dob);
        if (data.phoneNumber) setPhoneNumber(data.phoneNumber);
        if (data.gender) setGender(data.gender);
        if (data.profilePicUrl !== undefined) {
          setProfilePicUrl(data.profilePicUrl || '');
          setImgLoadFailed(false);
        }
      }
    } catch (e) {
      // Retain local state
    }
  };

  const fetchLikedBooks = async () => {
    try {
      const res = await fetch('/api/user/likes', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
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
        setLikedBooks(booksList);
      } else {
        setLikedBooks([]);
      }
    } catch (e) {
      setLikedBooks([]);
    }
  };

  const handleSaveProfile = async (e) => {
    e.preventDefault();
    setLoading(true);
    setSavedSuccess(false);

    try {
      await updateProfile({
        name,
        email,
        dob,
        phoneNumber,
        gender,
        profilePicUrl
      });
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
          Profile updated successfully!
        </div>
      )}

      {/* Profile Edit Form Card */}
      <div className="glass-card" style={{ padding: '32px', marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px', marginBottom: '28px' }}>
          {/* Avatar Container */}
          <div style={{
            width: '72px',
            height: '72px',
            borderRadius: '50%',
            background: 'linear-gradient(135deg, #6366f1, #06b6d4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            flexShrink: 0,
            overflow: 'hidden',
            boxShadow: '0 4px 20px rgba(99, 102, 241, 0.35)',
            border: '2px solid rgba(255, 255, 255, 0.2)',
            position: 'relative'
          }}>
            {profilePicUrl && !imgLoadFailed ? (
              <img
                src={profilePicUrl}
                alt="Profile Pic"
                style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                onError={() => setImgLoadFailed(true)}
              />
            ) : (
              <span style={{ fontSize: '2.3rem', userSelect: 'none', lineHeight: 1 }} title="Contact Emoji Fallback">
                👤
              </span>
            )}
          </div>

          <div>
            <h2 style={{ fontSize: '1.4rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span>{user?.name || name || 'PageMind Reader'}</span>
              {!profilePicUrl && (
                <span style={{
                  fontSize: '0.72rem',
                  background: 'rgba(99, 102, 241, 0.2)',
                  color: '#a5b4fc',
                  border: '1px solid rgba(99, 102, 241, 0.3)',
                  padding: '2px 8px',
                  borderRadius: '12px',
                  fontWeight: 500
                }}>
                  Contact Emoji 👤
                </span>
              )}
            </h2>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.92rem' }}>
              {email || user?.email || 'Guest Mode'}
            </p>
          </div>
        </div>

        <form onSubmit={handleSaveProfile}>
          {/* Profile Picture Settings */}
          <div style={{
            background: 'rgba(255, 255, 255, 0.02)',
            border: '1px solid var(--border-subtle)',
            padding: '16px',
            borderRadius: '14px',
            marginBottom: '20px'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px', margin: 0 }}>
                <Camera size={16} color="var(--accent-cyan)" />
                <span style={{ fontWeight: 600, color: 'var(--text-main)' }}>Profile Picture URL</span>
              </label>

              {profilePicUrl && (
                <button
                  type="button"
                  onClick={() => {
                    setProfilePicUrl('');
                    setImgLoadFailed(false);
                  }}
                  className="btn btn-secondary"
                  style={{
                    padding: '3px 10px',
                    fontSize: '0.75rem',
                    borderRadius: '20px'
                  }}
                >
                  Reset to Contact Emoji 👤
                </button>
              )}
            </div>

            {/* Custom URL Input */}
            <div style={{ position: 'relative' }}>
              <input
                type="url"
                className="input-field"
                value={profilePicUrl}
                onChange={(e) => {
                  setProfilePicUrl(e.target.value);
                  setImgLoadFailed(false);
                }}
                placeholder="Enter image URL (or leave blank to show Contact Emoji 👤)"
                style={{ fontSize: '0.88rem', paddingRight: profilePicUrl ? '32px' : '12px' }}
              />
              {profilePicUrl && (
                <button
                  type="button"
                  onClick={() => {
                    setProfilePicUrl('');
                    setImgLoadFailed(false);
                  }}
                  style={{
                    position: 'absolute',
                    right: '10px',
                    top: '50%',
                    transform: 'translateY(-50%)',
                    background: 'none',
                    border: 'none',
                    color: 'var(--text-muted)',
                    cursor: 'pointer',
                    padding: '4px'
                  }}
                >
                  <X size={14} />
                </button>
              )}
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', marginBottom: '16px' }}>
            {/* Full Name */}
            <div className="input-group" style={{ marginBottom: 0 }}>
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

            {/* Email Address */}
            <div className="input-group" style={{ marginBottom: 0 }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Mail size={16} />
                <span>Email Address</span>
              </label>
              <input
                type="email"
                required
                className="input-field"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@example.com"
              />
            </div>

            {/* Phone Number */}
            <div className="input-group" style={{ marginBottom: 0 }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Phone size={16} />
                <span>Phone Number</span>
              </label>
              <input
                type="tel"
                className="input-field"
                value={phoneNumber}
                onChange={(e) => setPhoneNumber(e.target.value)}
                placeholder="+1 (555) 000-0000"
              />
            </div>

            {/* Date of Birth */}
            <div className="input-group" style={{ marginBottom: 0 }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Calendar size={16} />
                <span>Date of Birth</span>
              </label>
              <input
                type="date"
                className="input-field"
                value={dob}
                onChange={(e) => setDob(e.target.value)}
                style={{ colorScheme: 'dark' }}
              />
            </div>

            {/* Gender */}
            <div className="input-group" style={{ marginBottom: 0, gridColumn: '1 / -1' }}>
              <label className="input-label" style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                <UserCheck size={16} />
                <span>Gender</span>
              </label>
              <select
                className="input-field"
                value={gender}
                onChange={(e) => setGender(e.target.value)}
                style={{
                  background: 'var(--bg-input, rgba(255, 255, 255, 0.05))',
                  color: 'var(--text-main)',
                  cursor: 'pointer'
                }}
              >
                <option value="" style={{ background: '#1e1b4b' }}>Select Gender</option>
                <option value="Female" style={{ background: '#1e1b4b' }}>Female</option>
                <option value="Male" style={{ background: '#1e1b4b' }}>Male</option>
                <option value="Non-Binary" style={{ background: '#1e1b4b' }}>Non-Binary</option>
                <option value="Prefer not to say" style={{ background: '#1e1b4b' }}>Prefer not to say</option>
              </select>
            </div>
          </div>

          <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '16px', padding: '12px' }}>
            <Save size={18} />
            <span>{loading ? 'Saving...' : t('profile.save_changes', 'Save Changes')}</span>
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
