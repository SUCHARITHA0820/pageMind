import React, { useState, useEffect } from 'react';
import { BookOpen } from 'lucide-react';

const GRADIENT_PALETTES = [
  'linear-gradient(135deg, #312e81 0%, #1e1b4b 50%, #0f172a 100%)',
  'linear-gradient(135deg, #1e3a8a 0%, #172554 50%, #0f172a 100%)',
  'linear-gradient(135deg, #4c1d95 0%, #2e1065 50%, #0f172a 100%)',
  'linear-gradient(135deg, #701a75 0%, #4a044e 50%, #0f172a 100%)',
  'linear-gradient(135deg, #831843 0%, #500724 50%, #0f172a 100%)',
  'linear-gradient(135deg, #064e3b 0%, #022c22 50%, #0f172a 100%)',
  'linear-gradient(135deg, #164e63 0%, #083344 50%, #0f172a 100%)'
];

function getHashIndex(str = '') {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = (hash << 5) - hash + str.charCodeAt(i);
    hash |= 0;
  }
  return Math.abs(hash) % GRADIENT_PALETTES.length;
}

export default function BookCover({
  coverUrl,
  cover_url,
  book,
  title = 'Untitled',
  author = '',
  genre = '',
  width = '100%',
  height = '100%',
  borderRadius = '12px',
  showTitleFallback = true,
  className = '',
  style = {}
}) {
  let rawUrl = coverUrl || cover_url || (book && (book.cover_url || book.coverUrl));
  if (typeof rawUrl === 'string' && rawUrl.startsWith('http://')) {
    rawUrl = rawUrl.replace('http://', 'https://');
  }

  const actualTitle = (book && book.title) || title || 'Untitled';
  const actualAuthor = (book && book.author) || author || '';

  const [imgError, setImgError] = useState(false);

  useEffect(() => {
    setImgError(false);
  }, [rawUrl]);

  const validUrl = Boolean(
    rawUrl &&
    typeof rawUrl === 'string' &&
    rawUrl.trim().length > 0 &&
    rawUrl !== 'null' &&
    rawUrl !== 'undefined' &&
    !rawUrl.includes('placehold.co') &&
    !rawUrl.includes('via.placeholder') &&
    !rawUrl.includes('placeholder')
  );

  const gradient = GRADIENT_PALETTES[getHashIndex(actualTitle + actualAuthor)];

  return (
    <div
      className={`book-cover-container ${className}`}
      style={{
        width,
        height,
        borderRadius,
        overflow: 'hidden',
        position: 'relative',
        background: gradient,
        border: '1px solid rgba(255, 255, 255, 0.12)',
        boxShadow: '0 8px 20px rgba(0, 0, 0, 0.4)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        flexShrink: 0,
        ...style
      }}
    >
      {validUrl && !imgError ? (
        <img
          src={rawUrl}
          alt={actualTitle}
          style={{
            width: '100%',
            height: '100%',
            objectFit: 'cover',
            display: 'block'
          }}
          onError={() => setImgError(true)}
        />
      ) : (
        <div
          style={{
            padding: '12px',
            width: '100%',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            textAlign: 'center',
            boxSizing: 'border-box'
          }}
        >
          <div
            style={{
              width: '36px',
              height: '36px',
              borderRadius: '50%',
              background: 'rgba(255, 255, 255, 0.1)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              marginBottom: '8px',
              border: '1px solid rgba(255, 255, 255, 0.2)'
            }}
          >
            <BookOpen size={20} color="#a5b4fc" />
          </div>
          {showTitleFallback && (
            <>
              <h5
                style={{
                  fontSize: '0.82rem',
                  fontWeight: 700,
                  color: '#f8fafc',
                  margin: '0 0 4px 0',
                  lineHeight: '1.25',
                  display: '-webkit-box',
                  WebkitLineClamp: 3,
                  WebkitBoxOrient: 'vertical',
                  overflow: 'hidden'
                }}
              >
                {actualTitle}
              </h5>
              {actualAuthor && (
                <p
                  style={{
                    color: '#06b6d4',
                    fontSize: '0.72rem',
                    margin: 0,
                    fontWeight: 500,
                    display: '-webkit-box',
                    WebkitLineClamp: 1,
                    WebkitBoxOrient: 'vertical',
                    overflow: 'hidden'
                  }}
                >
                  {actualAuthor}
                </p>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
}

export { BookCover as BookCard };
