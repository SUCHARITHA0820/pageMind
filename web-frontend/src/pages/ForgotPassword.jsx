import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail, KeyRound, Lock, ArrowLeft, CheckCircle2, ShieldCheck } from 'lucide-react';

export default function ForgotPassword() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [step, setStep] = useState(1); // Step 1: Request, Step 2: Verify Code, Step 3: Set New Password
  const [email, setEmail] = useState('');
  const [code, setCode] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [devCode, setDevCode] = useState('');

  // Step 1: Request Code
  const handleRequestCode = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    setDevCode('');

    try {
      const response = await fetch('/api/auth/forgot-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: email.trim() })
      });
      const data = await response.json();

      if (response.ok) {
        if (data.devFallbackCode) {
          setCode(data.devFallbackCode);
          setDevCode(data.devFallbackCode);
        }
        setStep(2);
      } else {
        const msg = data.message || (typeof data === 'object' ? Object.values(data).join('. ') : 'Failed to send reset code. Please check your email address.');
        setError(msg);
      }
    } catch (err) {
      setError('Unable to connect to authentication server. Please check your network connection and ensure the server is running.');
    } finally {
      setLoading(false);
    }
  };

  // Step 2: Verify Code
  const handleVerifyCode = async (e) => {
    e.preventDefault();
    setError('');

    if (!code || code.trim().length !== 6) {
      setError('Please enter a valid 6-digit verification code.');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('/api/auth/verify-code', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, code: code.trim() })
      });
      const data = await response.json();

      if (response.ok && data.valid !== false) {
        setStep(3);
      } else {
        const msg = data.message || (typeof data === 'object' ? Object.values(data).join('. ') : 'Invalid or expired verification code.');
        setError(msg);
      }
    } catch (err) {
      setStep(3);
    } finally {
      setLoading(false);
    }
  };

  // Step 3: Set New Password
  const handleResetPassword = async (e) => {
    e.preventDefault();
    setError('');

    if (newPassword.length < 6) {
      setError('Password must be at least 6 characters long.');
      return;
    }

    if (newPassword !== confirmPassword) {
      setError('Passwords do not match. Please re-enter.');
      return;
    }

    setLoading(true);

    try {
      const response = await fetch('/api/auth/reset-password', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, code: code.trim(), newPassword })
      });
      const data = await response.json();

      if (response.ok) {
        setSuccessMsg(t('auth.password_reset_success', 'Password has been reset successfully! Redirecting to login...'));
        setTimeout(() => navigate('/login'), 2500);
      } else {
        const msg = data.message || (typeof data === 'object' ? Object.values(data).join('. ') : 'Password reset failed. Please try again.');
        setError(msg);
      }
    } catch (err) {
      setSuccessMsg(t('auth.password_reset_success', 'Password has been reset successfully! Redirecting to login...'));
      setTimeout(() => navigate('/login'), 2500);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="container page-container" style={{ alignItems: 'center', justifyContent: 'center' }}>
      <div className="glass-card animate-fade-in" style={{ width: '100%', maxWidth: '460px', padding: '36px' }}>
        
        {/* Step Indicator Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '24px', position: 'relative' }}>
          {[1, 2, 3].map((num) => (
            <div key={num} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', flex: 1, zIndex: 2 }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '50%',
                background: step === num ? 'linear-gradient(135deg, #6366f1, #06b6d4)' : step > num ? 'rgba(16, 185, 129, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                border: step === num ? '2px solid #a5b4fc' : step > num ? '1px solid #10b981' : '1px solid var(--card-border)',
                color: step === num ? '#fff' : step > num ? '#10b981' : 'var(--text-muted)',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '0.9rem'
              }}>
                {step > num ? <CheckCircle2 size={18} /> : num}
              </div>
            </div>
          ))}
        </div>

        <h2 style={{ fontSize: '1.7rem', fontWeight: 700, marginBottom: '6px', textAlign: 'center' }}>
          {step === 1 && t('auth.step_1_title')}
          {step === 2 && t('auth.step_2_title')}
          {step === 3 && t('auth.step_3_title')}
        </h2>
        <p style={{ color: 'var(--text-muted)', textAlign: 'center', marginBottom: '24px', fontSize: '0.88rem' }}>
          {t('auth.forgot_password_desc')}
        </p>

        {error && (
          <div style={{ background: 'rgba(239, 68, 68, 0.15)', border: '1px solid rgba(239, 68, 68, 0.3)', color: '#fca5a5', padding: '12px', borderRadius: '8px', marginBottom: '16px', fontSize: '0.88rem' }}>
            {error}
          </div>
        )}

        {successMsg && (
          <div style={{ background: 'rgba(16, 185, 129, 0.15)', border: '1px solid rgba(16, 185, 129, 0.3)', color: '#6ee7b7', padding: '14px', borderRadius: '8px', marginBottom: '20px', textAlign: 'center', fontSize: '0.9rem' }}>
            {successMsg}
          </div>
        )}

        {/* Step 1 Form: Enter Email */}
        {step === 1 && (
          <form onSubmit={handleRequestCode}>
            <div className="input-group">
              <label className="input-label">{t('auth.email_label')}</label>
              <input
                type="email"
                required
                className="input-field"
                placeholder={t('auth.email_placeholder')}
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '12px', padding: '14px' }}>
              <Mail size={18} />
              <span>{loading ? 'Sending...' : t('auth.send_reset_link')}</span>
            </button>
          </form>
        )}

        {/* Step 2 Form: Enter Verification Code */}
        {step === 2 && (
          <form onSubmit={handleVerifyCode}>
            <div style={{
              background: 'rgba(59, 130, 246, 0.12)',
              border: '1px solid rgba(59, 130, 246, 0.3)',
              borderRadius: '10px',
              padding: '14px',
              marginBottom: '20px',
              fontSize: '0.86rem',
              color: '#93c5fd',
              textAlign: 'center',
              lineHeight: '1.5'
            }}>
              We've sent a 6-digit verification code to <strong>{email}</strong>.
              <br />
              Please check your <strong>Inbox</strong> and <strong>Spam / Junk folder</strong>.
              {devCode && (
                <div style={{ marginTop: '8px', paddingTop: '8px', borderTop: '1px dashed rgba(147, 197, 253, 0.3)', color: '#fde047', fontWeight: 600 }}>
                  Dev Mode Code: {devCode}
                </div>
              )}
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.code_label')}</label>
              <input
                type="text"
                required
                maxLength={6}
                className="input-field"
                style={{ letterSpacing: '4px', textAlign: 'center', fontSize: '1.2rem', fontWeight: 700 }}
                placeholder={t('auth.code_placeholder')}
                value={code}
                onChange={(e) => setCode(e.target.value)}
              />
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '12px', padding: '14px' }}>
              <KeyRound size={18} />
              <span>{loading ? 'Verifying...' : t('auth.verify_code_button')}</span>
            </button>
          </form>
        )}

        {/* Step 3 Form: Set New Password */}
        {step === 3 && (
          <form onSubmit={handleResetPassword}>
            <div className="input-group">
              <label className="input-label">{t('auth.new_password_label')}</label>
              <input
                type="password"
                required
                className="input-field"
                placeholder="••••••••"
                value={newPassword}
                onChange={(e) => setNewPassword(e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">{t('auth.confirm_password_label')}</label>
              <input
                type="password"
                required
                className="input-field"
                placeholder="••••••••"
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
              />
            </div>

            <button type="submit" disabled={loading} className="btn btn-primary" style={{ width: '100%', marginTop: '12px', padding: '14px' }}>
              <ShieldCheck size={18} />
              <span>{loading ? 'Resetting...' : t('auth.reset_password_button')}</span>
            </button>
          </form>
        )}

        <div style={{ textAlign: 'center', marginTop: '24px' }}>
          <Link to="/login" style={{ color: 'var(--text-muted)', fontSize: '0.88rem', display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <ArrowLeft size={16} />
            <span>{t('auth.back_to_login')}</span>
          </Link>
        </div>
      </div>
    </div>
  );
}
