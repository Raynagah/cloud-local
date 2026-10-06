import React, { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { getNotificaciones, marcarNotificacionLeida } from '../functions/apiService';
import './css/NotificationButton.css';

export function NotificationButton() {
    const [notificaciones, setNotificaciones] = useState([]);
    const [isOpen, setIsOpen] = useState(false);
    const dropdownRef = useRef(null);
    const navigate = useNavigate();

    const cargarNotificaciones = async () => {
        try {
            const res = await getNotificaciones();
            setNotificaciones(res.data || []);
        } catch (error) {
            console.error('Error al cargar notificaciones en navbar', error);
        }
    };

    useEffect(() => {
        cargarNotificaciones();
        // Polling opcional cada 30 segundos para actualizar sin recargar la página
        const interval = setInterval(cargarNotificaciones, 30000);
        return () => clearInterval(interval);
    }, []);

    // Cerrar el menú al hacer clic fuera del componente
    useEffect(() => {
        const handleClickOutside = (event) => {
            if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
                setIsOpen(false);
            }
        };
        document.addEventListener('mousedown', handleClickOutside);
        return () => document.removeEventListener('mousedown', handleClickOutside);
    }, []);

    const sinLeerCount = notificaciones.filter(n => !n.leido).length;

    const handleMarcarLeida = async (e, id) => {
        e.stopPropagation();
        try {
            await marcarNotificacionLeida(id);
            setNotificaciones(prev =>
                prev.map(n => n.id === id ? { ...n, leido: true } : n)
            );
        } catch (error) {
            console.error('Error al marcar como leída', error);
        }
    };

    const handleVerTodas = () => {
        setIsOpen(false);
        navigate('/notificaciones');
    };

    return (
        <div className="notif-dropdown" ref={dropdownRef}>
            <button
                className="notif-btn"
                onClick={() => setIsOpen(!isOpen)}
                aria-label="Notificaciones"
            >
                <span className="notif-btn__icon">🔔</span>
                {sinLeerCount > 0 && (
                    <span className="notif-btn__badge">
                        {sinLeerCount > 9 ? '9+' : sinLeerCount}
                    </span>
                )}
            </button>

            {isOpen && (
                <div className="notif-menu">
                    <div className="notif-menu__header">
                        <span>Notificaciones</span>
                        {sinLeerCount > 0 && (
                            <span className="notif-menu__unread-tag">{sinLeerCount} nuevas</span>
                        )}
                    </div>

                    <div className="notif-menu__body">
                        {notificaciones.length === 0 ? (
                            <p className="notif-menu__empty">No tienes notificaciones</p>
                        ) : (
                            notificaciones.slice(0, 5).map((notif) => (
                                <div
                                    key={notif.id}
                                    className={`notif-item ${!notif.leido ? 'notif-item--unread' : ''}`}
                                    onClick={handleVerTodas}
                                >
                                    <div className="notif-item__content">
                                        <strong className="notif-item__title">{notif.titulo}</strong>
                                        <p className="notif-item__message">{notif.mensaje}</p>
                                        <small className="notif-item__date">
                                            {new Date(notif.fechaCreacion).toLocaleString()}
                                        </small>
                                    </div>
                                    {!notif.leido && (
                                        <button
                                            className="notif-item__read-btn"
                                            title="Marcar como leída"
                                            onClick={(e) => handleMarcarLeida(e, notif.id)}
                                        >
                                            ✓
                                        </button>
                                    )}
                                </div>
                            ))
                        )}
                    </div>

                    <div className="notif-menu__footer">
                        <button onClick={handleVerTodas} className="notif-menu__see-all">
                            Ver todo el historial
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
}