import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export function Navbar() {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="navbar">
      <Link to="/" className="navbar-brand">
        Ecommerce
      </Link>
      <nav className="navbar-links">
        <Link to="/">Produtos</Link>
        {isAuthenticated && (
          <>
            <Link to="/cart">Carrinho</Link>
            <Link to="/orders">Meus pedidos</Link>
          </>
        )}
      </nav>
      <div className="navbar-actions">
        {isAuthenticated ? (
          <>
            <span className="navbar-user">Olá, {user?.name}</span>
            <button onClick={handleLogout}>Sair</button>
          </>
        ) : (
          <>
            <Link to="/login">Entrar</Link>
            <Link to="/register">Criar conta</Link>
          </>
        )}
      </div>
    </header>
  );
}
