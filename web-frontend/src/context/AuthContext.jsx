import React, { createContext, useState, useEffect, useContext, useCallback } from 'react';
import i18n from '../i18n/i18n';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  // Store JWT token in React state memory & sync with localStorage
  const [token, setToken] = useState(() => localStorage.getItem('pagemind_token') || null);
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('pagemind_user');
    try {
      return saved ? JSON.parse(saved) : null;
    } catch {
      return null;
    }
  });
  const [language, setLanguage] = useState(localStorage.getItem('pagemind_lang') || 'en');

  useEffect(() => {
    if (user && user.preferredLanguage) {
      setLanguage(user.preferredLanguage);
      localStorage.setItem('pagemind_lang', user.preferredLanguage);
      i18n.changeLanguage(user.preferredLanguage);
    }
  }, [user]);

  const login = useCallback((newToken, userData) => {
    console.log('[AuthContext] login() called with token:', newToken ? '[JWT PRESENT]' : 'NULL', 'userData:', userData);
    setToken(newToken);
    setUser(userData);
    if (newToken) {
      localStorage.setItem('pagemind_token', newToken);
    } else {
      localStorage.removeItem('pagemind_token');
    }
    if (userData) {
      localStorage.setItem('pagemind_user', JSON.stringify(userData));
    } else {
      localStorage.removeItem('pagemind_user');
    }

    if (userData && userData.preferredLanguage) {
      setLanguage(userData.preferredLanguage);
      localStorage.setItem('pagemind_lang', userData.preferredLanguage);
      i18n.changeLanguage(userData.preferredLanguage);
    }
  }, []);

  const logout = useCallback(() => {
    console.log('[AuthContext] logout() called.');
    setToken(null);
    setUser(null);
    localStorage.removeItem('pagemind_token');
    localStorage.removeItem('pagemind_user');
  }, []);

  const updateLanguage = useCallback(async (newLang) => {
    setLanguage(newLang);
    localStorage.setItem('pagemind_lang', newLang);
    i18n.changeLanguage(newLang);

    if (user) {
      const updatedUser = { ...user, preferredLanguage: newLang };
      setUser(updatedUser);

      if (token) {
        try {
          await fetch('/api/user/profile', {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ preferredLanguage: newLang })
          });
        } catch (error) {
          console.warn('Profile API language update failed, local state retained:', error);
        }
      }
    }
  }, [user, token]);

  const updateProfile = useCallback(async ({ name, preferredLanguage }) => {
    const updatedUser = { ...user, name, preferredLanguage };
    setUser(updatedUser);

    if (preferredLanguage) {
      setLanguage(preferredLanguage);
      localStorage.setItem('pagemind_lang', preferredLanguage);
      i18n.changeLanguage(preferredLanguage);
    }

    if (token) {
      try {
        const response = await fetch('/api/user/profile', {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify({ name, preferredLanguage })
        });
        if (response.ok) {
          const resData = await response.json();
          setUser(prev => ({ ...prev, ...resData }));
        }
      } catch (error) {
        console.warn('Profile PUT update failed:', error);
      }
    }
  }, [user, token]);

  return (
    <AuthContext.Provider value={{ token, user, language, login, logout, updateLanguage, updateProfile }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
