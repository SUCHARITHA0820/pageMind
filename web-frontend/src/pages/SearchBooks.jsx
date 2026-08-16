import React, { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { Search, BookOpen, ArrowRight, Frown, ChevronDown, Filter, Heart } from 'lucide-react';
import BookCover from '../components/BookCover';
import { getFallbackBooks } from '../data/fallbackBooks';

const ALL_GENRES = [
  'Fiction',
  'Science Fiction',
  'Romance',
  'Fantasy',
  'Mystery',
  'Non-Fiction',
  'Self-Help',
  'Classic Literature',
  'Thriller',
  'Dystopian',
  'Biography',
  'History',
  'Poetry',
  'Graphic Novel',
  'Young Adult',
  'Horror',
  'Philosophy'
];

export default function SearchBooks() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { token } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();

  const initialQuery = searchParams.get('search') || searchParams.get('q') || '';
  const initialGenre = searchParams.get('genre') || '';

  const [query, setQuery] = useState(initialQuery);
  const [selectedGenre, setSelectedGenre] = useState(initialGenre);
  const [books, setBooks] = useState([]);
  const [likedBookIds, setLikedBookIds] = useState(new Set());
  const [loading, setLoading] = useState(true);
  const [displayLimit, setDisplayLimit] = useState(50);

  // Fetch liked books when token is available
  useEffect(() => {
    if (token) {
      fetchUserLikes();
    }
  }, [token]);

  const fetchUserLikes = async () => {
    try {
      const res = await fetch('/api/user/likes', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          const ids = new Set(data.map(b => Number(b.id || b.bookId)));
          setLikedBookIds(ids);
        }
      }
    } catch (e) {
      console.error("[SearchBooks] Failed to fetch user likes:", e);
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
    const updatedLikes = new Set(likedBookIds);
    if (isLiked) {
      updatedLikes.delete(numericId);
    } else {
      updatedLikes.add(numericId);
    }
    setLikedBookIds(updatedLikes);

    try {
      const method = isLiked ? 'DELETE' : 'POST';
      const res = await fetch(`/api/user/likes/${bookId}`, {
        method,
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      if (!res.ok) {
        if (res.status === 401) {
          alert("Your session has expired. Please log in again.");
          navigate('/login');
        } else {
          alert(data.message || "Failed to update like status.");
        }
        setLikedBookIds(likedBookIds);
      }
    } catch (err) {
      console.error(`[SearchBooks] Error toggling like for bookId ${bookId}:`, err);
      setLikedBookIds(likedBookIds);
    }
  };

  // Sync state when URL params change
  useEffect(() => {
    const qParam = searchParams.get('search') || searchParams.get('q') || '';
    const gParam = searchParams.get('genre') || '';
    setQuery(qParam);
    setSelectedGenre(gParam);
  }, [searchParams]);

  useEffect(() => {
    fetchSearchResults(query, selectedGenre);
    setDisplayLimit(50);
  }, [query, selectedGenre]);

  const updateUrlParams = (newQuery, newGenre) => {
    const params = {};
    if (newQuery.trim()) params.search = newQuery.trim();
    if (newGenre.trim()) params.genre = newGenre.trim();
    setSearchParams(params);
  };

  const handleQueryChange = (e) => {
    const val = e.target.value;
    setQuery(val);
    updateUrlParams(val, selectedGenre);
  };

  const handleGenreSelect = (genreName) => {
    const newGenre = selectedGenre === genreName ? '' : genreName;
    setSelectedGenre(newGenre);
    updateUrlParams(query, newGenre);
  };

  const fetchSearchResults = async (searchQuery, genreFilter) => {
    setLoading(true);
    try {
      const params = new URLSearchParams();
      params.append('size', '500'); // Fetch full catalog matching query/genre
      if (searchQuery && searchQuery.trim()) {
        params.append('search', searchQuery.trim());
      }
      if (genreFilter && genreFilter.trim()) {
        params.append('genre', genreFilter.trim());
      }

      const url = `/api/books?${params.toString()}`;
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        if (data && Array.isArray(data.content)) {
          setBooks(data.content);
          return;
        } else if (Array.isArray(data)) {
          setBooks(data);
          return;
        }
      }
      throw new Error(`Server status ${res.status}`);
    } catch (e) {
      console.warn("Search API unavailable, using fallback dataset:", e);
      setBooks(getFallbackBooks(genreFilter, searchQuery));
    } finally {
      setLoading(false);
    }
  };

  const visibleBooks = books.slice(0, displayLimit);

  return (
    <div className="container page-container" style={{ maxWidth: '960px' }}>
      
      {/* Search Header */}
      <div style={{ textAlign: 'center', marginBottom: '28px' }}>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '10px' }} className="gradient-text">
          Search Books Catalog
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '600px', margin: '0 auto 20px' }}>
          Explore all books by title, author, or genre across our full catalog.
        </p>

        {/* Top Search Input Bar */}
        <div style={{ position: 'relative', maxWidth: '680px', margin: '0 auto 20px' }}>
          <Search size={22} color="#a5b4fc" style={{ position: 'absolute', left: '18px', top: '16px' }} />
          <input
            type="text"
            className="input-field"
            style={{
              paddingLeft: '54px',
              paddingRight: '20px',
              height: '54px',
              fontSize: '1.05rem',
              borderRadius: '28px',
              boxShadow: '0 8px 24px rgba(0,0,0,0.3)'
            }}
            placeholder="Search by book title or author name..."
            value={query}
            onChange={handleQueryChange}
          />
        </div>

        {/* Genre Filter Pills */}
        <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', flexWrap: 'wrap', maxWidth: '920px', margin: '0 auto' }}>
          <button
            onClick={() => handleGenreSelect('')}
            className={`btn ${selectedGenre === '' ? 'btn-primary' : 'btn-secondary'}`}
            style={{ padding: '6px 14px', fontSize: '0.82rem', borderRadius: '20px' }}
          >
            All Genres
          </button>
          {ALL_GENRES.map((g) => (
            <button
              key={g}
              onClick={() => handleGenreSelect(g)}
              className={`btn ${selectedGenre === g ? 'btn-primary' : 'btn-secondary'}`}
              style={{ padding: '6px 14px', fontSize: '0.82rem', borderRadius: '20px' }}
            >
              {g}
            </button>
          ))}
        </div>
      </div>

      {/* Results Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-muted)' }}>
          {selectedGenre ? `Genre: "${selectedGenre}"` : query ? `Search Results for "${query}"` : 'All Catalog Books'} 
          {' '}(<strong style={{ color: '#fff' }}>{visibleBooks.length}</strong> of <strong style={{ color: 'var(--accent-cyan)' }}>{books.length}</strong> books)
        </h3>
        {loading ? (
          <span style={{ fontSize: '0.88rem', color: 'var(--accent-cyan)' }}>Loading catalog...</span>
        ) : books.length > visibleBooks.length ? (
          <button
            onClick={() => setDisplayLimit(books.length)}
            style={{ background: 'none', border: 'none', color: 'var(--accent-cyan)', fontSize: '0.88rem', cursor: 'pointer', fontWeight: 600 }}
          >
            Show All ({books.length})
          </button>
        ) : null}
      </div>

      {/* Empty State Message */}
      {books.length === 0 && !loading && (
        <div className="glass-card" style={{ padding: '48px', textAlign: 'center', margin: '20px 0' }}>
          <div style={{
            width: '64px',
            height: '64px',
            borderRadius: '50%',
            background: 'rgba(239, 68, 68, 0.15)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            marginBottom: '16px'
          }}>
            <Frown size={32} color="#fca5a5" />
          </div>
          <h3 style={{ fontSize: '1.4rem', fontWeight: 700, marginBottom: '8px' }}>
            No matching books found
          </h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.94rem', maxWidth: '420px', margin: '0 auto' }}>
            We couldn't find any books matching your selection{selectedGenre ? ` in "${selectedGenre}"` : ''}. Try selecting a different genre or clearing your search.
          </p>
        </div>
      )}

      {/* Scrollable Book Cards List */}
      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {visibleBooks.map((book) => (
          <div
            key={book.id}
            className="glass-card"
            onClick={() => navigate(`/books/${book.id}`)}
            style={{
              padding: '20px 24px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              cursor: 'pointer',
              gap: '20px'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flex: 1 }}>
              {/* Thumbnail Cover Image */}
              <BookCover
                coverUrl={book.coverUrl || book.cover_url}
                title={book.title}
                author={book.author}
                genre={book.genre}
                width="56px"
                height="76px"
                borderRadius="8px"
                showTitleFallback={false}
              />

              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                  <h3 style={{ fontSize: '1.2rem', fontWeight: 700 }}>
                    {book.title}
                  </h3>
                  <span style={{
                    background: 'rgba(99, 102, 241, 0.15)',
                    color: '#a5b4fc',
                    border: '1px solid rgba(99, 102, 241, 0.3)',
                    padding: '2px 10px',
                    borderRadius: '12px',
                    fontSize: '0.75rem',
                    fontWeight: 600
                  }}>
                    {book.genre}
                  </span>
                </div>
                <p style={{ color: 'var(--accent-cyan)', fontSize: '0.9rem', fontWeight: 500, marginBottom: '6px' }}>
                  by {book.author}
                </p>
                <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', lineHeight: '1.4', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>
                  {book.description}
                </p>
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '14px', flexShrink: 0 }}>
              <button
                onClick={(e) => toggleLike(e, book.id)}
                style={{
                  background: likedBookIds.has(Number(book.id)) ? 'rgba(236, 72, 153, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                  border: likedBookIds.has(Number(book.id)) ? '1px solid #ec4899' : '1px solid var(--card-border)',
                  borderRadius: '50%',
                  width: '36px',
                  height: '36px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  transition: 'all 0.2s ease',
                  cursor: 'pointer'
                }}
                title={likedBookIds.has(Number(book.id)) ? 'Unlike book' : 'Like book'}
              >
                <Heart size={18} color={likedBookIds.has(Number(book.id)) ? '#ec4899' : '#94a3b8'} fill={likedBookIds.has(Number(book.id)) ? '#ec4899' : 'none'} />
              </button>

              <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--accent-cyan)', fontSize: '0.88rem', fontWeight: 600 }}>
                <span>View Details</span>
                <ArrowRight size={16} />
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Load More Button */}
      {books.length > visibleBooks.length && (
        <div style={{ textAlign: 'center', marginTop: '32px', marginBottom: '20px' }}>
          <button
            onClick={() => setDisplayLimit((prev) => prev + 50)}
            className="btn btn-primary"
            style={{ padding: '12px 32px', fontSize: '1rem', display: 'inline-flex', alignItems: 'center', gap: '8px', borderRadius: '30px' }}
          >
            Load More Books ({books.length - visibleBooks.length} remaining)
            <ChevronDown size={18} />
          </button>
        </div>
      )}

    </div>
  );
}
