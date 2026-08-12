import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Search, BookOpen, ArrowRight, Frown, ChevronDown } from 'lucide-react';
import BookCover from '../components/BookCover';

export default function SearchBooks() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [query, setQuery] = useState('');
  const [books, setBooks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [displayLimit, setDisplayLimit] = useState(15);

  useEffect(() => {
    fetchSearchResults(query);
  }, [query]);

  useEffect(() => {
    setDisplayLimit(15);
  }, [query]);

  const fetchSearchResults = async (searchQuery) => {
    setLoading(true);
    try {
      const url = searchQuery.trim() ? `/api/books?search=${encodeURIComponent(searchQuery.trim())}` : '/api/books';
      const res = await fetch(url);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          setBooks(data);
        }
      }
    } catch (e) {
      console.error("Failed to search books:", e);
    } finally {
      setLoading(false);
    }
  };

  const visibleBooks = books.slice(0, displayLimit);

  return (
    <div className="container page-container" style={{ maxWidth: '900px' }}>
      
      {/* Search Header */}
      <div style={{ textAlign: 'center', marginBottom: '32px' }}>
        <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '10px' }} className="gradient-text">
          Search Books Catalog
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '1rem', maxWidth: '560px', margin: '0 auto 24px' }}>
          Find books by title or author across our 500+ title collection in real time.
        </p>

        {/* Top Search Input Bar */}
        <div style={{ position: 'relative', maxWidth: '680px', margin: '0 auto' }}>
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
            onChange={(e) => setQuery(e.target.value)}
          />
        </div>
      </div>

      {/* Results Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h3 style={{ fontSize: '1.2rem', fontWeight: 700, color: 'var(--text-muted)' }}>
          {query ? `Search Results for "${query}"` : 'All Catalog Books'} ({visibleBooks.length} of {books.length})
        </h3>
        {loading ? (
          <span style={{ fontSize: '0.88rem', color: 'var(--accent-cyan)' }}>Searching...</span>
        ) : books.length > visibleBooks.length ? (
          <button
            onClick={() => setDisplayLimit(books.length)}
            style={{ background: 'none', border: 'none', color: 'var(--accent-cyan)', fontSize: '0.85rem', cursor: 'pointer', fontWeight: 600 }}
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
            We couldn't find any books matching "{query}". Try checking for spelling errors or searching by a different author or genre.
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
              {/* Thumbnail Cover Image with Fallback */}
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

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: 'var(--accent-cyan)', fontSize: '0.88rem', fontWeight: 600, flexShrink: 0 }}>
              <span>View Details</span>
              <ArrowRight size={16} />
            </div>
          </div>
        ))}
      </div>

      {/* Load More Button */}
      {books.length > visibleBooks.length && (
        <div style={{ textAlign: 'center', marginTop: '32px', marginBottom: '20px' }}>
          <button
            onClick={() => setDisplayLimit(prev => prev + 20)}
            className="btn btn-primary"
            style={{ padding: '12px 32px', fontSize: '1rem', display: 'inline-flex', alignItems: 'center', gap: '8px', borderRadius: '30px' }}
          >
            Load More Results ({books.length - visibleBooks.length} remaining)
            <ChevronDown size={18} />
          </button>
        </div>
      )}

    </div>
  );
}
