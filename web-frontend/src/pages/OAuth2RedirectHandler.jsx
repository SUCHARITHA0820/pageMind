import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function OAuth2RedirectHandler() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const { login } = useAuth();
  const [errorMsg, setErrorMsg] = useState(null);
  const processedRef = useRef(false);

  useEffect(() => {
    if (processedRef.current) return;
    processedRef.current = true;

    console.log('[OAuth2RedirectHandler] Step 1: Processing OAuth2 callback URL parameters...');
    const token = searchParams.get('token');
    const id = searchParams.get('id');
    const email = searchParams.get('email');
    const name = searchParams.get('name');

    if (token) {
      console.log('[OAuth2RedirectHandler] Step 2: Token parameter verified:', token ? '[PRESENT]' : '[NULL]');
      const userPayload = {
        id: id ? parseInt(id, 10) : null,
        email: email ? decodeURIComponent(email) : '',
        name: name ? decodeURIComponent(name) : (email ? email.split('@')[0] : 'Google User')
      };

      console.log('[OAuth2RedirectHandler] Step 3: Logging user in via AuthContext:', userPayload);
      login(token, userPayload);

      console.log('[OAuth2RedirectHandler] Step 4: Navigating immediately to home route (/).');
      navigate('/', { replace: true });
    } else {
      console.error('[OAuth2RedirectHandler] Step 2 Error: No token in search params:', window.location.search);
      setErrorMsg('Google login failed: No session token returned from server.');
    }
  }, [searchParams, login, navigate]);

  if (errorMsg) {
    return (
      <div className="container page-container" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <div className="glass-card" style={{ padding: '36px', textAlign: 'center', maxWidth: '450px' }}>
          <h3 style={{ marginBottom: '12px', fontSize: '1.4rem', color: '#ff4d4f' }}>Google Sign-In Failed</h3>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem', marginBottom: '24px' }}>{errorMsg}</p>
          <button className="btn btn-primary" onClick={() => navigate('/login', { replace: true })}>
            Return to Login
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="container page-container" style={{ alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
      <div className="glass-card" style={{ padding: '36px', textAlign: 'center' }}>
        <h3 style={{ marginBottom: '12px', fontSize: '1.4rem' }}>Authenticating with Google...</h3>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.95rem' }}>Setting up your session, please wait.</p>
      </div>
    </div>
  );
}
