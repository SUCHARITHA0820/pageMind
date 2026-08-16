import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { ArrowLeft, Heart, BookOpen, ShoppingCart, ExternalLink, Compass } from 'lucide-react';
import BookCover from '../components/BookCover';

const FALLBACK_BOOKS = [
  { id: 1, title: 'Dune', author: 'Frank Herbert', genre: 'Sci-Fi', description: 'Set on the desert planet Arrakis, Dune is the story of the boy Paul Atreides, heir to a noble family tasked with ruling an inhospitable world where the only thing of value is the spice melange, a drug capable of extending human life and enhancing mental abilities.' },
  { id: 2, title: 'The Seven Husbands of Evelyn Hugo', author: 'Taylor Jenkins Reid', genre: 'Romance', description: 'Aging and reclusive Hollywood movie icon Evelyn Hugo is finally ready to tell the truth about her glamorous and scandalous life. But when she chooses unknown magazine reporter Monique Grant for the job, no one is more astounded than Monique herself.' },
  { id: 3, title: 'The Silent Patient', author: 'Alex Michaelides', genre: 'Mystery', description: 'Alicia Berenson’s life is seemingly perfect. A famous painter married to an in-demand fashion photographer, she lives in a grand house with big windows overlooking a park in one of London’s most desirable areas. One evening her husband Gabriel returns home late and Alicia shoots him five times in the face, and then never speaks another word.' },
  { id: 4, title: 'Atomic Habits', author: 'James Clear', genre: 'Self-Help', description: 'No matter your goals, Atomic Habits offers a proven framework for improving every day. James Clear, one of the world’s leading experts on habit formation, reveals practical strategies that will teach you exactly how to form good habits, break bad ones, and master the tiny behaviors that lead to remarkable results.' },
  { id: 5, title: 'Project Hail Mary', author: 'Andy Weir', genre: 'Sci-Fi', description: 'Ryland Grace is the sole survivor on a desperate, last-chance mission—and if he fails, humanity and the earth itself will perish. Except that right now, he doesn’t know that. He can’t even remember his own name, let alone the nature of his assignment or how to complete it.' },
  { id: 6, title: 'The Name of the Wind', author: 'Patrick Rothfuss', genre: 'Fantasy', description: 'Told in Kvothe’s own voice, this is the tale of the magically gifted young man who grows to be the most notorious wizard his world has ever seen. The intimate portrait of an artist, an orphan, a musician, a thief, a murderer, and a hero.' }
];

export default function BookDetails() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const { token } = useAuth();

  const [book, setBook] = useState(null);
  const [isLiked, setIsLiked] = useState(false);
  const [loading, setLoading] = useState(true);
  const [imgError, setImgError] = useState(false);

  useEffect(() => {
    setImgError(false);
    fetchBookDetails();
    if (token) {
      checkLikedStatus();
    }
  }, [id, token]);

  const fetchBookDetails = async () => {
    setLoading(true);
    try {
      const res = await fetch(`/api/books/${id}`);
      if (res.ok) {
        const data = await res.json();
        setBook(data);
      } else {
        throw new Error('Not found');
      }
    } catch (e) {
      // Fallback matching lookup
      const found = FALLBACK_BOOKS.find(b => String(b.id) === String(id)) || FALLBACK_BOOKS[0];
      setBook(found);
    } finally {
      setLoading(false);
    }
  };

  const checkLikedStatus = async () => {
    try {
      console.log(`[BookDetails] Checking liked status for bookId ${id}`);
      const res = await fetch('/api/user/likes', {
        headers: { 'Authorization': `Bearer ${token}` }
      });
      console.log(`[BookDetails] GET /api/user/likes status: ${res.status}`);
      if (res.ok) {
        const data = await res.json();
        if (Array.isArray(data)) {
          const liked = data.some(b => String(b.id || b.bookId) === String(id));
          console.log(`[BookDetails] BookId ${id} is liked: ${liked}`);
          setIsLiked(liked);
        }
      }
    } catch (e) {
      console.error(`[BookDetails] Error checking liked status for bookId ${id}:`, e);
    }
  };

  const toggleLike = async () => {
    if (!token) {
      alert("Please log in to like books and save them to your profile!");
      navigate('/login');
      return;
    }
    const nextState = !isLiked;
    setIsLiked(nextState);
    console.log(`[BookDetails] Toggle like for bookId ${id}. Next state: ${nextState ? 'LIKED' : 'UNLIKED'}`);

    try {
      const method = nextState ? 'POST' : 'DELETE';
      console.log(`[BookDetails] Sending ${method} /api/user/likes/${id} with Authorization header attached`);
      const res = await fetch(`/api/user/likes/${id}`, {
        method,
        headers: { 'Authorization': `Bearer ${token}` }
      });
      const data = await res.json();
      console.log(`[BookDetails] ${method} /api/user/likes/${id} status: ${res.status}`, data);
      if (!res.ok) {
        if (res.status === 401) {
          alert("Your session has expired. Please log in again.");
          navigate('/login');
        } else {
          alert(data.message || "Failed to update like status.");
        }
        console.error(`[BookDetails] ${method} /api/user/likes/${id} failed, reverting state`);
        setIsLiked(!nextState);
      }
    } catch (e) {
      console.error(`[BookDetails] Error toggling like for bookId ${id}:`, e);
      setIsLiked(!nextState);
    }
  };

  if (loading) {
    return (
      <div className="container page-container" style={{ alignItems: 'center', justifyContent: 'center' }}>
        <p style={{ color: 'var(--text-muted)' }}>Loading book details...</p>
      </div>
    );
  }

  if (!book) {
    return (
      <div className="container page-container">
        <button onClick={() => navigate(-1)} className="btn btn-secondary" style={{ marginBottom: '20px' }}>
          <ArrowLeft size={16} /> Back
        </button>
        <p style={{ color: 'var(--text-muted)' }}>Book not found.</p>
      </div>
    );
  }

  // Constructed retailer search URLs
  const amazonUrl = `https://www.amazon.in/s?k=${encodeURIComponent(`${book.title} ${book.author || ''}`)}`;
  const flipkartUrl = `https://www.flipkart.com/search?q=${encodeURIComponent(`${book.title} ${book.author || ''}`)}`;

  return (
    <div className="container page-container" style={{ maxWidth: '880px' }}>
      {/* Back Button */}
      <button
        onClick={() => navigate(-1)}
        className="btn btn-secondary"
        style={{ marginBottom: '24px', padding: '8px 16px', borderRadius: '20px', width: 'fit-content' }}
      >
        <ArrowLeft size={16} />
        <span>Back to Previous Page</span>
      </button>

      {/* Main Glass Card */}
      <div className="glass-card animate-fade-in" style={{ padding: '36px' }}>
        <div style={{ display: 'flex', gap: '32px', flexWrap: 'wrap' }}>
          
          {/* Cover Art Visual Container */}
          <BookCover
            coverUrl={book.coverUrl || book.cover_url}
            title={book.title}
            author={book.author}
            genre={book.genre}
            width="200px"
            height="280px"
            borderRadius="16px"
          />


          {/* Details Content */}
          <div style={{ flex: 1, minWidth: '280px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '14px' }}>
                <span style={{
                  background: 'rgba(99, 102, 241, 0.15)',
                  color: '#a5b4fc',
                  border: '1px solid rgba(99, 102, 241, 0.3)',
                  padding: '6px 14px',
                  borderRadius: '20px',
                  fontSize: '0.85rem',
                  fontWeight: 600,
                  display: 'inline-flex',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  <Compass size={14} />
                  <span>{book.genre}</span>
                </span>

                {/* Heart / Like Icon */}
                <button
                  onClick={toggleLike}
                  style={{
                    background: isLiked ? 'rgba(236, 72, 153, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                    border: isLiked ? '1px solid #ec4899' : '1px solid var(--card-border)',
                    borderRadius: '50%',
                    width: '44px',
                    height: '44px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                >
                  <Heart size={22} color={isLiked ? '#ec4899' : '#94a3b8'} fill={isLiked ? '#ec4899' : 'none'} />
                </button>
              </div>

              <h1 style={{ fontSize: '2.2rem', fontWeight: 800, marginBottom: '6px' }}>
                {book.title}
              </h1>
              <p style={{ color: 'var(--accent-cyan)', fontSize: '1.1rem', fontWeight: 600, marginBottom: '20px' }}>
                by {book.author}
              </p>

              <h3 style={{ fontSize: '1.05rem', fontWeight: 700, color: 'var(--text-muted)', marginBottom: '8px' }}>
                Synopsis & Description
              </h3>
              <p style={{ color: 'var(--text-main)', fontSize: '0.96rem', lineHeight: '1.7', marginBottom: '28px' }}>
                {book.description}
              </p>
            </div>

            {/* Retailer Action Buttons */}
            <div style={{ paddingTop: '20px', borderTop: '1px solid var(--border-subtle)', display: 'flex', gap: '14px', flexWrap: 'wrap' }}>
              <a
                href={amazonUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-primary"
                style={{ padding: '12px 20px', fontSize: '0.9rem' }}
              >
                <ShoppingCart size={16} />
                <span>Buy on Amazon</span>
                <ExternalLink size={14} />
              </a>

              <a
                href={flipkartUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="btn btn-secondary"
                style={{ padding: '12px 20px', fontSize: '0.9rem' }}
              >
                <ShoppingCart size={16} color="#3b82f6" />
                <span>Buy on Flipkart</span>
                <ExternalLink size={14} />
              </a>
            </div>

          </div>

        </div>
      </div>
    </div>
  );
}
