// React 애플리케이션을 DOM에 마운트하는 진입점 파일입니다.
import React from 'react';
// React 18의 createRoot API를 사용하기 위한 import입니다.
import ReactDOM from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
// 전역 스타일을 적용합니다.
import './styles.css';
// 최상위 App 컴포넌트입니다.
import { App } from './App.jsx';

// root 엘리먼트를 가져옵니다.
const rootElement = document.getElementById('root');
// React 앱을 root에 렌더링합니다.
ReactDOM.createRoot(rootElement).render(
  // React StrictMode로 개발 시 문제를 더 잘 드러나게 합니다.
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
);

