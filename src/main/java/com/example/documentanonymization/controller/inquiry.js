import React, { useRef } from 'react';
import Navbar from '../Navbar/navbar';
import './inquiry.css';
import { useState } from 'react';
import searchIcon from '../Assets/searchicon.png';
import chatIcon from '../Assets/chaticon.png';
import closeIcon from '../Assets/closeicon.png';

// Tarihi formatla fonksiyonu ekleyin (bileşen dışında)
const formatDate = (dateString) => {
    if (!dateString) return '';

    const date = new Date(dateString);

    const day = date.getDate().toString().padStart(2, '0');
    const month = (date.getMonth() + 1).toString().padStart(2, '0');
    const year = date.getFullYear();
    const hours = date.getHours().toString().padStart(2, '0');
    const minutes = date.getMinutes().toString().padStart(2, '0');
    const seconds = date.getSeconds().toString().padStart(2, '0');

    return `${day}-${month}-${year} ${hours}:${minutes}:${seconds}`;
};

function Home() {

    const [inquiryType, setInquiryType] = useState('article');
    const [showResult, setShowResult] = useState(false);
    const [showChatPanel, setShowChatPanel] = useState(false);
    const [trackingNumber, setTrackingNumber] = useState('');
    const [email, setEmail] = useState('');
    const [activeTab, setActiveTab] = useState('active');
    const [trackingNumberArticleData, setTrackingNumberArticleData] = useState(null);
    const [emailArticleData, setEmailArticleData] = useState([]);
    const [sendMessages, setSendMessages] = useState([]);
    const [senderEmail, setSenderEmail] = useState('');
    const [messages, setMessages] = useState([]);

    const articleInputRef = useRef();
    const emailInputRef = useRef();
    const messageInputRef = useRef();

    const handleSendMessage = async (e) => {
        e.preventDefault();
        const messageValue = messageInputRef.current.value;
        try {
            const response = fetch('/messages/create', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    email: senderEmail,
                    content: messageValue
                })
            });
            if (response.ok) {
                handleMessages();
            }
            if (!response.ok) {
                alert("Mesaj gönderilemedi. Lütfen tekrar deneyin.");
            }
        }
        catch (error) {
            alert("Bağlantı hatası: " + error.message);
        }
        setSendMessages([...sendMessages, messageValue]);
        messageInputRef.current.value = '';
    }

    const handleMessages = async () => {
        try {
            const response = await fetch(`/messages/email/${senderEmail}`);
            if (response.ok) {
                const data = await response.json();
                setMessages(data);
            } else {
                alert("Mesajlar getirilemedi. Lütfen tekrar deneyin.");
            }
        }
        catch (error) {
            alert("Bağlantı hatası: " + error.message);
        }
    }

    const handleInquiryType = (e) => {
        setInquiryType(e.target.value);
        setShowResult(false);
    }

    const handleChatPanel = () => {
        setShowChatPanel(!showChatPanel);
    }

    const handleSetEmail = (e) => {
        e.preventDefault();
        const emailValue = emailInputRef.current.value;
        setSenderEmail(emailValue);
        handleMessages();
        setShowChatPanel(true);
    }

    const handleSubmit = async (e) => {
        e.preventDefault();
        if (inquiryType === 'article') {
            const trackingNumberValue = articleInputRef.current.value;
            setTrackingNumber(trackingNumberValue);
            try {
                console.log(trackingNumber);
                const response = await fetch(`/articles/tracking/${trackingNumberValue}`);

                if (response.ok) {
                    const data = await response.json();
                    setTrackingNumberArticleData(data);
                    setShowResult(true);
                } else {
                    alert("Makale bulunamadı. Lütfen tekrar deneyin.");
                    setTrackingNumberArticleData(null);
                }
            }
            catch (error) {
                alert("Bağlantı hatası: " + error.message);
            }
        } else {
            const emailValue = emailInputRef.current.value;
            setEmail(emailValue);
            try {
                const response = await fetch(`/articles/email/${emailValue}`);
                if (response.ok) {
                    const data = await response.json();
                    setEmailArticleData(data);
                    setShowResult(true);
                } else {
                    alert("Makale bulunamadı. Lütfen tekrar deneyin.");
                    setEmailArticleData([]);
                }
            }
            catch (error) {
                alert("Bağlantı hatası: " + error.message);
            }
        }
    }

    const handleTabChange = (tab) => {
        setActiveTab(tab);
    }

    return (
        <div>
            <Navbar />
            <div className="inquiry-article-header">
                <h1>Makale Takip Sistemi</h1>
            </div>
            <div className="inquiry-card">
                <h2>Makale Sorgulama</h2>
                <p className='inquiry-labelinfo'>E-posta adresiniz veya makale takip numaranız ile makalenizin durumunu sorgulayabilirsiniz.</p>
                <br></br>

                <div className="inquiry-form">
                    <input
                        type='radio'
                        id='article'
                        name='inquiry'
                        value='article'
                        className='inquiry-radio'
                        checked={inquiryType === 'article'}
                        onChange={handleInquiryType}
                    />
                    <label htmlFor='article' className='inquiry-articlenumber-label'>Takip Numarası ile Sorgula</label>
                    <input
                        type='radio'
                        id='email'
                        name='inquiry'
                        value='email'
                        className='inquiry-radio'
                        checked={inquiryType === 'email'}
                        onChange={handleInquiryType}
                    />
                    <label htmlFor='email' className='inquiry-email-label'>E-posta ile Sorgula</label>
                </div>

                {inquiryType === 'article' ? (
                    <div className='inquiry-article-input'>
                        <span className='inquiry-radio-span'>Takip Numarası</span>
                        <div className="inquiry-input-with-icon">
                            <img src={searchIcon} alt="search" className="inquiry-input-icon" />
                            <input
                                type='text'
                                placeholder='Örn: 12345678'
                                ref={articleInputRef}
                            />
                        </div>
                        <button onClick={handleSubmit}>Sorgula</button>
                    </div>
                ) : (
                    <div className='inquiry-article-input'>
                        <span className='inquiry-radio-span'>E-posta Adresi</span>
                        <div className="inquiry-input-with-icon">
                            <img src={searchIcon} alt="search" className="inquiry-input-icon" />
                            <input
                                type='text'
                                placeholder='Örn: ornek@email.com'
                                ref={emailInputRef}
                            />
                        </div>
                        <button onClick={handleSubmit}>Sorgula</button>
                    </div>
                )}
            </div>

            {showResult && (
                <div className='inquiry-card'>
                    <h2>Sorgu Sonuçları</h2>
                    {inquiryType === 'article' ? (
                        <p className='inquiry-labelinfo'>"{trackingNumber}" takip numaralı makale sonuçları</p>
                    ) : (
                        <p className='inquiry-labelinfo'>"{email}" e-posta adresine ait makale sonuçları</p>
                    )}

                    <div className='inquiry-tablist'>
                        <div className='inquiry-tablist-item'>
                            <div
                                className={`inquiry-tab ${activeTab === 'active' ? 'active' : ''}`}
                                onClick={() => handleTabChange('active')}
                            >
                                Aktif Makaleler
                            </div>
                            <div
                                className={`inquiry-tab ${activeTab === 'evaluated' ? 'active' : ''}`}
                                onClick={() => handleTabChange('evaluated')}
                            >
                                Değerlendirilen Makaleler
                            </div>
                        </div>
                    </div>

                    {inquiryType === 'article' && (
                        <div className="tab-content">
                            {activeTab === 'active' && trackingNumberArticleData ? (
                                <div className="inquiry-articleinfo-card">
                                    <div className="inquiry-articleinfo">
                                        <span className="inquiry-articletitle">Makale Başlığı</span>
                                        <span className="inquiry-articlestatus">Değerlendirmede</span>
                                    </div>
                                    <span className="inquiry-info-label">Takip No: {trackingNumberArticleData.trackingNumber}</span>
                                    <div className="inquiry-articleinfo2">
                                        <div className="inquiry-info-column">
                                            <span className="inquiry-info-label">Gönderim Tarihi:</span>
                                            <span className="inquiry-info-value">
                                                    {trackingNumberArticleData.submissionDate ?
                                                        formatDate(trackingNumberArticleData.submissionDate) :
                                                        ''}
                                                </span>
                                            <span className="inquiry-info-label">Hakem Sayısı:</span>
                                            <span className="inquiry-info-value">0/1 Tamamlandı</span>
                                        </div>
                                        <div className="inquiry-info-column right">
                                            <span className="inquiry-info-label">Son Güncelleme:</span>
                                            <span className="inquiry-info-value">
                                                    {trackingNumberArticleData.reviewDate ?
                                                        formatDate(trackingNumberArticleData.reviewDate) :
                                                        ''}
                                                </span>
                                        </div>
                                    </div>
                                </div>
                            ) : (
                                <div className="inquiry-no-article">
                                    <span>Bu takip numarasına ait değerlendirilen makale bulunamadı.</span>
                                </div>
                            )}
                        </div>
                    )}

                    {inquiryType === 'email' && (
                        <div className="tab-content">
                            {activeTab === 'active' && emailArticleData && emailArticleData.length > 0 ? (
                                emailArticleData.map((emailArticle) => (
                                    <div className="inquiry-articleinfo-card" key={emailArticle.trackingNumber}>
                                        <div className="inquiry-articleinfo">
                                            <span className="inquiry-articletitle">Makale Başlığı</span>
                                            <span className="inquiry-articlestatus">Değerlendirmede</span>
                                        </div>
                                        <span className="inquiry-info-label">Takip No: {emailArticle.trackingNumber}</span>
                                        <div className="inquiry-articleinfo2">
                                            <div className="inquiry-info-column">
                                                <span className="inquiry-info-label">Gönderim Tarihi:</span>
                                                <span className="inquiry-info-value">
                                                        {emailArticle.submissionDate ?
                                                            formatDate(emailArticle.submissionDate) :
                                                            ''}
                                                    </span>
                                                <span className="inquiry-info-label">Hakem Sayısı:</span>
                                                <span className="inquiry-info-value">0/1 Tamamlandı</span>
                                            </div>
                                            <div className="inquiry-info-column right">
                                                <span className="inquiry-info-label">Son Güncelleme:</span>
                                                <span className="inquiry-info-value">
                                                        {emailArticle.reviewDate ?
                                                            formatDate(emailArticle.reviewDate) :
                                                            ''}
                                                    </span>
                                            </div>
                                        </div>
                                    </div>
                                ))
                            ) : (
                                <div className="inquiry-no-article">
                                    <span>Bu e-posta adresine ait değerlendirilen makale bulunamadı.</span>
                                </div>
                            )}
                        </div>
                    )}
                </div>
            )}
            <button className="inquiry-chat-icon-button" onClick={handleChatPanel}>
                <img src={chatIcon} alt="chat" className="inquiry-chat-icon" />
            </button>
            {showChatPanel && senderEmail.length>0 && (
                <div className="inquiry-chat-panel">
                    <div className="inquiry-chat-header">
                        <div className="inquiry-chat-title">
                            <span>Admin Destek </span>
                            <span className="inquiry-chat-status">Çevrimiçi</span>
                        </div>
                        <button className="inquiry-chatclose-button" onClick={handleChatPanel}>
                            <img src={closeIcon} alt="chat" className="inquiry-chatclose-icon" />
                        </button>
                    </div>
                    <div className="inquiry-chat-content">
                        <div className="inquiry-chat-adminmessage">
                            <span>Merhaba! Size nasıl yardımcı olabilirim?</span>
                            <span className="inquiry-chat-clock">14.30</span>
                        </div>

                        {sendMessages.map((message, index) => (
                            <div className="inquiry-chat-authormessage" key={index}>
                                <span>{message}</span>
                                <span className="inquiry-chat-authorclock">14.31</span>
                            </div>
                        ))}
                    </div>

                    <form className="inquiry-chat-input" onSubmit={handleSendMessage}>
                        <textarea
                            ref={messageInputRef}
                            placeholder="Mesajınızı yazın..."
                            required
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && !e.shiftKey) {
                                    e.preventDefault();
                                    handleSendMessage(e);
                                }
                            }}
                        ></textarea>
                        <button type="submit">
                            <img src={chatIcon} alt="chat" className="inquiry-chat-messageicon" />
                        </button>
                    </form>
                </div>
            )}
            {showChatPanel && senderEmail.length===0 && (
                <form className="inquiry-chat-panel-email" onSubmit={handleSetEmail}>
                    <span>E-posta adresinizi girerek destek alabilirsiniz.</span>
                    <input
                        type='text'
                        placeholder='Örn: ornek@email.com'
                        ref={emailInputRef}
                    />
                    <button type="submit">
                        İletişime Geç
                    </button>
                </form>
            )}
        </div>
    )
}

export default Home;