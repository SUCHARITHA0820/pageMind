import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { Heart, Search, ChevronLeft, ChevronRight, WifiOff } from 'lucide-react';
import BookCover from '../components/BookCover';
import { getFallbackBooks } from '../data/fallbackBooks';

const ALL_GENRES = [
  'Fiction', 'Mystery', 'Thriller', 'Romance', 'Fantasy',
  'Science Fiction', 'Horror', 'Self-Help', 'Biography', 'History',
  'Philosophy', 'Young Adult', 'Poetry', 'Classic Literature', 'Non-Fiction'
];

export default function Home() {
  const { t } = useTranslation();
  const { token } = useAuth();
  const navigate = useNavigate();

  const [books, setBooks] = useState([]);
  const [likedBookIds, setLikedBookIds] = useState(new Set());
  const [selectedGenre, setSelectedGenre] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize] = useState(20);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(true);
  const [isOffline, setIsOffline] = useState(false);

  useEffect(() => {
    fetchBooks(currentPage, selectedGenre, searchQuery);
  }, [currentPage, selectedGenre, searchQuery]);

  useEffect(() => {
    if (token) {
      fetchUserLikes();
    }
  }, [token]);

  const handleGenreChange = (genre) => {
    setSelectedGenre(genre);
    setCurrentPage(0);
  };

  const handleSearchChange = (e) => {
    setSearchQuery(e.target.value);
    setCurrentPage(0);
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < totalPages) {
      setCurrentPage(newPage);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    }
  };

  const fetchBooks = async (page, genre, search) => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.append('page', page);
      params.append('size', pageSize);
      if (genre) params.append('genre', genre);
      if (search) params.append('search', search);

      const url = `/api/books?${params.toString()}`;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        if (data && Array.isArray(data.content)) {
          setBooks(data.content);
          setTotalElements(data.totalElements || 0);
          setTotalPages(data.totalPages || 0);
          setCurrentPage(data.currentPage !== undefined ? data.currentPage : page);
          setIsOffline(false);
          return;
        } else if (Array.isArray(data)) {
          setBooks(data);
          setTotalElements(data.length);
          setTotalPages(1);
          setCurrentPage(0);
          setIsOffline(false);
          return;
        }
      }
      throw new Error(`Server returned ${res.status}`);
    } catch (e) {
      console.warn("API unavailable, loading fallback catalog:", e);
      const fallbacks = getFallbackBooks(genre, search);
      setBooks(fallbacks);
      setTotalElements(fallbacks.length);
      setTotalPages(1);
      setCurrentPage(0);
      setIsOffline(true);
    } finally {
      setLoading(false);
    }
  };

  const fetchUserLikes = async () => {
    try {
      console.log('[Home] Fetching user likes with token:', token ? '[PRESENT]' : '[NULL]');
      const res = await fetch('/api/user/likes', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      console.log(`[Home] GET /api/user/likes status: ${res.status}`);
      if (res.ok) {
        const data = await res.json();
        console.log('[Home] GET /api/user/likes returned payload:', data);
        if (Array.isArray(data)) {
          const ids = new Set(data.map(b => Number(b.id || b.bookId)));
          setLikedBookIds(ids);
        }
      } else {
        console.error(`[Home] GET /api/user/likes failed with status ${res.status}`);
      }
    } catch (e) {
      console.error("[Home] Failed to fetch user likes:", e);
    }
  };

  const toggleLike = async (e, bookId) => {
    e.stopPropagation();
    const numericId = Number(bookId);

    if (!token) {
      alert("Please log in to like books and save them to your profile!");
      navigate('/login');
      return;
    }

    const isLiked = likedBookIds.has(numericId);
    console.log(`[Home] Toggling like for bookId ${numericId}. Action: ${isLiked ? 'UNLIKE' : 'LIKE'}`);

    // Optimistic UI update
    const updatedLikes = new Set(likedBookIds);
    if (isLiked) {
      updatedLikes.delete(numericId);
    } else {
      updatedLikes.add(numericId);
    }
    setLikedBookIds(updatedLikes);

    try {
      const method = isLiked ? 'DELETE' : 'POST';
      console.log(`[Home] Sending ${method} /api/user/likes/${bookId} with Authorization header attached`);
      const res = await fetch(`/api/user/likes/${bookId}`, {
        method,
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      console.log(`[Home] ${method} /api/user/likes/${bookId} status: ${res.status}`, data);
      if (!res.ok) {
        if (res.status === 401) {
          alert("Your session has expired. Please log in again.");
          navigate('/login');
        } else {
          alert(data.message || "Failed to update like status.");
        }
        console.error(`[Home] ${method} /api/user/likes/${bookId} failed, reverting UI`);
        setLikedBookIds(likedBookIds);
      }
    } catch (err) {
      console.error(`[Home] Error toggling like for bookId ${bookId}:`, err);
      setLikedBookIds(likedBookIds);
    }
  };

  const startRange = totalElements === 0 ? 0 : currentPage * pageSize + 1;
  const endRange = Math.min((currentPage + 1) * pageSize, totalElements);

  const getPageNumbers = () => {
    if (totalPages <= 7) {
      return Array.from({ length: totalPages }, (_, i) => i);
    }
    const pages = [];
    if (currentPage <= 3) {
      pages.push(0, 1, 2, 3, 4, '...', totalPages - 1);
    } else if (currentPage >= totalPages - 4) {
      pages.push(0, '...', totalPages - 5, totalPages - 4, totalPages - 3, totalPages - 2, totalPages - 1);
    } else {
      pages.push(0, '...', currentPage - 1, currentPage, currentPage + 1, '...', totalPages - 1);
    }
    return pages;
  };

  return (
    <div className="container page-container">
      {/* Hero Header */}
      <div style={{ textAlign: 'center', margin: '20px 0 35px' }}>
        <h1 style={{ fontSize: '2.5rem', fontWeight: 800, marginBottom: '12px' }} className="gradient-text">
          {t('home.hero_title', 'Explore PageMind Book Catalog')}
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1.1rem', maxWidth: '600px', margin: '0 auto 25px' }}>
          {t('home.hero_subtitle', 'Discover 5000+ curated titles across 15 genres, powered by AI mood recommendation.')}
        </p>

        {/* Search Input */}
        <div style={{ display: 'flex', gap: '16px', maxWidth: '700px', margin: '0 auto 20px', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '260px', position: 'relative' }}>
            <Search size={18} color="#94a3b8" style={{ position: 'absolute', left: '16px', top: '14px' }} />
            <input
              type="text"
              className="input-field"
              style={{ paddingLeft: '44px' }}
              placeholder={t('home.search_placeholder', 'Search books by title, author, or genre...')}
              value={searchQuery}
              onChange={handleSearchChange}
            />
          </div>
        </div>

        {/* Genre Filters */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', flexWrap: 'wrap', maxWidth: '900px', margin: '0 auto' }}>
          <button
            onClick={() => handleGenreChange('')}
            className={`btn ${selectedGenre === '' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '6px 14px', fontSize: '0.82rem', borderRadius: '20px' }}
          >
            {t('home.filter_all', 'All Genres')}
          </button>
          {ALL_GENRES.map(g => (
            <button
              key={g}
              onClick={() => handleGenreChange(g)}
              className={`btn ${selectedGenre === g ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '6px 14px', fontSize: '0.82rem', borderRadius: '20px' }}
            >
              {g}
            </button>
          ))}
        </div>
      </div>

      {/* Catalog Status Bar */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', padding: '0 4px' }}>
        <span style={{ fontSize: '0.92rem', color: 'var(--text-muted)', fontWeight: 500 }}>
          Showing <strong style={{ color: 'var(--accent-cyan)' }}>{startRange}-{endRange}</strong> of <strong style={{ color: '#fff' }}>{totalElements}</strong> books
          {selectedGenre && <span> in <strong style={{ color: '#a5b4fc' }}>{selectedGenre}</strong></span>}
          {searchQuery && <span> matching "<strong style={{ color: '#a5b4fc' }}>{searchQuery}</strong>"</span>}
        </span>
      </div>

      {/* Book Cards Grid */}
      {loading ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
          Loading book catalog...
        </div>
      ) : books.length === 0 ? (
        <div style={{ textAlign: 'center', padding: '60px 0', color: 'var(--text-muted)' }}>
          No books found matching your filter criteria.
        </div>
      ) : (
        <>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '24px' }}>
            {books.map(book => {
              const isLiked = likedBookIds.has(book.id);
              const cover = book.coverUrl || book.cover_url;
              return (
                <div
                  key={book.id}
                  className="glass-card"
                  onClick={() => navigate(`/books/${book.id}`)}
                  style={{ padding: '20px', display: 'flex', gap: '16px', cursor: 'pointer', flexDirection: 'row', alignItems: 'stretch' }}
                >
                  {/* Book Cover Image Container */}
                  <div style={{ flexShrink: 0 }}>
                    <BookCover
                      coverUrl={cover}
                      title={book.title}
                      author={book.author}
                      genre={book.genre}
                      width="105px"
                      height="155px"
                      borderRadius="10px"
                    />
                  </div>

                  {/* Book Details */}
                  <div style={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'space-between', minWidth: 0 }}>
                    <div>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px', gap: '8px' }}>
                        <span style={{
                          background: 'rgba(99, 102, 241, 0.15)',
                          color: '#a5b4fc',
                          border: '1px solid rgba(99, 102, 241, 0.3)',
                          padding: '3px 8px',
                          borderRadius: '10px',
                          fontSize: '0.75rem',
                          fontWeight: 600,
                          whiteSpace: 'nowrap'
                        }}>
                          {book.genre}
                        </span>

                        {/* Heart / Like Icon */}
                        <button
                          onClick={(e) => toggleLike(e, book.id)}
                          style={{
                            background: isLiked ? 'rgba(236, 72, 153, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                            border: isLiked ? '1px solid #ec4899' : '1px solid var(--card-border)',
                            borderRadius: '50%',
                            width: '32px',
                            height: '32px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            transition: 'all 0.2s ease',
                            cursor: 'pointer',
                            flexShrink: 0
                          }}
                        >
                          <Heart size={16} color={isLiked ? '#ec4899' : '#94a3b8'} fill={isLiked ? '#ec4899' : 'none'} />
                        </button>
                      </div>

                      <h3 style={{
                        fontSize: '1.05rem',
                        fontWeight: 700,
                        marginBottom: '4px',
                        lineHeight: '1.3',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden'
                      }}>
                        {book.title}
                      </h3>
                      <p style={{
                        color: 'var(--accent-cyan)',
                        fontSize: '0.82rem',
                        fontWeight: 500,
                        marginBottom: '8px',
                        display: '-webkit-box',
                        WebkitLineClamp: 1,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden'
                      }}>
                        by {book.author}
                      </p>
                      <p style={{
                        color: 'var(--text-muted)',
                        fontSize: '0.8rem',
                        lineHeight: '1.4',
                        display: '-webkit-box',
                        WebkitLineClamp: 2,
                        WebkitBoxOrient: 'vertical',
                        overflow: 'hidden',
                        marginBottom: '10px'
                      }}>
                        {book.description}
                      </p>
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '8px', borderTop: '1px solid var(--border-subtle)' }}>
                      <span style={{ fontSize: '0.78rem', color: isLiked ? '#ec4899' : 'var(--text-muted)', fontWeight: 600 }}>
                        {isLiked ? t('home.liked', 'Liked') : t('home.like', 'Like')}
                      </span>
                      <span style={{ fontSize: '0.78rem', color: 'var(--accent-cyan)', fontWeight: 600 }}>View →</span>
                    </div>
                  </div>
                </div>
              );
            })}
          </div>

          {/* Numbered Pagination Controls */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '40px', marginBottom: '30px', flexWrap: 'wrap' }}>
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 0}
                className="btn btn-secondary"
                style={{ padding: '8px 14px', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '4px', opacity: currentPage === 0 ? 0.5 : 1, cursor: currentPage === 0 ? 'not-allowed' : 'pointer' }}
              >
                <ChevronLeft size={16} /> Previous
              </button>

              {getPageNumbers().map((p, idx) => (
                typeof p === 'number' ? (
                  <button
                    key={p}
                    onClick={() => handlePageChange(p)}
                    className={`btn ${currentPage === p ? 'btn-primary' : 'btn-secondary'}`}
                    style={{ width: '38px', height: '38px', padding: 0, borderRadius: '8px', fontWeight: currentPage === p ? '700' : '500' }}
                  >
                    {p + 1}
                  </button>
                ) : (
                  <span key={`dots-${idx}`} style={{ color: 'var(--text-muted)', padding: '0 4px' }}>
                    ...
                  </span>
                )
              ))}

              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage >= totalPages - 1}
                className="btn btn-secondary"
                style={{ padding: '8px 14px', borderRadius: '8px', display: 'flex', alignItems: 'center', gap: '4px', opacity: currentPage >= totalPages - 1 ? 0.5 : 1, cursor: currentPage >= totalPages - 1 ? 'not-allowed' : 'pointer' }}
              >
                Next <ChevronRight size={16} />
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}
