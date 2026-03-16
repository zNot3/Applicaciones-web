import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter } from 'react-router-dom';
import 'sonner/dist/styles.css';
import App from './App';
import './index.css';

// Obtenemos el elemento root del DOM
const rootElement = document.getElementById('root');

if (!rootElement) {
  throw new Error('No se encontró el elemento #root en el DOM');
}

createRoot(rootElement).render(
  <StrictMode>
    {/* BrowserRouter habilita la navegación del lado del cliente */}
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </StrictMode>
);
