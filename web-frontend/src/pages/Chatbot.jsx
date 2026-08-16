import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../context/AuthContext';
import { Bot, Send, ShoppingCart, Sparkles, BookOpen, ExternalLink, User as UserIcon, Heart, Compass, Star, Calendar } from 'lucide-react';
import BookCover from '../components/BookCover';

export default function Chatbot() {
  const { t } = useTranslation();
  const { user, token } = useAuth();
  const navigate = useNavigate();
  const messagesEndRef = useRef(null);

  const [inputMessage, setInputMessage] = useState('');
  const [loading, setLoading] = useState(false);
  const [likedBookIds, setLikedBookIds] = useState(new Set());
  const [sessionId] = useState(() => (typeof crypto !== 'undefined' && crypto.randomUUID ? crypto.randomUUID() : 'session_' + Date.now() + '_' + Math.random().toString(36).substring(2, 9)));

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
      console.error("[Chatbot] Failed to fetch user likes:", e);
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
      console.error(`[Chatbot] Error toggling like for bookId ${bookId}:`, err);
      setLikedBookIds(likedBookIds);
    }
  };

  // Conversational message thread history
  const [messages, setMessages] = useState([
    {
      id: 1,
      sender: 'agent',
      text: 'Hello! I am your PageMind AI Companion. Tell me how you are feeling or what mood of book you are looking for today!',
      mood: null,
      genre: null,
      books: [],
      retailer_links: []
    }
  ]);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages, loading]);

  const handleSendMessage = async (e) => {
    e.preventDefault();
    if (!inputMessage.trim() || loading) return;

    const userText = inputMessage.trim();
    setInputMessage('');

    // Add User Message to thread
    const userMsgObj = {
      id: Date.now(),
      sender: 'user',
      text: userText
    };

    setMessages(prev => [...prev, userMsgObj]);
    setLoading(true);

    try {
      // Primary call to Spring Boot backend API bridge
      let res = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token ? { 'Authorization': `Bearer ${token}` } : {})
        },
        body: JSON.stringify({
          message: userText,
          userId: user?.id || 1,
          sessionId: sessionId,
          session_id: sessionId
        })
      });

      let data;
      if (res.ok) {
        data = await res.json();
      } else {
        // Fallback direct call to Python AI Agent service at localhost:8000/recommend
        const pythonRes = await fetch('http://localhost:8000/recommend', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            message: userText,
            sessionId: sessionId,
            session_id: sessionId
          })
        });
        if (pythonRes.ok) {
          data = await pythonRes.json();
        } else {
          throw new Error('AI Service unreachable');
        }
      }

      // Add Agent Response Bubble
      const agentMsgObj = {
        id: Date.now() + 1,
        sender: 'agent',
        text: data.message || `Based on your input, our AI agent analyzed your mood and mapped it to targeted book recommendations.`,
        mood: data.mood || 'curious',
        genre: data.detectedGenre || data.genre || 'General Fiction',
        books: data.books || data.recommendedBooks || [],
        retailer_links: data.retailer_links || []
      };

      setMessages(prev => [...prev, agentMsgObj]);
    } catch (err) {
      // Offline fallback mock bubble
      const fallbackAgentObj = {
        id: Date.now() + 1,
        sender: 'agent',
        text: `I understand you are feeling "${userText}". Here are carefully matched book recommendations to suit your mood:`,
        mood: 'calm',
        genre: 'Self-Help, Fiction',
        books: [
          { id: 1, title: 'The Midnight Library', author: 'Matt Haig', description: 'Between life and death there is a library of infinite possibilities.' },
          { id: 4, title: 'Atomic Habits', author: 'James Clear', description: 'An easy and proven way to build good habits and break bad ones.' },
          { id: 6, title: 'The Alchemist', author: 'Paulo Coelho', description: 'An inspiring fable about following your dream.' }
        ],
        retailer_links: [
          { title: 'The Midnight Library', amazon: 'https://www.amazon.in/s?k=The%20Midnight%20Library', flipkart: 'https://www.flipkart.com/search?q=The%20Midnight%20Library' },
          { title: 'Atomic Habits', amazon: 'https://www.amazon.in/s?k=Atomic%20Habits', flipkart: 'https://www.flipkart.com/search?q=Atomic%20Habits' },
          { title: 'The Alchemist', amazon: 'https://www.amazon.in/s?k=The%20Alchemist', flipkart: 'https://www.flipkart.com/search?q=The%20Alchemist' }
        ]
      };

      setMessages(prev => [...prev, fallbackAgentObj]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-container" style={{ maxWidth: '940px' }}>
      {/* Header */}
      <div style={{ textAlign: 'center', marginBottom: '24px' }}>
        <div style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: '8px',
          background: 'rgba(99, 102, 241, 0.15)',
          border: '1px solid rgba(99, 102, 241, 0.3)',
          padding: '6px 16px',
          borderRadius: '30px',
          color: '#a5b4fc',
          fontSize: '0.85rem',
          fontWeight: 600,
          marginBottom: '12px'
        }}>
          <Sparkles size={16} />
          <span>4-Node LangGraph AI Agent Bridge</span>
        </div>
        <h1 style={{ fontSize: '2rem', fontWeight: 800, marginBottom: '6px' }} className="gradient-text">
          {t('chatbot.title')}
        </h1>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>
          {t('chatbot.subtitle')}
        </p>
      </div>

      {/* Chat Container Card */}
      <div className="glass-card" style={{ padding: '0', overflow: 'hidden', display: 'flex', flexDirection: 'column', height: '620px' }}>
        
        {/* Scrollable Chat Speech-Bubble Thread */}
        <div className="chat-thread" style={{ flex: 1 }}>
          {messages.map((msg) => (
            <div key={msg.id} className={msg.sender === 'user' ? 'chat-bubble-user' : 'chat-bubble-agent'}>
              
              {/* Agent Header */}
              {msg.sender === 'agent' && (
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '10px' }}>
                  <div style={{ background: 'linear-gradient(135deg, #6366f1, #06b6d4)', padding: '6px', borderRadius: '50%', display: 'flex' }}>
                    <Bot size={16} color="#fff" />
                  </div>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: '#a5b4fc' }}>PageMind AI</span>
                </div>
              )}

              {/* Message Text */}
              <p style={{ fontSize: '0.95rem', lineHeight: '1.5', marginBottom: (msg.mood || msg.books?.length > 0) ? '14px' : '0' }}>
                {msg.text}
              </p>

              {/* Mood & Genre Badges */}
              {(msg.mood || msg.genre) && (
                <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '14px' }}>
                  {msg.mood && (
                    <span style={{
                      background: 'rgba(236, 72, 153, 0.15)',
                      color: '#f472b6',
                      border: '1px solid rgba(236, 72, 153, 0.3)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '0.78rem',
                      fontWeight: 600,
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}>
                      <Heart size={12} />
                      <span>{t('chatbot.detected_mood')}: {msg.mood}</span>
                    </span>
                  )}
                  {msg.genre && (
                    <span style={{
                      background: 'rgba(6, 182, 212, 0.15)',
                      color: '#38bdf8',
                      border: '1px solid rgba(6, 182, 212, 0.3)',
                      padding: '4px 12px',
                      borderRadius: '20px',
                      fontSize: '0.78rem',
                      fontWeight: 600,
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}>
                      <Compass size={12} />
                      <span>{t('chatbot.detected_genre')}: {msg.genre}</span>
                    </span>
                  )}
                </div>
              )}

              {/* Recommended Books Grid inside Agent Bubble */}
              {msg.books && msg.books.length > 0 && (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px', marginTop: '10px' }}>
                  {msg.books.map((book, idx) => {
                    const bookId = typeof book === 'object' ? (book.id || idx + 1) : idx + 1;
                    const title = typeof book === 'string' ? book : book.title;
                    const author = typeof book === 'object' ? book.author : '';
                    const desc = typeof book === 'object' ? book.description : '';
                    const linkObj = msg.retailer_links?.find(l => l.title === title) || msg.retailer_links?.[idx];

                    return (
                      <div
                        key={idx}
                        className="chat-book-card"
                        onClick={() => navigate(`/books/${bookId}`)}
                        style={{ cursor: 'pointer' }}
                      >
                        {/* Book Cover Visual Thumbnail */}
                        <BookCover
                          coverUrl={typeof book === 'object' ? (book.coverUrl || book.cover_url) : null}
                          title={title}
                          author={author}
                          width="56px"
                          height="76px"
                          borderRadius="8px"
                          showTitleFallback={false}
                        />


                        {/* Details & Retailer Links */}
                        <div style={{ flex: 1 }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2px' }}>
                            <h4 style={{ fontSize: '1.05rem', fontWeight: 700, color: '#f8fafc', margin: 0 }}>
                              {title}
                            </h4>
                            <button
                              onClick={(e) => toggleLike(e, bookId)}
                              style={{
                                background: likedBookIds.has(Number(bookId)) ? 'rgba(236, 72, 153, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                                border: likedBookIds.has(Number(bookId)) ? '1px solid #ec4899' : '1px solid var(--card-border)',
                                borderRadius: '50%',
                                width: '28px',
                                height: '28px',
                                display: 'flex',
                                alignItems: 'center',
                                justifyContent: 'center',
                                transition: 'all 0.2s ease',
                                cursor: 'pointer',
                                flexShrink: 0
                              }}
                              title={likedBookIds.has(Number(bookId)) ? 'Unlike book' : 'Like book'}
                            >
                              <Heart size={14} color={likedBookIds.has(Number(bookId)) ? '#ec4899' : '#94a3b8'} fill={likedBookIds.has(Number(bookId)) ? '#ec4899' : 'none'} />
                            </button>
                          </div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap', marginBottom: '6px' }}>
                            {author && (
                              <span style={{ color: 'var(--accent-cyan)', fontSize: '0.84rem', fontWeight: 500 }}>
                                by {author}
                              </span>
                            )}
                            {typeof book === 'object' && book.published_year && (
                              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: '#94a3b8', fontSize: '0.78rem' }}>
                                <Calendar size={12} color="#94a3b8" />
                                {book.published_year}
                              </span>
                            )}
                            {typeof book === 'object' && book.rating && (
                              <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', color: '#f59e0b', fontSize: '0.78rem', fontWeight: 600 }}>
                                <Star size={12} color="#f59e0b" fill="#f59e0b" />
                                {Number(book.rating).toFixed(1)}
                              </span>
                            )}
                          </div>
                          {desc && (
                            <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', lineHeight: '1.4', marginBottom: '10px' }}>
                              {desc}
                            </p>
                          )}

                          {/* Buy on Amazon / Flipkart Buttons */}
                          {linkObj && (
                            <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }} onClick={(e) => e.stopPropagation()}>
                              {linkObj.amazon && (
                                <a
                                  href={linkObj.amazon}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="btn btn-secondary"
                                  style={{ padding: '4px 10px', fontSize: '0.78rem', borderRadius: '6px' }}
                                >
                                  <ShoppingCart size={12} color="#f59e0b" />
                                  <span>{t('chatbot.buy_amazon')}</span>
                                  <ExternalLink size={10} />
                                </a>
                              )}
                              {linkObj.flipkart && (
                                <a
                                  href={linkObj.flipkart}
                                  target="_blank"
                                  rel="noopener noreferrer"
                                  className="btn btn-secondary"
                                  style={{ padding: '4px 10px', fontSize: '0.78rem', borderRadius: '6px' }}
                                >
                                  <ShoppingCart size={12} color="#3b82f6" />
                                  <span>{t('chatbot.buy_flipkart')}</span>
                                  <ExternalLink size={10} />
                                </a>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

            </div>
          ))}

          {/* Typing Indicator when Agent is analyzing */}
          {loading && (
            <div className="chat-bubble-agent" style={{ width: 'fit-content' }}>
              <div className="typing-indicator">
                <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)', marginRight: '6px' }}>PageMind AI is thinking</span>
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
                <div className="typing-dot"></div>
              </div>
            </div>
          )}

          <div ref={messagesEndRef} />
        </div>

        {/* Bottom Chat Input Form */}
        <div style={{ padding: '16px 20px', background: 'rgba(17, 20, 37, 0.95)', borderTop: '1px solid var(--border-subtle)' }}>
          <form onSubmit={handleSendMessage} style={{ display: 'flex', gap: '12px' }}>
            <input
              type="text"
              className="input-field"
              style={{ flex: 1, padding: '12px 18px', fontSize: '0.95rem', borderRadius: '24px' }}
              placeholder={t('chatbot.input_placeholder')}
              value={inputMessage}
              onChange={(e) => setInputMessage(e.target.value)}
            />
            <button type="submit" disabled={loading} className="btn btn-primary" style={{ padding: '12px 22px', borderRadius: '24px' }}>
              <Send size={18} />
              <span>{t('chatbot.submit_button')}</span>
            </button>
          </form>
        </div>

      </div>
    </div>
  );
}
